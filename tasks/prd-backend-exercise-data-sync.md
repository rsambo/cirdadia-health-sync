# PRD: Backend API for Exercise Session Sync

## Introduction/Overview

The CircadiaHealthSync Android app syncs health data (steps and exercise sessions) from Health Connect to a backend server. This document describes the backend API requirements to receive, store, and manage exercise session data alongside the existing steps data.

The backend must handle two types of sync operations:
1. **Full Sync** - Replace all records for a user (used on first sync or when the change token expires)
2. **Incremental Sync** - Upsert specific records and delete specific records by ID (used for efficient ongoing syncs)

## Goals

1. Accept and correctly process sync payloads from the Android app
2. Handle both full and incremental sync types appropriately
3. Store multiple record types (steps, exercise sessions) with polymorphic handling
4. Use Health Connect record IDs as unique identifiers to enable proper upsert/delete operations
5. Prevent duplicate records while supporting record updates and deletions
6. Return appropriate success/error responses to the Android app

## API Contract

### Endpoint

```
POST /api/sync/health-data
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
    // Step records
    {
      "id": "string",
      "type": "steps",
      "date": "YYYY-MM-DD",
      "count": 12345,
      "data": {
        "startTime": "2025-12-15T09:30:00Z",
        "endTime": "2025-12-15T09:45:00Z"
      }
    },
    // Exercise session records
    {
      "id": "string",
      "type": "exercise_session",
      "exerciseType": "RUNNING",
      "startTime": "2025-12-17T08:00:00Z",
      "endTime": "2025-12-17T08:45:00Z",
      "source": "com.fitbit.FitbitMobile",
      "title": "Morning Run",
      "notes": "Felt great!",
      "data": {
        "energyBurned": 350.5,
        "totalDistance": 5200.0,
        "steps": 4800,
        "elevationGain": 45.0,
        "avgHeartRate": 145.0,
        "laps": [
          {
            "startTime": "2025-12-17T08:00:00Z",
            "endTime": "2025-12-17T08:10:00Z"
          }
        ]
      }
    }
  ],
  "deletedRecordIds": ["string"]
}
```

## Data Models

### 1. Step Record

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique Health Connect record ID |
| `type` | string | Yes | Always `"steps"` |
| `date` | string | Yes | Date in `YYYY-MM-DD` format |
| `count` | long | Yes | Step count value |
| `data.startTime` | string | Yes | Start time in ISO 8601 format |
| `data.endTime` | string | Yes | End time in ISO 8601 format |

### 2. Exercise Session Record

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `id` | string | Yes | Unique Health Connect record ID |
| `type` | string | Yes | Always `"exercise_session"` |
| `exerciseType` | string | Yes | Exercise type enum (see below) |
| `startTime` | string | Yes | Start time in ISO 8601 format |
| `endTime` | string | Yes | End time in ISO 8601 format |
| `source` | string | Yes | Package name of app that created the record |
| `title` | string | No | Optional user title |
| `notes` | string | No | Optional user notes |
| `data.energyBurned` | double | No | Calories burned (kcal) |
| `data.totalDistance` | double | No | Distance in meters |
| `data.steps` | long | No | Step count during session |
| `data.elevationGain` | double | No | Elevation gained in meters |
| `data.avgHeartRate` | double | No | Average heart rate (bpm) |
| `data.laps` | array | No | List of lap objects (see below) |

### 3. Exercise Lap

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `startTime` | string | Yes | Start time in ISO 8601 format |
| `endTime` | string | Yes | End time in ISO 8601 format |

### 4. Exercise Types (Enum Values)

The `exerciseType` field will contain one of the following string values:

