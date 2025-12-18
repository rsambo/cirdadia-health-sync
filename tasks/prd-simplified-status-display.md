# PRD: Simplified Status Display

## Introduction/Overview

The current status display in the app shows too many elements that are confusing to users:
- Today's steps (large number)
- "steps today" label
- 7-day total
- Step diff since last sync (+/- X)
- Last updated timestamp
- Upload status (OK/Pending/Error)

**Problem with step count display:** The step count shown reflects what's currently in Health Connect, which doesn't always match other apps (Google Fit, Fitbit) due to delayed syncing between apps. This creates confusion when users see a number that doesn't match their fitness app.

Users only need to know:
1. **Whether their data is up-to-date (synced) or not**
2. **When the last successful sync occurred**

This feature simplifies the status display to focus on sync status only.

## Goals

1. Simplify the UI to show only sync status information
2. Make sync status intuitive - users should immediately know if their data is current
3. Remove confusing data displays (step counts, totals, diffs)
4. Show when the last successful sync occurred
5. Follow patterns from other fitness apps that hide sync complexity

## User Stories

1. **As a user**, I want to know if my data is up-to-date with a simple visual indicator, so I don't have to think about sync mechanics.

2. **As a user**, I want to see when my data was last synced, so I know how fresh the data is.

3. **As a user**, I want to see when something is wrong with syncing, so I know to take action (retry, check connection, etc.).

## Functional Requirements

### 1. Primary Display - Sync Status
- 1.1. Remove today's step count display
- 1.2. Remove "steps today" label
- 1.3. Remove the 7-day total display
- 1.4. Remove the step diff (+/- since last sync) display

### 2. Sync Status - Simple Visual Indicator
- 2.1. Show a simple status indicator with three states:

| State | Visual | Meaning |
|-------|--------|---------|
| **Up to date** | Green checkmark icon | Data is synced, no pending changes |
| **Syncing** | Spinning indicator | Currently syncing with Health Connect/backend |
| **Needs attention** | Orange/red warning icon | Sync failed or couldn't complete |

- 2.2. Show minimal text below the icon:
  - Up to date: "Up to date"
  - Syncing: "Syncing..."
  - Needs attention: "Tap to retry" or brief error hint

- 2.3. Remove "Upload status: OK" verbose text - replace with simple status text

### 3. Last Sync Timestamp
- 3.1. Show "Last synced: [relative time]" below the status indicator
- 3.2. Use relative time format (e.g., "2 min ago", "1 hour ago", "Yesterday")
- 3.3. Always show this timestamp regardless of sync state

### 4. Status Card Simplification
- 4.1. Remove the "Status" header from the card
- 4.2. Center the content vertically in the available space
- 4.3. Keep the card background for visual containment

### 5. Error Handling
- 5.1. When sync fails, show the warning icon with "Needs attention" or error hint
- 5.2. Tapping the status area or refresh icon should retry sync
- 5.3. Still show the last successful sync timestamp even when there's an error

## Non-Goals (Out of Scope)

- Displaying step counts or any health metrics
- Historical data views (7-day trends, charts)
- Daily step goals or progress rings
- Detailed sync logs or error messages
- Storing sync record IDs locally
- Extending query window beyond 7 days

## Design Considerations

### Current Layout (Remove)
```
┌─────────────────────────────────┐
│           Status                │  ← Remove header
│                                 │
│           5,432                 │  ← Remove
│        steps today              │  ← Remove
│                                 │
│   12,345 steps (7 days)         │  ← Remove
│   +222 since last sync          │  ← Remove
│                                 │
│   Last updated: 2 min ago       │  ← Keep (rename to "Last synced")
│   ● Upload status: OK           │  ← Replace with icon + simple text
└─────────────────────────────────┘
```

### New Layout (Simplified)
```
┌─────────────────────────────────┐
│                                 │
│             ✓                   │  ← Status icon (large)
│        Up to date               │  ← Status text
│                                 │
│     Last synced: 2 min ago      │  ← Timestamp
│                                 │
└─────────────────────────────────┘
```

### Status States

**Up to date:**
```
┌─────────────────────────────────┐
│                                 │
│             ✓                   │  Green checkmark
│        Up to date               │
│                                 │
│     Last synced: 2 min ago      │
│                                 │
└─────────────────────────────────┘
```

**Syncing:**
```
┌─────────────────────────────────┐
│                                 │
│             ↻                   │  Spinning indicator
│          Syncing...             │
│                                 │
│     Last synced: 5 min ago      │
│                                 │
└─────────────────────────────────┘
```

**Needs attention:**
```
┌─────────────────────────────────┐
│                                 │
│             ⚠                   │  Orange/red warning
│       Needs attention           │
│        Tap to retry             │
│                                 │
│     Last synced: 1 hour ago     │
│                                 │
└─────────────────────────────────┘
```

### Status Icons
- **Up to date**: ✓ (green checkmark, ~48dp)
- **Syncing**: ↻ (spinning/animated indicator, ~48dp)
- **Needs attention**: ⚠ (orange/red warning, ~48dp)

## Technical Considerations

1. **Determining "Up to Date" Status**:
   - Up to date = Last sync succeeded AND Changes API reports no new changes
   - Syncing = Currently fetching from Health Connect or sending to backend
   - Needs attention = Last sync failed OR network error OR backend error

2. **Keep existing sync logic** - only change the UI display
3. **Remove unused UI elements** - step counts, totals, diffs, verbose status text
4. **Keep TimeFormatters.kt** - still needed for relative timestamp formatting

## Success Metrics

1. Users can understand their sync status at a glance (no confusion)
2. Reduced visual clutter - only essential sync info on screen
3. No confusion about step counts not matching other apps
4. Clear indication of when data was last synced

## Open Questions

None - requirements are complete.

