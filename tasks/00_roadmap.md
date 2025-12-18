# Roadmap:Automatic & Reliable Sync Sprints


## Sprint 1 — Make sync feel automatic

- [x] Auto-update on open

    - When the user opens the app, steps update without them doing anything
    - [x] on-open-syncing
    - [x] ui-polish

- [x] Incremental updates

    - Only fetch what's new since the last time (so it's fast)

- [x] Running daily total & status

    - In mobile app show running total of steps for the current day.
    - Show "Last updated at …" and "Upload status: OK / Pending / Error"



## Sprint 2 - Add more metrics

- [ ] Handling editing and deletion of records

- [ ] More daily tiles

    - Add 2–3 additional daily metrics (e.g., sleep, resting heart rate, active calories)

### Available Health Connect Metrics

#### Physical Activity (Focus for Sprint 2)
| Metric | Health Connect Record | Description | Unit | Priority |
|--------|----------------------|-------------|------|----------|
| ✅ Steps | `StepsRecord` | Daily step count | count | Done |
| Distance | `DistanceRecord` | Total distance traveled | meters | High |
| Active Calories | `ActiveCaloriesBurnedRecord` | Calories burned through activity | kcal | High |
| Total Calories | `TotalCaloriesBurnedRecord` | Total calories (active + basal) | kcal | Medium |
| Floors Climbed | `FloorsClimbedRecord` | Elevation gained in floors | floors | Medium |
| **Exercise Sessions** | `ExerciseSessionRecord` | Workout details (see below) | various | High |

**Exercise Session Data:**
| Field | Description | Unit |
|-------|-------------|------|
| Exercise Type | Type of workout (running, cycling, etc.) | enum |
| Start Time | When workout started | datetime |
| End Time | When workout ended | datetime |
| Duration | Total workout time | minutes |
| Energy Burned | Calories burned during workout | kcal |
| Total Distance | Total distance covered | meters |
| Steps | Steps during workout | count |
| Elevation Gain | Total elevation climbed | meters |
| **Heart Rate Samples** | Raw HR readings during session | list |
| ↳ Timestamp | When HR was recorded | datetime |
| ↳ BPM | Heart rate value | bpm |
| Pace Laps/Splits | Per-lap or per-km/mi pace splits | min/km |
| Exercise Segments | Laps or intervals within session | list |
| Exercise Route | GPS coordinates (if outdoor) | lat/lng |
| Notes/Title | User notes or workout title | text |
| **Swimming-Specific** | | |
| ↳ Laps | Number of pool laps completed | count |
| ↳ Stroke Count | Total strokes taken | count |
| ↳ Stroke Type | Freestyle, backstroke, breaststroke, butterfly, mixed | enum |

**Exercise Session Types (Supported):**

Running, Walking, Hiking, Biking, Mountain Biking, Swimming (Pool), Swimming (Open Water), Strength Training, Weightlifting, HIIT, Elliptical, Rowing Machine, Stair Climbing, Treadmill, Yoga, Pilates, Jump Rope, Kayaking, Skiing, Skiing Cross-Country, Tennis, Golf, Rock Climbing, Workout







#### Sleep (Future Sprint)
| Metric | Health Connect Record | Description | Unit | Priority |
|--------|----------------------|-------------|------|----------|
| Sleep Duration | `SleepSessionRecord` | Total time asleep | hours | High |
| Sleep Stages | `SleepSessionRecord.Stage` | Light, deep, REM, awake | minutes | Medium |
| Time in Bed | `SleepSessionRecord` | Total time in bed | hours | Low |

#### Vitals (Future Sprint)
| Metric | Health Connect Record | Description | Unit | Priority |
|--------|----------------------|-------------|------|----------|
| Resting Heart Rate | `RestingHeartRateRecord` | Average resting HR | bpm | High |
| Heart Rate | `HeartRateRecord` | Heart rate samples | bpm | Medium |
| Heart Rate Variability | `HeartRateVariabilityRmssdRecord` | HRV (stress indicator) | ms | Medium |
| Blood Oxygen (SpO2) | `OxygenSaturationRecord` | Oxygen saturation | % | Medium |
| **VO2 Max** | `Vo2MaxRecord` | Cardio fitness level | mL/kg/min | Medium |
| Respiratory Rate | `RespiratoryRateRecord` | Breaths per minute | breaths/min | Low |
| Body Temperature | `BodyTemperatureRecord` | Body temperature | °C | Low |
| Blood Pressure | `BloodPressureRecord` | Systolic/diastolic | mmHg | Low |
| Blood Glucose | `BloodGlucoseRecord` | Blood sugar levels | mg/dL | Low |





### Sprint 2 Implementation Plan

**Phase 1: Daily Activity Metrics**
- [ ] Active Calories (daily total)
- [ ] Distance (daily total)
- [ ] Total Calories (daily total)

**Phase 2: Exercise Sessions**
- [ ] Exercise Sessions (list of workouts)
  - [ ] Exercise Type
  - [ ] Start/End Time
  - [ ] Duration
  - [ ] Energy burned (calories)
  - [ ] Total Distance
  - [ ] Steps
  - [ ] Elevation gain
  - [ ] Heart rate samples (raw data)
  - [ ] Pace laps/splits
  - [ ] Exercise segments
  - [ ] Exercise route (GPS)
  - [ ] Notes/Title
  - [ ] Swimming: Laps, Stroke Count, Stroke Type




## Sprint 3— Make it dependable when things go wrong

- [ ] Offline-safe

    - If there's no internet, remember the updates and send them later automatically

- [ ] No duplicates

    - If the same day's steps get sent twice, the web dashboard doesn't double-count

- [ ] Catch-up mode

    - If the user hasn't opened the app in a while, it catches up cleanly

- [ ] Clear failure + retry

    - If syncing fails, show a simple message and a "Retry" action



## Sprint 4 — Keep it updated in the background (optional) + expand metrics

- [ ] Optional background updating

    - If the device supports it and the user allows it, keep data fresh even when the app isn't opened


- [ ] Simple settings

    - Let the user control: sync behavior (on-open only vs background if available) and how far back to catch up