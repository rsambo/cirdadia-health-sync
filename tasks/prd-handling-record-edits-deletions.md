# PRD: Handling Editing and Deletion of Records

## Introduction/Overview

Currently, the app syncs health data from Health Connect to the backend, but doesn't handle cases where records are edited or deleted in source apps (Google Fit, Garmin, Fitbit, etc.). When a user edits a workout or deletes step data in their fitness app, those changes sync to Health Connect but our app doesn't detect or propagate them.

This feature adds the ability to:
1. Detect when records have been edited or deleted in Health Connect using the Changes API
2. Sync those changes to the backend using record IDs
3. Show a specific notification/toast when data has been updated externally

## Goals

1. Ensure data consistency between Health Connect and the backend
2. Support record-level sync using Health Connect record IDs for precise tracking
3. Handle edits and deletions for all metric types (Steps, Exercise Sessions, etc.)
4. Notify users with specific messages when their data has been updated from external sources

## User Stories

1. **As a user**, I want my step count to update correctly when I edit it in Google Fit, so my dashboard shows accurate data.

2. **As a user**, I want deleted workouts to be removed from my dashboard when I delete them in my fitness app.

3. **As a user**, I want to see a specific notification when my data has been updated (e.g., "Steps updated" or "Workout deleted"), so I know exactly what changed.

## Functional Requirements

### 1. Record ID Tracking
- 1.1. The app must extract the unique Health Connect record ID (`metadata.id`) for each record
- 1.2. The app must send record IDs to the backend with each sync request
- 1.3. Record IDs must be included for all metric types: Steps, Distance, Calories, Exercise Sessions, etc.

### 2. Change Detection (Health Connect Changes API)
- 2.1. The app must use Health Connect's Changes API to detect changes since last sync
- 2.2. The app must store and manage the changes token between syncs
- 2.3. The app must handle token expiration by falling back to full 7-day sync
- 2.4. The app must categorize changes as: Upsert (new/updated) or Deletion
- 2.5. Change detection must cover all synced record types (Steps, etc.)

### 3. Sync Protocol
- 3.1. For **new/updated records (Upsert)**: Send full record data with record ID to backend
- 3.2. For **deleted records**: Send list of deleted record IDs to backend
- 3.3. The backend will handle deduplication and updates based on record ID
- 3.4. Sync request must include a flag indicating if this is a full sync or incremental (changes-based)

### 4. User Notification
- 4.1. Show a toast/snackbar when data has been updated from external changes
- 4.2. Message must be specific to what changed:
  - "Steps updated" - when step records are modified
  - "Steps deleted" - when step records are removed
  - "Workout updated" - when exercise sessions are modified
  - "Workout deleted" - when exercise sessions are removed
  - "[N] records updated" - when multiple record types change
- 4.3. Notification should only appear when external changes are detected, not on normal syncs

### 5. Data Model Updates
- 5.1. Update sync request payload to include record IDs
- 5.2. Add deleted record IDs to sync request payload
- 5.3. Store changes token in SharedPreferences for persistence across app restarts

## Non-Goals (Out of Scope)

- Editing or deleting records from within our app (changes originate from Health Connect only)
- Conflict resolution (backend is source of truth for deduplication)
- Real-time change detection (only checked on app open/manual sync)
- Changes beyond 7 days (matches current sync window)

## Technical Considerations

### Health Connect Changes API

Health Connect provides a Changes API for efficient change tracking:

```kotlin
// Get changes token for record types we care about
val changesToken = healthConnectClient.getChangesToken(
    ChangesTokenRequest(recordTypes = setOf(StepsRecord::class))
)

// Later, get changes since that token
val changesResponse = healthConnectClient.getChanges(changesToken)

// Process changes
changesResponse.changes.forEach { change ->
    when (change) {
        is UpsertionChange -> {
            // Record was added or updated
            val record = change.record
            val recordId = record.metadata.id
        }
        is DeletionChange -> {
            // Record was deleted
            val deletedRecordId = change.recordId
        }
    }
}

// Store new token for next sync
val newToken = changesResponse.nextChangesToken
```

### Token Management
- Store token in `SyncPreferences` alongside last sync timestamp
- On first launch or token expiration: perform full 7-day sync, then get fresh token
- On subsequent syncs: use Changes API with stored token

### Deleted Records Handling
- Changes API returns `DeletionChange` with the deleted record's ID
- Collect all deleted record IDs during change processing
- Send to backend in `deletedRecordIds` array
- Backend removes records matching those IDs

### API Payload Structure
```json
{
  "syncType": "incremental",
  "records": [
    {
      "id": "health-connect-record-id-123",
      "type": "steps",
      "date": "2025-12-15",
      "count": 5432,
      "startTime": "2025-12-15T08:00:00Z",
      "endTime": "2025-12-15T09:00:00Z"
    }
  ],
  "deletedRecordIds": [
    "health-connect-record-id-456",
    "health-connect-record-id-789"
  ]
}
```

### Fallback Strategy
If Changes API fails (token expired, API error):
1. Log the error
2. Clear stored token
3. Perform full 7-day sync
4. Get new changes token for future syncs

## Success Metrics

1. Edited records in Health Connect are reflected on the backend within one sync cycle
2. Deleted records in Health Connect are removed from the backend within one sync cycle
3. User sees specific notification toast when external changes are detected
4. No duplicate records on backend after multiple syncs
5. Changes API token is properly managed across app sessions

## Open Questions

None - all questions resolved.