| Value | Description |
|-------|-------------|
| `RUNNING` | Running outdoors |
| `WALKING` | Walking |
| `HIKING` | Hiking |
| `BIKING` | Cycling |
| `MOUNTAIN_BIKING` | Mountain biking |
| `SWIMMING_POOL` | Pool swimming |
| `SWIMMING_OPEN_WATER` | Open water swimming |
| `STRENGTH_TRAINING` | Strength training |
| `WEIGHTLIFTING` | Weightlifting |
| `HIIT` | High-intensity interval training |
| `ELLIPTICAL` | Elliptical machine |
| `ROWING_MACHINE` | Rowing machine |
| `STAIR_CLIMBING` | Stair climbing |
| `TREADMILL` | Treadmill running |
| `YOGA` | Yoga |
| `PILATES` | Pilates |
| `JUMP_ROPE` | Jump rope |
| `KAYAKING` | Kayaking |
| `SKIING` | Skiing |
| `SKIING_CROSS_COUNTRY` | Cross-country skiing |
| `TENNIS` | Tennis |
| `GOLF` | Golf |
| `ROCK_CLIMBING` | Rock climbing |
| `WORKOUT` | Generic workout |
| `OTHER` | Unknown exercise type |

## Functional Requirements

### 1. Sync Type Handling

#### 1.1 Full Sync (`syncType: "full"`)

When `syncType` is `"full"`, the backend must:

1. **Delete all existing records** for the authenticated user (both steps and exercise sessions)
2. **Insert all records** from the `records` array
3. **Ignore `deletedRecordIds`** (since all records are being replaced anyway)

**Use case:** First-time sync, or when the Android app's change token has expired and it needs to re-sync everything.

#### 1.2 Incremental Sync (`syncType: "incremental"`)

When `syncType` is `"incremental"`, the backend must:

1. **Upsert each record** in the `records` array (insert if new, update if exists based on `id`)
2. **Delete records** matching the IDs in `deletedRecordIds`
3. **Preserve all other existing records** that are not in the payload

**Use case:** Efficient ongoing sync where only changed records are transmitted.

### 2. Polymorphic Record Handling

The `records` array contains mixed record types. The backend must:

1. Check the `type` field to determine record type (`"steps"` or `"exercise_session"`)
2. Store each record type in appropriate tables/collections
3. Handle unknown types gracefully (log warning, skip record)

### 3. Record ID as Primary Key

- The `id` field from Health Connect must be stored and used as the unique identifier
- This ID is stable across syncs and uniquely identifies each record
- Use this ID for upsert operations (ON CONFLICT UPDATE) and deletions

### 4. Null Handling

- Exercise session metrics (`energyBurned`, `totalDistance`, `steps`, `elevationGain`, `avgHeartRate`, `laps`) can be `null`
- Store `null` values as-is (do not convert to 0)
- Backend should handle missing/null fields gracefully

### 5. Response Format

#### Success Response

