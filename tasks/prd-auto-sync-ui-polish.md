# PRD: Auto-Sync UI Polish

## Introduction/Overview

This PRD builds on the "Auto-Sync on App Open" feature to add UI polish and refinements. It focuses on improving the visual feedback when data changes, formatting timestamps in a user-friendly way, and replacing the prominent sync button with a subtle refresh icon.

**Prerequisite**: PRD "Auto-Sync on App Open" must be completed first.

## Goals

1. Provide clear visual feedback when step data changes after a sync
2. Display timestamps in a human-friendly format (relative vs absolute)
3. De-emphasize manual sync action since auto-sync is now the primary flow

## User Stories

1. **As a user**, I want to see how my step count changed since the last sync, so I can quickly understand if new data was recorded.

2. **As a user**, I want to see timestamps in a friendly format like "2 min ago", so I don't have to do mental math.

3. **As a user**, I want a clean UI where the refresh action is available but not prominent, since sync happens automatically.

## Functional Requirements

1. **Visual diff on data change**
   - When step count changes after a sync, display the difference (e.g., "+222 steps")
   - The diff should remain visible (not fade away) until the next sync
   - Show positive changes as "+X steps" and negative as "-X steps"
   - If no change, don't show a diff

2. **Smart timestamp formatting**
   - If last sync was within 5 days: show relative time
     - "just now" (< 1 minute)
     - "2 min ago" (< 60 minutes)
     - "3 hours ago" (< 24 hours)
     - "Yesterday" (1 day ago)
     - "2 days ago", "3 days ago", etc.
   - If last sync was more than 5 days ago: show absolute time
     - Format: "Dec 10, 10:45 AM"

3. **Replace Sync button with refresh icon**
   - Remove the large "Sync Data" button
   - Add a subtle refresh icon (top-right corner or in the app bar)
   - Icon triggers manual sync when tapped
   - Icon should animate (spin) while sync is in progress

## Non-Goals (Out of Scope)

- Pull-to-refresh gesture
- Sync history or log
- Customizable timestamp format
- Sound or haptic feedback on sync

## Design Considerations

### Visual Diff Display

```
┌─────────────────────────────┐
│          Status             │
│            ✓                │
│     Sync successful!        │
│      Sent 3 records         │
│       +222 steps            │  ← Diff displayed here
│   Last synced: 2 min ago    │
└─────────────────────────────┘
```

- Diff text should be smaller than the main count
- Use a neutral color (gray) or subtle green for positive, red for negative
- Position below the record count

### Refresh Icon Placement

Option A: Top-right corner of the screen (app bar area)
Option B: Inside the status card

Recommendation: **Option A** - keeps the status card clean and follows common patterns

### Refresh Icon States

1. **Idle**: Static refresh icon (↻)
2. **Syncing**: Spinning animation
3. **Error**: Icon with error indicator (optional: red dot badge)

## Technical Considerations

1. **Diff calculation**
   - Store previous step count before sync
   - After sync, calculate: `newCount - previousCount`
   - Only show diff if there's a change

2. **Timestamp formatting**
   - Create a utility function `formatRelativeTimestamp(timestamp: Instant): String`
   - Use `java.time` APIs for date calculations
   - Update displayed timestamp periodically (optional: every minute) or on recomposition

3. **Refresh icon**
   - Use Material Icons: `Icons.Default.Refresh`
   - Implement rotation animation using `Modifier.rotate()` with `InfiniteTransition`
   - Place in `TopAppBar` or as a floating action element

4. **State changes**
   - Add `stepDiff: Int?` to success state
   - Add `lastSyncTimestamp: Instant` to relevant states

## Success Metrics

1. **Clarity**: Users can immediately see if their step count changed
2. **Usability**: Timestamps are easily understood without calculation
3. **Clean UI**: Manual sync option exists but doesn't dominate the screen

## Testing Checklist

- [ ] Step diff shows correctly when count increases
- [ ] Step diff shows correctly when count decreases  
- [ ] No diff shown when count unchanged
- [ ] Timestamp shows "just now" immediately after sync
- [ ] Timestamp shows "X min ago" format within 5 days
- [ ] Timestamp shows absolute format after 5 days
- [ ] Refresh icon is visible and tappable
- [ ] Refresh icon spins during sync
- [ ] Sync triggers correctly when refresh icon tapped

