# Tasks: Auto-Sync on App Open

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/data/HealthConnectManager.kt` - Modify to support incremental sync with `since` parameter
- `app/src/main/java/com/circadia/healthsync/data/SyncRepository.kt` - New file to manage sync logic, caching, and persistence
- `app/src/main/java/com/circadia/healthsync/data/local/SyncPreferences.kt` - New file for SharedPreferences wrapper to store cached data and timestamps
- `app/src/main/java/com/circadia/healthsync/data/model/CachedSyncData.kt` - New data class for cached sync data
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncViewModel.kt` - Update to support auto-sync, cached data, and new UI states
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncUiState.kt` - New file to hold refactored UI state classes
- `app/src/main/java/com/circadia/healthsync/ui/sync/SyncScreen.kt` - Update UI to show cached data, loading indicator, and "Last synced" timestamp
- `app/src/main/java/com/circadia/healthsync/MainActivity.kt` - Trigger auto-sync on app resume

### Notes

- Use SharedPreferences for simple key-value storage (cached data as JSON, timestamps)
- Gson is already included for JSON serialization
- No new external dependencies required
- Test by killing app and reopening to verify cached data appears immediately

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [ ] 0.0 Create feature branch
  - [ ] 0.1 Create and checkout a new branch: `git checkout -b feature/auto-sync-on-open`

- [ ] 1.0 Implement local data persistence
  - [ ] 1.1 Create `SyncPreferences.kt` class with SharedPreferences wrapper
  - [ ] 1.2 Add method to save last sync timestamp (ISO 8601 format)
  - [ ] 1.3 Add method to get last sync timestamp
  - [ ] 1.4 Add method to save cached step data (JSON serialization)
  - [ ] 1.5 Add method to get cached step data
  - [ ] 1.6 Add method to clear all cached data (for testing/logout)

- [ ] 2.0 Implement incremental sync in HealthConnectManager
  - [ ] 2.1 Add `readStepsSince(since: Instant)` method to fetch steps from a given timestamp
  - [ ] 2.2 Modify method to fall back to 7 days if no `since` timestamp provided
  - [ ] 2.3 Ensure new method returns data in same format as existing method

- [ ] 3.0 Create SyncRepository to coordinate sync operations
  - [ ] 3.1 Create `SyncRepository.kt` class that combines HealthConnectManager, API client, and SyncPreferences
  - [ ] 3.2 Add method to get cached data (returns immediately from SharedPreferences)
  - [ ] 3.3 Add method to perform sync (incremental fetch → API call → cache result)
  - [ ] 3.4 Store sync timestamp and step data on successful sync
  - [ ] 3.5 Return appropriate result objects for success/failure states

- [ ] 4.0 Update UI state management
  - [ ] 4.1 Create `CachedSyncData` data class to hold cached step count, record count, and timestamp
  - [ ] 4.2 Add `Refreshing(cachedData: CachedSyncData)` state to SyncUiState
  - [ ] 4.3 Modify `Success` state to include `CachedSyncData`
  - [ ] 4.4 Modify `Error` state to optionally include `CachedSyncData` (to show last data on error)
  - [ ] 4.5 Add `Ready` state variant that can hold cached data

- [ ] 5.0 Update SyncViewModel for auto-sync
  - [ ] 5.1 Inject or create SyncRepository instance
  - [ ] 5.2 Load cached data on ViewModel init and emit to UI immediately
  - [ ] 5.3 Add `autoSync()` method that loads cache first, then fetches new data
  - [ ] 5.4 Update state transitions: Ready(cached) → Refreshing(cached) → Success/Error
  - [ ] 5.5 Ensure manual sync button still triggers full sync
  - [ ] 5.6 Handle first-time launch (no cached data) gracefully

- [ ] 6.0 Update SyncScreen UI
  - [ ] 6.1 Display cached step data immediately when available
  - [ ] 6.2 Show "Last synced: [timestamp]" text in status card
  - [ ] 6.3 Show loading indicator during sync (can overlay or be inline)
  - [ ] 6.4 Update displayed data when sync completes
  - [ ] 6.5 Show error message while still displaying cached data
  - [ ] 6.6 Keep "Sync Data" button functional for manual retry

- [ ] 7.0 Trigger auto-sync on app open
  - [ ] 7.1 Add lifecycle observer or LaunchedEffect to detect app resume
  - [ ] 7.2 Call `viewModel.autoSync()` when app comes to foreground
  - [ ] 7.3 Prevent multiple simultaneous syncs (debounce/guard)
  - [ ] 7.4 Handle configuration changes without re-triggering sync

- [ ] 8.0 Test and verify
  - [ ] 8.1 Test first launch: shows "Syncing..." with no cached data
  - [ ] 8.2 Test normal launch: cached data appears immediately
  - [ ] 8.3 Test sync success: data and timestamp update
  - [ ] 8.4 Test sync failure: error shows, cached data remains visible
  - [ ] 8.5 Test kill and reopen: cached data persists
  - [ ] 8.6 Test manual sync button still works
  - [ ] 8.7 Verify incremental sync only fetches new data