```json
{
  "success": true,
  "message": "Sync successful",
  "recordCount": 15
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

## Database Schema Suggestion

### Steps Table

```sql
CREATE TABLE step_records (
    id VARCHAR(255) PRIMARY KEY,          -- Health Connect record ID
    user_id VARCHAR(255) NOT NULL,        -- User identifier
    date DATE NOT NULL,                   -- Date of the record
    count BIGINT NOT NULL,                -- Step count
    start_time TIMESTAMP NOT NULL,        -- Start time
    end_time TIMESTAMP NOT NULL,          -- End time
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_user_date (user_id, date)
);
```

### Exercise Sessions Table

```sql
CREATE TABLE exercise_sessions (
    id VARCHAR(255) PRIMARY KEY,          -- Health Connect record ID
    user_id VARCHAR(255) NOT NULL,        -- User identifier
    exercise_type VARCHAR(50) NOT NULL,   -- Exercise type enum
    start_time TIMESTAMP NOT NULL,        -- Start time
    end_time TIMESTAMP NOT NULL,          -- End time
    source VARCHAR(255) NOT NULL,         -- Source app package name
    title VARCHAR(255),                   -- Optional title
    notes TEXT,                           -- Optional notes
    energy_burned DOUBLE,                 -- Calories (nullable)
    total_distance DOUBLE,                -- Meters (nullable)
    steps BIGINT,                         -- Step count (nullable)
    elevation_gain DOUBLE,                -- Meters (nullable)
    avg_heart_rate DOUBLE,                -- BPM (nullable)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    
    INDEX idx_user_date (user_id, start_time)
);
```

### Exercise Laps Table

```sql
CREATE TABLE exercise_laps (
    id SERIAL PRIMARY KEY,
    exercise_session_id VARCHAR(255) NOT NULL,  -- FK to exercise_sessions.id
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    
    FOREIGN KEY (exercise_session_id) REFERENCES exercise_sessions(id) ON DELETE CASCADE
);
```

## Pseudocode Implementation

```javascript
async function handleSync(userId, payload) {
  const { syncType, records, deletedRecordIds } = payload;
  
  if (syncType === 'full') {
    // Full sync: Replace all records
    await db.query('DELETE FROM step_records WHERE user_id = ?', [userId]);
    await db.query('DELETE FROM exercise_sessions WHERE user_id = ?', [userId]);
    
    for (const record of records) {
      await upsertRecord(userId, record);
    }
  } else if (syncType === 'incremental') {
    // Incremental sync: Upsert records and delete specified IDs
    
    // Upsert each record
    for (const record of records) {
      await upsertRecord(userId, record);
    }
    
    // Delete specified records (from both tables)
    if (deletedRecordIds.length > 0) {
      await db.query(
        'DELETE FROM step_records WHERE id IN (?) AND user_id = ?',
        [deletedRecordIds, userId]
      );
      await db.query(
        'DELETE FROM exercise_sessions WHERE id IN (?) AND user_id = ?',
        [deletedRecordIds, userId]
      );
    }
  }
  
  // Get final record count
  const stepCount = await db.query(
    'SELECT COUNT(*) as count FROM step_records WHERE user_id = ?', [userId]
  );
  const exerciseCount = await db.query(
    'SELECT COUNT(*) as count FROM exercise_sessions WHERE user_id = ?', [userId]
  );
  
  return {
    success: true,
    message: 'Sync successful',
    recordCount: stepCount + exerciseCount
  };
}

async function upsertRecord(userId, record) {
  if (record.type === 'steps') {
    await db.query(`
      INSERT INTO step_records (id, user_id, date, count, start_time, end_time, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, NOW())
      ON CONFLICT (id) DO UPDATE SET
        count = EXCLUDED.count,
        start_time = EXCLUDED.start_time,
        end_time = EXCLUDED.end_time,
        updated_at = NOW()
    `, [record.id, userId, record.date, record.count, record.data.startTime, record.data.endTime]);
    
  } else if (record.type === 'exercise_session') {
    // Upsert exercise session
    await db.query(`
      INSERT INTO exercise_sessions (
        id, user_id, exercise_type, start_time, end_time, source, title, notes,
        energy_burned, total_distance, steps, elevation_gain, avg_heart_rate, updated_at
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
      ON CONFLICT (id) DO UPDATE SET
        exercise_type = EXCLUDED.exercise_type,
        start_time = EXCLUDED.start_time,
        end_time = EXCLUDED.end_time,
        source = EXCLUDED.source,
        title = EXCLUDED.title,
        notes = EXCLUDED.notes,
        energy_burned = EXCLUDED.energy_burned,
        total_distance = EXCLUDED.total_distance,
        steps = EXCLUDED.steps,
        elevation_gain = EXCLUDED.elevation_gain,
        avg_heart_rate = EXCLUDED.avg_heart_rate,
        updated_at = NOW()
    `, [
      record.id, userId, record.exerciseType, record.startTime, record.endTime,
      record.source, record.title, record.notes,
      record.data?.energyBurned, record.data?.totalDistance, record.data?.steps,
      record.data?.elevationGain, record.data?.avgHeartRate
    ]);
    
    // Handle laps - delete existing and insert new
    await db.query('DELETE FROM exercise_laps WHERE exercise_session_id = ?', [record.id]);
    
    if (record.data?.laps) {
      for (const lap of record.data.laps) {
        await db.query(`
          INSERT INTO exercise_laps (exercise_session_id, start_time, end_time)
          VALUES (?, ?, ?)
        `, [record.id, lap.startTime, lap.endTime]);
      }
    }
  }
}
```

## Edge Cases

| Scenario | Expected Behavior |
|----------|-------------------|
| `syncType: "incremental"` with empty `records` and empty `deletedRecordIds` | Success, no changes made |
| `syncType: "incremental"` with empty `records` and populated `deletedRecordIds` | Only perform deletions |
| `syncType: "incremental"` with populated `records` and empty `deletedRecordIds` | Only perform upserts |
| `syncType: "full"` with empty `records` | Delete all existing records |
| `deletedRecordIds` contains an ID that doesn't exist | Ignore silently, don't error |
| Duplicate IDs in `records` array | Process all, last one wins |
| Unknown `type` in records | Log warning, skip record |
| `null` values in exercise session metrics | Store as NULL in database |
| Exercise session with no laps | Store `NULL` or empty array for laps |

## Non-Goals (Out of Scope)

- User authentication/authorization (assumed to be handled separately)
- Rate limiting
- Data validation beyond basic type checking
- Heart rate samples (Phase 2 feature)
- Heart rate zones calculation (Phase 2 feature)
- GPS/Exercise route data (future feature)
- Swimming-specific data (future feature)
- Batch processing or queuing (sync is synchronous)

## Success Metrics

1. Full sync correctly replaces all existing records
2. Incremental sync correctly upserts and deletes only specified records
3. No duplicate records after multiple syncs
4. Deleted records are properly removed from the database
5. Exercise sessions with all metrics are stored correctly
6. Null metrics are handled gracefully
7. Response includes accurate `recordCount`

## Testing Scenarios

### Test 1: First Sync (Full) - Steps Only
```json
// Request
{ "syncType": "full", "records": [
  { "id": "a", "type": "steps", "date": "2025-12-17", "count": 5000, "data": { "startTime": "...", "endTime": "..." } }
], "deletedRecordIds": [] }

