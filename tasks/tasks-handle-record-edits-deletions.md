# Tasks: Handling Record Edits and Deletions

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/data/HealthConnectManager.kt` - Add Changes API methods for token and change detection
- `app/src/main/java/com/circadia/healthsync/data/local/SyncPreferences.kt` - Add storage for changes token
- `app/src/main/java/com/circadia/healthsync/data/SyncRepository.kt` - Update sync logic to use Changes API
- `app/src/main/java/com/circadia/healthsync/data/model/StepRecord.kt` - Update to include record ID
- `app/src/main/java/com/circadia/healthsync/data/model/SyncRequest.kt` - Update payload with syncType and deletedRecordIds
- `app/src/main/java/com/circadia/healthsync/data/model/ChangeResult.kt` - New data class for change detection results
- `app/src/main/java/com/circadia/healthsync/data/api/HealthSyncApi.kt` - Update API interface if needed
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncViewModel.kt` - Add notification event handling
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncScreen.kt` - Add Snackbar for change notifications

### Notes

- Health Connect Changes API requires `getChangesToken()` and `getChanges()` methods
- Token must persist across app sessions via SharedPreferences
- Fallback to full 7-day sync if token is expired or invalid
- Notifications should be specific: "Steps updated", "Steps deleted", etc.

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/handle-record-edits-deletions`

- [x] 1.0 Update data models for record IDs
  - [x] 1.1 Update `StepRecord` data class to include `id: String` field for Health Connect record ID
  - [x] 1.2 Update `SyncRequest` to add `syncType: String` field ("full" or "incremental")
  - [x] 1.3 Update `SyncRequest` to add `deletedRecordIds: List<String>` field
  - [x] 1.4 Create `ChangeResult` data class to hold upserted records and deleted record IDs

- [x] 2.0 Update SyncPreferences for token storage
  - [x] 2.1 Add `saveChangesToken(token: String)` method
  - [x] 2.2 Add `getChangesToken(): String?` method
  - [x] 2.3 Add `clearChangesToken()` method for fallback scenarios

- [x] 3.0 Implement Changes API in HealthConnectManager
  - [x] 3.1 Add `getChangesToken(): String` method to request a new changes token
  - [x] 3.2 Add `getChanges(token: String): ChangeResult` method to detect changes since token
  - [x] 3.3 Process `UpsertionChange` to extract record data with IDs
  - [x] 3.4 Process `DeletionChange` to collect deleted record IDs
  - [x] 3.5 Handle token expiration by throwing a specific exception
  - [x] 3.6 Add logging for change detection debugging

- [x] 4.0 Update SyncRepository for change-based sync
  - [x] 4.1 Modify `performSync()` to check for existing changes token
  - [x] 4.2 If token exists: use Changes API for incremental sync
  - [x] 4.3 If no token or token expired: perform full 7-day sync, then get new token
  - [x] 4.4 Extract record IDs from Health Connect records (`metadata.id`)
  - [x] 4.5 Build sync request with records, deleted IDs, and sync type
  - [x] 4.6 Save new changes token after successful sync
  - [x] 4.7 Implement fallback: clear token and retry with full sync on Changes API failure

- [x] 5.0 Update API payload and client
  - [x] 5.1 Ensure `StepRecord` serializes with `id` field
  - [x] 5.2 Ensure `SyncRequest` serializes `syncType` and `deletedRecordIds`
  - [x] 5.3 Verify API endpoint accepts the updated payload structure

- [x] 6.0 Implement user notifications
  - [x] 6.1 Create `SyncEvent` sealed class for notification events (StepsUpdated, StepsDeleted, etc.)
  - [x] 6.2 Add `SharedFlow<SyncEvent>` in SyncViewModel for one-time events
  - [x] 6.3 Emit specific events based on change types detected
  - [x] 6.4 Add Snackbar in SyncScreen to observe and display events
  - [x] 6.5 Format notification messages: "Steps updated", "Steps deleted", "[N] records updated"

- [x] 7.0 Test and verify
  - [x] 7.1 Test first sync: full sync performed, token stored
  - [x] 7.2 Test subsequent sync with no changes: incremental sync, no notification
  - [x] 7.3 Test sync after record edit: change detected, "Steps updated" notification
  - [x] 7.4 Test sync after record deletion: deletion detected, "Steps deleted" notification
  - [x] 7.5 Test token expiration: fallback to full sync, new token stored
  - [x] 7.6 Test multiple changes: "[N] records updated" notification
  - [x] 7.7 Verify no duplicate records on backend after multiple syncs

