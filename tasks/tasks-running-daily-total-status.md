# Tasks: Running Daily Total & Status

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/data/model/CachedSyncData.kt` - Add todayStepCount field
- `app/src/main/java/com/circadia/healthsync/data/model/UploadStatus.kt` - New enum for upload status (OK, Pending, Error)
- `app/src/main/java/com/circadia/healthsync/data/HealthConnectManager.kt` - Add method to read today's steps
- `app/src/main/java/com/circadia/healthsync/data/SyncRepository.kt` - Calculate and store today's step count
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncViewModel.kt` - Expose upload status to UI
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncScreen.kt` - Update status card UI with new layout

### Notes

- The "Last updated" timestamp already uses relative format from TimeFormatters.kt
- Upload status should be derived from SyncUiState (Success=OK, Syncing/Refreshing=Pending, Error=Error)
- Today's steps should be calculated by filtering daily steps to today's date

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/running-daily-total-status`

- [x] 1.0 Add today's steps to data model
  - [x] 1.1 Update `CachedSyncData` to add `todayStepCount: Long` field with default value of 0
  - [x] 1.2 Update `HealthConnectManager` to filter today's steps from daily step data
  - [x] 1.3 Update `SyncRepository.performSync()` to calculate and store today's step count

- [x] 2.0 Create upload status enum
  - [x] 2.1 Create `UploadStatus.kt` enum with OK, Pending, Error states
  - [x] 2.2 Add color property to each enum value (Green, Amber, Red)
  - [x] 2.3 Add display text property to each enum value ("OK", "Pending", "Error")

- [x] 3.0 Update UI to show daily total and upload status
  - [x] 3.1 Update `CachedDataDisplay` to show today's steps prominently (large, bold)
  - [x] 3.2 Add "today" label below today's step count
  - [x] 3.3 Show 7-day total as secondary info with "(7 days)" label
  - [x] 3.4 Add upload status indicator with colored dot and text
  - [x] 3.5 Derive upload status from SyncUiState (Success→OK, Syncing/Refreshing→Pending, Error→Error)
  - [x] 3.6 Ensure "Last updated" timestamp remains visible using relative format

- [x] 4.0 Test and verify
  - [x] 4.1 Test today's steps displays correctly
  - [x] 4.2 Test 7-day total displays as secondary info
  - [x] 4.3 Test upload status shows OK (green) after successful sync
  - [x] 4.4 Test upload status shows Pending (amber) during sync
  - [x] 4.5 Test upload status shows Error (red) after failed sync
  - [x] 4.6 Test "Last updated" shows relative timestamp

