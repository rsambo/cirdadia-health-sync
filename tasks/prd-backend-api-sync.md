# PRD: Backend API for Health Sync Data

## Introduction/Overview

The CircadiaHealthSync Android app syncs health data (steps) from Health Connect to a backend server. The backend API must handle two types of sync operations:

1. **Full Sync** - Replace all records for a user (used on first sync or when the change token expires)
2. **Incremental Sync** - Upsert specific records and delete specific records by ID (used for efficient ongoing syncs)

This document describes the exact API contract the backend must implement to correctly receive and process sync payloads from the Android app.

## Goals

1. Accept and correctly process sync payloads from the Android app
2. Handle both full and incremental sync types appropriately
3. Use Health Connect record IDs as unique identifiers to enable proper upsert/delete operations
4. Prevent duplicate records while supporting record updates
5. Return appropriate success/error responses to the Android app

## API Contract

### Endpoint

```
POST /api/sync
```

### Request Headers

```
Content-Type: application/json
Authorization: Bearer <user-token>  (if applicable)
```

### Request Payload Structure

```json
{
  "syncType": "full" | "incremental",
  "records": [
    {
      "id": "string",       // Health Connect unique record ID
      "type": "steps",      // Record type (currently only "steps")
      "date": "YYYY-MM-DD", // Date of the record
      "count": 12345,       // Step count (Long integer)
      "data": {
        "startTime": "2025-12-15T09:30:00Z",  // ISO 8601 timestamp
        "endTime": "2025-12-15T09:45:00Z"     // ISO 8601 timestamp
      }
    }
  ],
  "deletedRecordIds": ["string"]  // Array of Health Connect record IDs to delete
}
```

### Field Descriptions

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `syncType` | string | Yes | Either `"full"` or `"incremental"` |
| `records` | array | Yes | Array of record objects to upsert (can be empty) |
| `records[].id` | string | Yes | Unique Health Connect record ID (e.g., `"abc123-def456-..."`) |
| `records[].type` | string | Yes | Type of record, currently always `"steps"` |
| `records[].date` | string | Yes | Date in `YYYY-MM-DD` format (e.g., `"2025-12-15"`) |
| `records[].count` | long | Yes | Step count value |
| `records[].data` | object | Yes | Time range data for the record |
| `records[].data.startTime` | string | Yes | Start time in ISO 8601 format (e.g., `"2025-12-15T09:30:00Z"`) |
| `records[].data.endTime` | string | Yes | End time in ISO 8601 format (e.g., `"2025-12-15T09:45:00Z"`) |
| `deletedRecordIds` | array | Yes | Array of record IDs to delete (can be empty) |

## Functional Requirements

### 1. Sync Type Handling

#### 1.1 Full Sync (`syncType: "full"`)

When `syncType` is `"full"`, the backend must:

1. **Delete all existing records** for the authenticated user (for the relevant record type)
2. **Insert all records** from the `records` array
3. **Ignore `deletedRecordIds`** (since all records are being replaced anyway)

**Use case:** First-time sync, or when the Android app's change token has expired and it needs to re-sync everything.

**Example payload:**
```json
{
  "syncType": "full",
  "records": [
    { "id": "hc-001", "type": "steps", "date": "2025-12-14", "count": 8500 },
    { "id": "hc-002", "type": "steps", "date": "2025-12-15", "count": 5432 }
  ],
  "deletedRecordIds": []
}
```

**Backend action:**
```
1. DELETE FROM health_records WHERE user_id = ? AND type = 'steps'
2. INSERT all records from payload
```

#### 1.2 Incremental Sync (`syncType: "incremental"`)

When `syncType` is `"incremental"`, the backend must:

1. **Upsert each record** in the `records` array (insert if new, update if exists based on `id`)
2. **Delete records** matching the IDs in `deletedRecordIds`
3. **Preserve all other existing records** that are not in the payload

**Use case:** Efficient ongoing sync where only changed records are transmitted.

**Example payload:**
```json
{
  "syncType": "incremental",
  "records": [
    { "id": "hc-003", "type": "steps", "date": "2025-12-15", "count": 6000 }
  ],
  "deletedRecordIds": ["hc-001"]
}
```

**Backend action:**
```
1. UPSERT record hc-003 (insert or update based on id)
2. DELETE FROM health_records WHERE id = 'hc-001' AND user_id = ?
3. All other records remain unchanged
```

### 2. Record ID as Primary Key

- The `id` field from Health Connect must be stored and used as the unique identifier
- This ID is stable across syncs and uniquely identifies each record
- Use this ID for upsert operations (ON CONFLICT UPDATE) and deletions

### 3. Response Format

#### Success Response

```json
{
  "success": true,
  "message": "Sync successful",
  "recordCount": 2
}
```

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | `true` if sync was successful |
| `message` | string | Human-readable status message |
| `recordCount` | integer | Total number of records now stored for this user |

