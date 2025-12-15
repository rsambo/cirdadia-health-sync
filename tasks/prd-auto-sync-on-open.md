# PRD: Auto-Sync on App Open

## Introduction/Overview

Currently, users must manually tap the "Sync Data" button to fetch their step data from Health Connect and send it to the backend. This feature will automatically trigger a sync when the user opens the app, making the experience seamless and ensuring the dashboard always shows up-to-date information.

The app will show the last synced data immediately on open, then fetch new data in the background and update the UI when it arrives. If the sync fails, the user will see an error message with a retry option.

## Goals

1. Eliminate the need for users to manually trigger sync on every app open
2. Ensure the web dashboard receives the latest step data without user intervention
3. Provide immediate feedback by showing cached/last synced data while fetching updates
4. Handle sync failures gracefully with clear error messaging and retry capability

## User Stories

1. **As a user**, I want my step data to sync automatically when I open the app, so I don't have to remember to tap a button.

2. **As a user**, I want to see my last synced data immediately when opening the app, so I'm not staring at a blank screen while waiting.

3. **As a user**, I want to know when new data has been fetched, so I can trust the information is current.

4. **As a user**, I want to see a clear error message if sync fails, so I know something went wrong and can retry.

## Functional Requirements

1. **Auto-trigger sync on app open**
   - The app must automatically initiate a sync operation when the app is opened (comes to foreground)
   - Sync should trigger on every app open (no cooldown/throttling)

2. **Show last synced data immediately**
   - On app open, display the most recent synced data from the previous session
   - This data should appear instantly before any network request completes

3. **Loading indicator during sync**
   - While fetching new data, show a loading indicator
   - For this PRD, showing a spinner or "Syncing..." text is sufficient

4. **Update UI when new data arrives**
   - When the sync completes successfully, update the displayed data
   - Show "Last updated: just now" or timestamp to confirm refresh

5. **Incremental sync**
   - Only fetch step data since the last successful sync
   - Store the last sync timestamp locally to determine the fetch range

6. **Error handling with retry**
   - If sync fails, display an error message to the user
   - Provide a "Retry" button to manually trigger another sync attempt
   - The error state should not prevent viewing previously synced data

7. **Persist last synced data**
   - Store the last synced step data locally (SharedPreferences or local database)
   - Store the timestamp of the last successful sync

## Non-Goals (Out of Scope)

- Background sync when app is closed (future sprint)
- Sync throttling or cooldown periods
- WiFi-only sync options
- User-configurable sync settings
- Syncing data types other than steps
- Offline queue for failed syncs (future sprint)
- Visual diff showing step count changes (see PRD: Auto-Sync UI Polish)
- Relative/absolute timestamp formatting (see PRD: Auto-Sync UI Polish)
- Replacing Sync button with refresh icon (see PRD: Auto-Sync UI Polish)

## Design Considerations

### UI States

1. **Initial Load (first ever open)**
   - Show "Syncing..." with loading indicator
   - No cached data to display

2. **Normal Open (has cached data)**
   - Immediately show last synced data with "Last synced: [timestamp]"
   - Show loading indicator while fetching new data
   - Update data and timestamp when sync completes

3. **Sync Success**
   - Update displayed step data
   - Update "Last synced: [timestamp]" to current time

4. **Sync Error**
   - Keep showing last synced data
   - Show error message (e.g., "Sync failed")
   - Keep existing "Sync Data" button visible for retry

### UI Changes for Testing

- Add "Last synced: [timestamp]" text to the status card
- Show cached step count on app open (before sync completes)
- Display loading indicator during auto-sync
- Keep existing "Sync Data" button for manual retry (will be replaced in UI Polish PRD)

## Technical Considerations

1. **Lifecycle handling**
   - Trigger sync in `onResume()` or equivalent Compose lifecycle event
   - Ensure sync doesn't trigger multiple times on configuration changes

2. **Local storage**
   - Use SharedPreferences for storing:
     - Last sync timestamp (ISO 8601 format)
     - Cached step data (as JSON)

3. **Incremental fetch**
   - Modify `HealthConnectManager.readStepsForLast7Days()` to accept a `since` parameter
   - Calculate date range: from last sync timestamp to now
   - On first sync (no timestamp), fetch last 7 days

4. **State management**
   - Add new UI state to `SyncUiState`:
     - `Refreshing(cachedData)` - has cached data, currently fetching new data
   - Modify existing states to include cached data when available

5. **Dependencies**
   - No new external dependencies required
   - May use Gson (already included) for JSON serialization of cached data

## Success Metrics

1. **Primary**: Users no longer need to tap "Sync" manually - data updates automatically on app open
2. **Technical**: Sync completes within 3 seconds on normal network conditions
3. **Reliability**: Error states are clearly communicated and recoverable via retry

## Testing Checklist

- [ ] App automatically syncs when opened
- [ ] Cached data appears immediately on app open (after first sync)
- [ ] Loading indicator shows during sync
- [ ] "Last synced" timestamp updates after successful sync
- [ ] Error message shows if sync fails
- [ ] Manual "Sync Data" button still works for retry
- [ ] Killing and reopening app shows cached data instantly
