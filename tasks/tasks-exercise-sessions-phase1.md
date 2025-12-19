# Tasks: Exercise Sessions Sync (Phase 1 - Aggregated Data)

## Relevant Files

- `app/src/main/java/com/circadia/healthsync/data/model/ExerciseSessionRecord.kt` - New data class for exercise session payloads
- `app/src/main/java/com/circadia/healthsync/data/model/ExerciseLap.kt` - New data class for lap data
- `app/src/main/java/com/circadia/healthsync/data/model/ExerciseType.kt` - New enum/mapper for exercise types
- `app/src/main/java/com/circadia/healthsync/data/model/SyncRecord.kt` - New sealed class/interface for polymorphic records (steps + exercise sessions)
- `app/src/main/java/com/circadia/healthsync/data/model/SyncRequest.kt` - Update to handle mixed record types
- `app/src/main/java/com/circadia/healthsync/data/HealthConnectManager.kt` - Add exercise session reading and aggregation methods
- `app/src/main/java/com/circadia/healthsync/data/SyncRepository.kt` - Integrate exercise session sync
- `app/src/main/AndroidManifest.xml` - Add new Health Connect permissions

### Notes

- Exercise sessions use Health Connect's Aggregation API to get metrics like calories, distance, steps
- The Changes API must be extended to track both StepsRecord and ExerciseSessionRecord
- All record types go in the same `records` array with a `type` field to differentiate them
- Null values for aggregations should be sent as-is (not converted to 0)

## Instructions for Completing Tasks

**IMPORTANT:** As you complete each task, you must check it off in this markdown file by changing `- [ ]` to `- [x]`. This helps track progress and ensures you don't skip any steps.

Example:
- `- [ ] 1.1 Read file` → `- [x] 1.1 Read file` (after completing)

Update the file after completing each sub-task, not just after completing an entire parent task.

## Tasks

- [x] 0.0 Create feature branch
  - [x] 0.1 Create and checkout a new branch: `git checkout -b feature/exercise-sessions-phase1`

- [x] 1.0 Create ExerciseSession data models
  - [x] 1.1 Create `ExerciseType.kt` with mapping function from Health Connect enum to string
  - [x] 1.2 Create `ExerciseLap.kt` data class with startTime, endTime fields
  - [x] 1.3 Create `ExerciseSessionData.kt` data class for aggregated metrics (energyBurned, totalDistance, steps, elevationGain, avgHeartRate, laps)
  - [x] 1.4 Create `ExerciseSessionRecord.kt` data class with id, type, exerciseType, startTime, endTime, source, title, notes, data
  - [x] 1.5 Ensure all fields use proper Gson `@SerializedName` annotations

- [x] 2.0 Create polymorphic SyncRecord structure
  - [x] 2.1 Create `SyncRecord.kt` sealed interface or base class that both StepRecord and ExerciseSessionRecord implement
  - [x] 2.2 Update `StepRecord.kt` to implement/extend SyncRecord
  - [x] 2.3 Update `ExerciseSessionRecord.kt` to implement/extend SyncRecord
  - [x] 2.4 Update `SyncRequest.kt` to use `List<SyncRecord>` instead of `List<StepRecord>`

- [x] 3.0 Update Health Connect permissions
  - [x] 3.1 Add `ExerciseSessionRecord` read permission to HealthConnectManager.PERMISSIONS
  - [x] 3.2 Add `TotalCaloriesBurnedRecord` read permission
  - [x] 3.3 Add `DistanceRecord` read permission
  - [x] 3.4 Add `ElevationGainedRecord` read permission
  - [x] 3.5 Add `HeartRateRecord` read permission
  - [x] 3.6 Update AndroidManifest.xml with new permission declarations if needed

- [x] 4.0 Implement exercise session reading in HealthConnectManager
  - [x] 4.1 Create `readExerciseSessionsWithIds()` method to read all sessions from last 7 days
  - [x] 4.2 Create `aggregateMetricsForSession()` method to get calories, distance, steps, elevation, avgHR for a time range
  - [x] 4.3 Create `readLapsForSession()` method to get ExerciseLap records for a session
  - [x] 4.4 Create `buildExerciseSessionRecord()` method to combine session data with aggregated metrics and laps
  - [x] 4.5 Add logging for exercise session reading and aggregation

- [x] 5.0 Extend Changes API for exercise sessions
  - [x] 5.1 Add `ExerciseSessionRecord::class` to `ChangesTokenRequest` in `getChangesToken()`
  - [x] 5.2 Update `getChanges()` to process `UpsertionChange` for ExerciseSessionRecord
  - [x] 5.3 Update `getChanges()` to include exercise session deletions in deletedRecordIds
  - [x] 5.4 Update `ChangeResult` to hold both step records and exercise session records

- [x] 6.0 Update SyncRepository
  - [x] 6.1 Update `performFullSync()` to read and include exercise sessions
  - [x] 6.2 Update `performIncrementalSync()` to include changed exercise sessions
  - [x] 6.3 Combine step records and exercise session records into single records list
  - [x] 6.4 Ensure deletedRecordIds includes both step and exercise session deletions

- [x] 7.0 Test and verify (manual testing required)
  - [x] 7.1 Test full sync includes exercise sessions with aggregated metrics
  - [x] 7.2 Test incremental sync detects new exercise sessions
  - [x] 7.3 Test incremental sync detects edited exercise sessions
  - [x] 7.4 Test incremental sync detects deleted exercise sessions
  - [x] 7.5 Test aggregated metrics are correctly populated (or null if not available)
  - [x] 7.6 Test exercise type mapping works for all supported types
  - [x] 7.7 Verify payload structure matches PRD specification

