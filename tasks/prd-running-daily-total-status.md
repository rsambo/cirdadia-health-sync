# PRD: Running Daily Total & Status

## Introduction/Overview

Currently, the app displays a 7-day total step count after syncing. Users want to see their **current day's step progress** prominently, along with clear feedback about the sync status.

This feature adds:
1. A running total of today's steps (displayed prominently)
2. A "Last updated at..." timestamp showing when data was last synced
3. An upload status indicator (OK / Pending / Error)

## Goals

1. Show users their current day's step count as the primary metric
2. Provide clear visibility into sync health with an upload status indicator
3. Display when data was last updated so users know how fresh their data is
4. Maintain visibility of the 7-day total as secondary information

## User Stories

1. **As a user**, I want to see my step count for today so I can track my daily progress at a glance.

2. **As a user**, I want to see when my data was last updated so I know how current the information is.

3. **As a user**, I want to see a clear status indicator so I know if my data is syncing properly (OK), waiting to upload (Pending), or has a problem (Error).

## Functional Requirements

1. **Today's Steps Display**
   - 1.1. The app must display today's step count prominently (larger font, primary position)
   - 1.2. The app must display the 7-day total as secondary information (smaller font, below today's count)
   - 1.3. Today's steps must update each time the app syncs with Health Connect

2. **Last Updated Timestamp**
   - 2.1. The app must display "Last updated at [time]" in the status card
   - 2.2. The timestamp must use relative format (e.g., "2 min ago", "1 hour ago")
   - 2.3. The timestamp must update after each successful sync

3. **Upload Status Indicator**
   - 3.1. The app must display an upload status with one of three states:
     - **OK** - Last sync completed successfully
     - **Pending** - Sync is in progress
     - **Error** - Last sync failed
   - 3.2. The status indicator must use appropriate colors:
     - OK: Green
     - Pending: Yellow/Amber
     - Error: Red
   - 3.3. The status must update in real-time as sync state changes

4. **Data Model Updates**
   - 4.1. The cached sync data must include today's step count separately from the 7-day total
   - 4.2. Health Connect queries must aggregate today's steps independently

## Non-Goals (Out of Scope)

- Hourly breakdown of steps within the day
- Historical daily totals (only today and 7-day total)
- Push notifications about sync status
- Detailed error messages (just show "Error" status for now; detailed errors are Sprint 2)

## Design Considerations

**Status Card Layout:**
```
┌─────────────────────────────────┐
│           Status                │
│                                 │
│        5,432 steps              │  ← Today's steps (large, bold)
│          today                  │
│                                 │
│     12,345 steps (7 days)       │  ← 7-day total (smaller, secondary)
│                                 │
│   Last updated: 2 min ago       │
│   Upload status: ● OK           │  ← Green dot + "OK"
└─────────────────────────────────┘
```

**Status Colors:**
- OK: Green (#4CAF50)
- Pending: Amber (#FFC107)
- Error: Red (#F44336)

## Technical Considerations

1. **HealthConnectManager** - Add method to read today's steps specifically
2. **CachedSyncData** - Add `todayStepCount: Long` field
3. **SyncRepository** - Calculate and store today's step count during sync
4. **SyncUiState** - Include upload status enum (OK, Pending, Error)
5. **SyncScreen** - Update UI layout to show new information

## Success Metrics

1. Today's step count is displayed and matches Health Connect data
2. Upload status accurately reflects sync state (OK/Pending/Error)
3. "Last updated" timestamp updates after each sync
4. UI remains responsive during sync operations

## Open Questions

None at this time.

