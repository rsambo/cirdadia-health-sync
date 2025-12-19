package com.circadia.healthsync.data.model

/**
 * Result of change detection from Health Connect Changes API.
 * Contains upserted records (new/updated) and deleted record IDs.
 */
data class ChangeResult(
    /** Step records that were added or updated */
    val upsertedStepRecords: List<StepRecord>,

    /** Exercise session records that were added or updated */
    val upsertedExerciseSessionRecords: List<ExerciseSessionRecord>,

    /** IDs of records that were deleted (both steps and exercise sessions) */
    val deletedRecordIds: List<String>,

    /** New changes token to use for next sync */
    val nextToken: String,

    /** Whether there are more changes to fetch */
    val hasMoreChanges: Boolean = false
) {
    /** All upserted records combined */
    val allUpsertedRecords: List<SyncRecord>
        get() = upsertedStepRecords + upsertedExerciseSessionRecords

    /** Total number of changes detected */
    val totalChanges: Int
        get() = upsertedStepRecords.size + upsertedExerciseSessionRecords.size + deletedRecordIds.size

    /** Whether any changes were detected */
    val hasChanges: Boolean
        get() = totalChanges > 0
}