#### Error Response

```json
{
  "success": false,
  "message": "Error description"
}
```

### 4. Edge Cases

| Scenario | Expected Behavior |
|----------|-------------------|
| `syncType: "incremental"` with empty `records` and empty `deletedRecordIds` | Success, no changes made |
| `syncType: "incremental"` with empty `records` and populated `deletedRecordIds` | Only perform deletions |
| `syncType: "incremental"` with populated `records` and empty `deletedRecordIds` | Only perform upserts |
| `syncType: "full"` with empty `records` | Delete all existing records |
| `deletedRecordIds` contains an ID that doesn't exist | Ignore silently, don't error |
| Duplicate IDs in `records` array | Process all, last one wins |

## Database Schema Suggestion

```sql
CREATE TABLE health_records (
    id VARCHAR(255) PRIMARY KEY,      -- Health Connect record ID
    user_id VARCHAR(255) NOT NULL,    -- User identifier
    type VARCHAR(50) NOT NULL,        -- Record type (e.g., 'steps')
    date DATE NOT NULL,               -- Date of the record
    count BIGINT NOT NULL,            -- Step count
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_user_type (user_id, type),
    INDEX idx_user_date (user_id, date)
);
```

## Pseudocode Implementation

```javascript
async function handleSync(userId, payload) {
  const { syncType, records, deletedRecordIds } = payload;
  
  if (syncType === 'full') {
    // Full sync: Replace all records
    await db.query('DELETE FROM health_records WHERE user_id = ?', [userId]);
    
    for (const record of records) {
      await db.query(
        'INSERT INTO health_records (id, user_id, type, date, count) VALUES (?, ?, ?, ?, ?)',
        [record.id, userId, record.type, record.date, record.count]
      );
    }
  } else if (syncType === 'incremental') {
    // Incremental sync: Upsert records and delete specified IDs
    
    // Upsert each record
    for (const record of records) {
      await db.query(`
        INSERT INTO health_records (id, user_id, type, date, count, updated_at)
        VALUES (?, ?, ?, ?, ?, NOW())
        ON CONFLICT (id) DO UPDATE SET
          count = EXCLUDED.count,
          date = EXCLUDED.date,
          updated_at = NOW()
      `, [record.id, userId, record.type, record.date, record.count]);
    }
    
    // Delete specified records
    if (deletedRecordIds.length > 0) {
      await db.query(
        'DELETE FROM health_records WHERE id IN (?) AND user_id = ?',
        [deletedRecordIds, userId]
      );
    }
  }
  
  // Get final record count
  const countResult = await db.query(
    'SELECT COUNT(*) as count FROM health_records WHERE user_id = ?',
    [userId]
  );
  
  return {
    success: true,
    message: 'Sync successful',
    recordCount: countResult[0].count
  };
}
```

## Non-Goals (Out of Scope)

- User authentication/authorization (assumed to be handled separately)
- Rate limiting
- Data validation beyond basic type checking
- Support for record types other than "steps" (future feature)
- Batch processing or queuing (sync is synchronous)

## Success Metrics

1. Full sync correctly replaces all existing records
2. Incremental sync correctly upserts and deletes only specified records
3. No duplicate records after multiple syncs
4. Deleted records are properly removed from the database
5. Response includes accurate `recordCount`

## Testing Scenarios

### Test 1: First Sync (Full)
```json
// Request
{ "syncType": "full", "records": [{ "id": "a", ... }, { "id": "b", ... }], "deletedRecordIds": [] }

// Expected: Database contains exactly records "a" and "b"
```

### Test 2: Incremental Add
```json
// Existing: records "a", "b"
// Request
{ "syncType": "incremental", "records": [{ "id": "c", ... }], "deletedRecordIds": [] }

// Expected: Database contains records "a", "b", "c"
```

### Test 3: Incremental Update
```json
// Existing: record "a" with count=100
// Request
{ "syncType": "incremental", "records": [{ "id": "a", "count": 200, ... }], "deletedRecordIds": [] }

// Expected: Record "a" now has count=200
```

### Test 4: Incremental Delete
```json
// Existing: records "a", "b", "c"
// Request
{ "syncType": "incremental", "records": [], "deletedRecordIds": ["b"] }

// Expected: Database contains records "a", "c"
```

### Test 5: Incremental Mixed
```json
// Existing: records "a", "b"
// Request
{ "syncType": "incremental", "records": [{ "id": "c", ... }], "deletedRecordIds": ["a"] }

// Expected: Database contains records "b", "c"
```

### Test 6: Full Sync Replaces Everything
```json
// Existing: records "a", "b", "c"
// Request
{ "syncType": "full", "records": [{ "id": "x", ... }], "deletedRecordIds": [] }

// Expected: Database contains only record "x"
```

## Open Questions

None - this document fully describes the required API contract.

