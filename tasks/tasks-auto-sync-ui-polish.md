# Tasks: Auto-Sync UI Polish

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/data/model/CachedSyncData.kt` - Update to include previous step count for diff calculation
- `app/src/main/java/com/circadia/healthsync/data/SyncRepository.kt` - Update to calculate and store step diff
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncViewModel.kt` - Update UI states to include step diff
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncScreen.kt` - Update UI to show diff, format timestamps, add refresh icon
- `app/src/main/java/com/circadia/healthsync/ui/utils/TimeFormatters.kt` - New file for timestamp formatting utilities
- `app/src/main/java/com/circadia/healthsync/MainActivity.kt` - Add TopAppBar with refresh icon

### Notes

- This feature builds on the completed "Auto-Sync on App Open" feature
- Use Material3 components for consistency
- The refresh icon should replace the large "Sync Data" button
- Relative timestamps should update dynamically (handled via recomposition)

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/auto-sync-ui-polish`

- [x] 1.0 Add step diff calculation and storage
  - [x] 1.1 Update `CachedSyncData` to include `previousStepCount: Long?` field
  - [x] 1.2 Update `SyncRepository.performSync()` to calculate step diff
  - [x] 1.3 Store both new count and previous count in cached data
  - [x] 1.4 Calculate diff as `newCount - previousCount` when previous exists

- [x] 2.0 Create timestamp formatting utilities
  - [x] 2.1 Create `TimeFormatters.kt` utility file
  - [x] 2.2 Implement `formatRelativeTimestamp(timestamp: Instant): String` function
  - [x] 2.3 Add logic for "just now", "X min ago", "X hours ago" formats
  - [x] 2.4 Add logic for "Yesterday", "X days ago" formats
  - [x] 2.5 Add fallback to absolute format "MMM d, h:mm a" for dates > 5 days old
  - [x] 2.6 Add unit tests for timestamp formatting edge cases

- [x] 3.0 Update UI state to include step diff
  - [x] 3.1 Add `stepDiff: Long?` to `SyncUiState.Success` state
  - [x] 3.2 Update `SyncViewModel` to pass step diff from cached data to UI state
  - [x] 3.3 Ensure diff is preserved across state transitions

- [x] 4.0 Add refresh icon to app bar
  - [x] 4.1 Update `MainActivity` to add `TopAppBar` with refresh icon
  - [x] 4.2 Add `IconButton` with `Icons.Default.Refresh` in the app bar
  - [x] 4.3 Implement rotation animation for sync in progress state
  - [x] 4.4 Connect refresh icon tap to `viewModel.sync()` call
  - [x] 4.5 Conditionally show spinning animation when `uiState` is Syncing or Refreshing

- [x] 5.0 Update SyncScreen UI
  - [x] 5.1 Remove the large "Sync Data" button from SyncScreen
  - [x] 5.2 Update `CachedDataDisplay` to show step diff when available
  - [x] 5.3 Format step diff as "+X steps" (green) or "-X steps" (red)
  - [x] 5.4 Replace absolute timestamp with relative timestamp using `formatRelativeTimestamp()`
  - [x] 5.5 Update status card layout to accommodate diff display
  - [x] 5.6 Ensure diff only shows when there's an actual change (not zero)

- [x] 6.0 Test and verify
  - [x] 6.1 Test step diff shows correctly when count increases
  - [x] 6.2 Test step diff shows correctly when count decreases
  - [x] 6.3 Test no diff shown when count unchanged
  - [x] 6.4 Test timestamp shows "just now" immediately after sync
  - [x] 6.5 Test timestamp shows relative format within 5 days
  - [x] 6.6 Test timestamp shows absolute format after 5 days
  - [x] 6.7 Test refresh icon is visible and tappable
  - [x] 6.8 Test refresh icon spins during sync
  - [x] 6.9 Test sync triggers correctly when refresh icon tapped

