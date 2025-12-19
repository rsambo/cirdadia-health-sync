# PRD: Exercise Sessions Sync (Phase 1 - Aggregated Data)

## Introduction/Overview

The CircadiaHealthSync app currently syncs step data from Health Connect to a backend server. This feature extends the app to also sync **Exercise Sessions** (workouts) from Health Connect.

Phase 1 focuses on syncing exercise session metadata and aggregated metrics using Health Connect's Aggregation API. This keeps payloads small and avoids syncing large amounts of raw data. The app acts as a "dumb pipe" - it reads data from Health Connect and sends it to the backend without performing calculations.

## Goals

1. Sync exercise sessions (workouts) from Health Connect to the backend
2. Include aggregated metrics for each session (calories, distance, steps, elevation, avg HR)
3. Use the existing Changes API pattern for incremental sync (detect new/edited/deleted sessions)
4. Keep payloads small by using Health Connect Aggregation API instead of raw samples
5. Support editing and deletion of exercise sessions (same pattern as steps)

## User Stories

1. **As a user**, I want my workouts from Fitbit/Garmin/Google Fit to automatically sync to my backend when I open the app.

2. **As a user**, I want my workout data to include details like duration, calories burned, distance, and average heart rate.

3. **As a user**, I want edits to my workouts (e.g., corrected duration in Fitbit) to be reflected in the backend.

4. **As a user**, I want deleted workouts to be removed from the backend.

## Functional Requirements

### 1. Data Model - ExerciseSessionRecord

The app must sync the following fields for each exercise session:

| # | Field | Source | Required | Notes |
|---|-------|--------|----------|-------|
| 1.1 | id | `metadata.id` | Yes | Health Connect record ID (for upsert/delete) |
| 1.2 | type | `"exercise_session"` | Yes | Record type identifier |
| 1.3 | exerciseType | `ExerciseSessionRecord.exerciseType` | Yes | Enum (e.g., RUNNING, WALKING, etc.) |
| 1.4 | startTime | `ExerciseSessionRecord.startTime` | Yes | ISO 8601 timestamp |
| 1.5 | endTime | `ExerciseSessionRecord.endTime` | Yes | ISO 8601 timestamp |
| 1.6 | source | `metadata.dataOrigin.packageName` | Yes | App that created the record |
| 1.7 | title | `ExerciseSessionRecord.title` | No | Optional user title |
| 1.8 | notes | `ExerciseSessionRecord.notes` | No | Optional user notes |

### 2. Aggregated Metrics

For each exercise session, the app must query Health Connect's Aggregation API for the session's time range and include:

| # | Field | Aggregation Source | Unit | Notes |
|---|-------|-------------------|------|-------|
| 2.1 | energyBurned | `TotalCaloriesBurnedRecord` (SUM) | kcal | Total calories for session |
| 2.2 | totalDistance | `DistanceRecord` (SUM) | meters | Total distance for session |
| 2.3 | steps | `StepsRecord` (SUM) | count | Total steps for session |
| 2.4 | elevationGain | `ElevationGainedRecord` (SUM) | meters | Total elevation for session |
| 2.5 | avgHeartRate | `HeartRateRecord` (AVG) | bpm | Average HR for session |

### 3. Pace Laps

| # | Requirement |
|---|-------------|
| 3.1 | If `ExerciseLap` records exist for the session, include them in the payload |
| 3.2 | Each lap should include: startTime, endTime, and any available metrics |

### 4. Sync Behavior

| # | Requirement |
|---|-------------|
| 4.1 | Use Health Connect Changes API to detect new/modified/deleted exercise sessions |
| 4.2 | Include `ExerciseSessionRecord` in the `ChangesTokenRequest` record types |
| 4.3 | On incremental sync: send only changed sessions with `syncType: "incremental"` |
| 4.4 | On full sync: send all sessions from last 7 days with `syncType: "full"` |
| 4.5 | Include deleted session IDs in `deletedRecordIds` array |

### 5. Permissions

| # | Requirement |
|---|-------------|
| 5.1 | Request read permission for `ExerciseSessionRecord` |
| 5.2 | Request read permissions for aggregation records: `TotalCaloriesBurnedRecord`, `DistanceRecord`, `StepsRecord`, `ElevationGainedRecord`, `HeartRateRecord` |
| 5.3 | Handle permission denial gracefully (skip exercise sessions if not granted) |

### 6. API Payload Structure

The exercise session records should be included in the existing sync request:

