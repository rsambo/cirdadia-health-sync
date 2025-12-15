# Roadmap:Automatic & Reliable Sync Sprints


## Sprint 1 — Make sync feel automatic

- [ ] Auto-update on open

    - When the user opens the app, steps update without them doing anything

- [ ] Incremental updates

    - Only fetch what's new since the last time (so it's fast)

- [ ] Daily totals

    - Show and send one clear "steps today / steps per day" number (not a bunch of raw entries)

- [ ] Web dashboard stays current

    - The newest daily totals appear on the web side

- [ ] Basic status

    - Show "Last updated at …" and "Upload status: OK / Pending / Error"


## Sprint 2 — Make it dependable when things go wrong

- [ ] Offline-safe

    - If there's no internet, remember the updates and send them later automatically

- [ ] No duplicates

    - If the same day's steps get sent twice, the web dashboard doesn't double-count

- [ ] Catch-up mode

    - If the user hasn't opened the app in a while, it catches up cleanly

- [ ] Clear failure + retry

    - If syncing fails, show a simple message and a "Retry" action



## Sprint 3 — Keep it updated in the background (optional) + expand metrics

- [ ] Optional background updating

    - If the device supports it and the user allows it, keep data fresh even when the app isn't opened

- [ ] More daily tiles

    - Add 2–3 additional daily metrics (e.g., sleep, resting heart rate, active calories)

- [ ] Simple settings

    - Let the user control: sync behavior (on-open only vs background if available) and how far back to catch up