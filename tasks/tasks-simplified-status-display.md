# Tasks: Simplified Status Display

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncScreen.kt` - Main UI composable, contains StatusCard and CachedDataDisplay to be simplified
- `app/src/main/java/com/circadia/healthsync/data/model/CachedSyncData.kt` - Data class with step counts to be cleaned up
- `app/src/main/java/com/circadia/healthsync/data/model/UploadStatus.kt` - Upload status enum, may be renamed/simplified
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncViewModel.kt` - ViewModel managing UI state
- `app/src/main/java/com/circadia/healthsync/ui/utils/TimeFormatters.kt` - Keep for relative timestamp formatting

### Notes

- Keep existing sync logic unchanged - only modify UI display
- The "Last synced" timestamp should persist even during error states
- TimeFormatters.kt is still needed for relative time formatting

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/simplified-status-display`

- [x] 1.0 Simplify CachedSyncData model
  - [x] 1.1 Remove `totalStepCount` field from CachedSyncData
  - [x] 1.2 Remove `todayStepCount` field from CachedSyncData
  - [x] 1.3 Remove `recordCount` field from CachedSyncData
  - [x] 1.4 Remove `previousStepCount` field from CachedSyncData
  - [x] 1.5 Remove `stepDiff` field from CachedSyncData
  - [x] 1.6 Keep only `syncTimestamp` and `formattedTimestamp` fields
  - [x] 1.7 Update any code that creates CachedSyncData instances

- [x] 2.0 Simplify UploadStatus enum
  - [x] 2.1 Rename UploadStatus to SyncStatus (or keep as is)
  - [x] 2.2 Update status values: OK → UpToDate, PENDING → Syncing, ERROR → NeedsAttention
  - [x] 2.3 Update display text: "Up to date", "Syncing...", "Needs attention"
  - [x] 2.4 Update colors if needed (green, neutral, orange/red)

- [x] 3.0 Update StatusCard composable
  - [x] 3.1 Remove "Status" header text from the card
  - [x] 3.2 Center content vertically in the card
  - [x] 3.3 Simplify the when(uiState) branches to use new simplified display

- [x] 4.0 Create new SimplifiedStatusDisplay composable
  - [x] 4.1 Create composable that shows large status icon (48dp)
  - [x] 4.2 Add status text below icon ("Up to date", "Syncing...", "Needs attention")
  - [x] 4.3 Add "Last synced: [relative time]" text below status
  - [x] 4.4 Add spinning animation for syncing state
  - [x] 4.5 Use green checkmark icon for up-to-date state
  - [x] 4.6 Use warning icon for needs-attention state
  - [x] 4.7 Make status area tappable to retry sync on error

- [x] 5.0 Remove old CachedDataDisplay composable
  - [x] 5.1 Delete the CachedDataDisplay composable function
  - [x] 5.2 Delete the formatWithCommas extension function (no longer needed)
  - [x] 5.3 Remove any unused imports

- [x] 6.0 Update SyncRepository
  - [x] 6.1 Update CachedSyncData creation in performIncrementalSync to only include timestamp fields
  - [x] 6.2 Update CachedSyncData creation in performFullSync to only include timestamp fields

- [ ] 7.0 Test and verify (manual testing required)
  - [ ] 7.1 Test "Up to date" state displays correctly with green checkmark
  - [ ] 7.2 Test "Syncing" state displays correctly with spinning indicator
  - [ ] 7.3 Test "Needs attention" state displays correctly with warning icon
  - [ ] 7.4 Test "Last synced" timestamp displays correctly in all states
  - [ ] 7.5 Test tapping status area retries sync on error state
  - [ ] 7.6 Verify no step count data is displayed anywhere