```json
{
  "syncType": "full" | "incremental",
  "records": [
    {
      "id": "step-record-id",
      "type": "steps",
      "date": "2025-12-17",
      "count": 5000,
      "data": { "startTime": "...", "endTime": "..." }
    },
    {
      "id": "exercise-session-id",
      "type": "exercise_session",
      "exerciseType": "RUNNING",
      "startTime": "2025-12-17T08:00:00Z",
      "endTime": "2025-12-17T08:45:00Z",
      "source": "com.fitbit.FitbitMobile",
      "title": "Morning Run",
      "notes": null,
      "data": {
        "energyBurned": 350.5,
        "totalDistance": 5200.0,
        "steps": 4800,
        "elevationGain": 45.0,
        "avgHeartRate": 145.0,
        "laps": [
          {
            "startTime": "2025-12-17T08:00:00Z",
            "endTime": "2025-12-17T08:10:00Z"
          }
        ]
      }
    }
  ],
  "deletedRecordIds": ["deleted-step-id", "deleted-exercise-id"]
}
```

### 7. Supported Exercise Types

The app must map Health Connect exercise type enums to string values:

| Health Connect Enum | String Value |
|--------------------|--------------|
| `EXERCISE_TYPE_RUNNING` | `"RUNNING"` |
| `EXERCISE_TYPE_WALKING` | `"WALKING"` |
| `EXERCISE_TYPE_HIKING` | `"HIKING"` |
| `EXERCISE_TYPE_BIKING` | `"BIKING"` |
| `EXERCISE_TYPE_BIKING_MOUNTAIN` | `"MOUNTAIN_BIKING"` |
| `EXERCISE_TYPE_SWIMMING_POOL` | `"SWIMMING_POOL"` |
| `EXERCISE_TYPE_SWIMMING_OPEN_WATER` | `"SWIMMING_OPEN_WATER"` |
| `EXERCISE_TYPE_STRENGTH_TRAINING` | `"STRENGTH_TRAINING"` |
| `EXERCISE_TYPE_WEIGHTLIFTING` | `"WEIGHTLIFTING"` |
| `EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING` | `"HIIT"` |
| `EXERCISE_TYPE_ELLIPTICAL` | `"ELLIPTICAL"` |
| `EXERCISE_TYPE_ROWING_MACHINE` | `"ROWING_MACHINE"` |
| `EXERCISE_TYPE_STAIR_CLIMBING` | `"STAIR_CLIMBING"` |
| `EXERCISE_TYPE_RUNNING_TREADMILL` | `"TREADMILL"` |
| `EXERCISE_TYPE_YOGA` | `"YOGA"` |
| `EXERCISE_TYPE_PILATES` | `"PILATES"` |
| `EXERCISE_TYPE_JUMPING_ROPE` | `"JUMP_ROPE"` |
| `EXERCISE_TYPE_KAYAKING` | `"KAYAKING"` |
| `EXERCISE_TYPE_SKIING` | `"SKIING"` |
| `EXERCISE_TYPE_SKIING_CROSS_COUNTRY` | `"SKIING_CROSS_COUNTRY"` |
| `EXERCISE_TYPE_TENNIS` | `"TENNIS"` |
| `EXERCISE_TYPE_GOLF` | `"GOLF"` |
| `EXERCISE_TYPE_ROCK_CLIMBING` | `"ROCK_CLIMBING"` |
| `EXERCISE_TYPE_OTHER_WORKOUT` | `"WORKOUT"` |
| (any other type) | `"OTHER"` |

## Non-Goals (Out of Scope)

1. **Heart Rate Zones** - Phase 2 will add raw HR samples for zone calculation
2. **GPS/Exercise Route** - Large payload, deferred to future sprint
3. **Exercise Segments** - Complex, deferred to future sprint
4. **Swimming-specific data** (laps, strokes, stroke type) - Deferred
5. **Calculations in the app** - Backend handles any derived calculations (e.g., average pace)
6. **UI changes** - This is sync-only, no new UI for exercise sessions

## Technical Considerations

### Health Connect Aggregation API

To get aggregated metrics for an exercise session's time range:

```kotlin
val request = AggregateRequest(
    metrics = setOf(
        TotalCaloriesBurnedRecord.ENERGY_TOTAL,
        DistanceRecord.DISTANCE_TOTAL,
        StepsRecord.COUNT_TOTAL,
        ElevationGainedRecord.ELEVATION_GAINED_TOTAL,
        HeartRateRecord.BPM_AVG
    ),
    timeRangeFilter = TimeRangeFilter.between(
        session.startTime,
        session.endTime
    )
)
val result = healthConnectClient.aggregate(request)
```

### Changes API Extension

The existing Changes API implementation needs to be extended:
- Add `ExerciseSessionRecord::class` to `ChangesTokenRequest`
- Process `UpsertionChange` for exercise sessions
- Process `DeletionChange` for exercise sessions

### Null Handling for Aggregations

Some aggregations may return null if no data exists for that metric during the session. The app should:
- Send `null` for missing metrics (not 0)
- Backend should handle null values appropriately

## Success Metrics

1. Exercise sessions from Health Connect are successfully synced to backend
2. Aggregated metrics are included for each session
3. Edits to sessions are detected and synced
4. Deletions are detected and synced
5. Payload size remains small (no raw HR samples in Phase 1)

## Open Questions

None - requirements are complete for Phase 1.