// Expected: Database contains only step record "a"
```

### Test 2: Full Sync - Mixed Records
```json
// Request
{ "syncType": "full", "records": [
  { "id": "s1", "type": "steps", ... },
  { "id": "e1", "type": "exercise_session", ... }
], "deletedRecordIds": [] }

// Expected: Database contains step "s1" and exercise session "e1"
```

### Test 3: Incremental Add Exercise Session
```json
// Existing: step "s1"
// Request
{ "syncType": "incremental", "records": [
  { "id": "e1", "type": "exercise_session", ... }
], "deletedRecordIds": [] }

// Expected: Database contains step "s1" and exercise session "e1"
```

### Test 4: Incremental Update Exercise Session
```json
// Existing: exercise session "e1" with energyBurned=300
// Request
{ "syncType": "incremental", "records": [
  { "id": "e1", "type": "exercise_session", "data": { "energyBurned": 350, ... } }
], "deletedRecordIds": [] }

// Expected: Exercise session "e1" now has energyBurned=350
```

### Test 5: Incremental Delete
```json
// Existing: step "s1", exercise session "e1"
// Request
{ "syncType": "incremental", "records": [], "deletedRecordIds": ["e1"] }

// Expected: Database contains only step "s1"
```

### Test 6: Exercise Session with Laps
```json
// Request
{ "syncType": "full", "records": [
  { "id": "e1", "type": "exercise_session", "data": {
    "laps": [
      { "startTime": "...", "endTime": "..." },
      { "startTime": "...", "endTime": "..." }
    ]
  }}
], "deletedRecordIds": [] }

// Expected: Exercise session "e1" with 2 laps stored
```

### Test 7: Exercise Session with Null Metrics
```json
// Request
{ "syncType": "full", "records": [
  { "id": "e1", "type": "exercise_session", "data": {
    "energyBurned": null,
    "totalDistance": 5000.0,
    "steps": null,
    "elevationGain": null,
    "avgHeartRate": 140.0,
    "laps": null
  }}
], "deletedRecordIds": [] }

// Expected: Exercise session stored with null values preserved
```

## Future Considerations (Phase 2)

The backend should be designed to easily accommodate future additions:

1. **Heart Rate Samples**: Raw HR data will be sent in Phase 2 for zone calculations
2. **Heart Rate Zones**: Backend will calculate time spent in each HR zone
3. **Additional Record Types**: Sleep sessions, daily metrics, etc.
4. **GPS Route Data**: Large payload handling for exercise routes

## Open Questions

None - requirements are complete for Phase 1.


