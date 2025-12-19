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


- [x] Handling editing and deletion of records

## Add more metrics

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
| Field | Source | Phase | Notes |
|-------|--------|-------|-------|
| id | `metadata.id` | 1 | Health Connect record ID |
| Exercise Type | `ExerciseSessionRecord.exerciseType` | 1 | Enum |
| Start Time | `ExerciseSessionRecord.startTime` | 1 | ISO 8601 |
| End Time | `ExerciseSessionRecord.endTime` | 1 | ISO 8601 |
| Source | `metadata.dataOrigin.packageName` | 1 | App that created record |
| Duration | Stored or calculated | 1 | Minutes |
| Title | `ExerciseSessionRecord.title` | 1 | Optional |
| Notes | `ExerciseSessionRecord.notes` | 1 | Optional |
| Energy Burned | Aggregation API (TotalCaloriesBurned) | 1 | kcal |
| Total Distance | Aggregation API (Distance) | 1 | meters |
| Steps | Aggregation API (Steps) | 1 | count |
| Elevation Gain | Aggregation API (ElevationGained) | 1 | meters |
| Average Heart Rate | Aggregation API (HeartRate AVG) | 1 | bpm |
| Pace Laps | `ExerciseLap` records | 1 | List of laps |
| **Heart Rate Samples** | `HeartRateRecord` (raw) | 2 | For zone calculation |
| ↳ Timestamp | Sample time | 2 | ISO 8601 |
| ↳ BPM | Heart rate value | 2 | bpm |

**Exercise Session Types (Supported):**

Running, Walking, Hiking, Biking, Mountain Biking, Swimming (Pool), Swimming (Open Water), Strength Training, Weightlifting, HIIT, Elliptical, Rowing Machine, Stair Climbing, Treadmill, Yoga, Pilates, Jump Rope, Kayaking, Skiing, Skiing Cross-Country, Tennis, Golf, Rock Climbing, Workout





### Sprint 2 Implementation Plan

**Phase 1: Exercise Sessions (Aggregated Data)**
Sync exercise sessions using Health Connect Aggregation API - no raw samples, small payloads.

- [ ] Exercise Sessions (list of workouts)
  - [ ] id (Health Connect record ID)
  - [ ] Exercise Type (enum)
  - [ ] Start Time / End Time
  - [ ] Source (app that created the record)
  - [ ] Duration (from record or endTime - startTime)
  - [ ] Title / Notes
  - [ ] **Aggregated metrics for session time range:**
    - [ ] Energy Burned (TotalCaloriesBurnedRecord aggregation)
    - [ ] Total Distance (DistanceRecord aggregation)
    - [ ] Steps (StepsRecord aggregation)
    - [ ] Elevation Gain (ElevationGainedRecord aggregation)
    - [ ] Average Heart Rate (HeartRateRecord AVG aggregation)
  - [ ] Pace Laps (ExerciseLap records - small, sync as-is)

**Phase 2: Heart Rate Samples (for Zone Calculation)**
Add raw HR samples so backend can calculate time in HR zones.

- [ ] Heart Rate Samples during exercise session
  - [ ] Timestamp
  - [ ] BPM
  - [ ] Backend calculates: time in light/moderate/vigorous/peak zones
  - [ ] Backend stores zone times, discards raw samples

**Phase 3: Daily Activity Metrics (if needed)**
- [ ] Active Calories (daily total, outside of exercise sessions)
- [ ] Distance (daily total, outside of exercise sessions)
- [ ] Total Calories (daily total)

**Deferred (Future Sprints):**
- Exercise Route (GPS) - large payloads
- Exercise Segments - complex
- Swimming-specific data (laps, strokes)




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