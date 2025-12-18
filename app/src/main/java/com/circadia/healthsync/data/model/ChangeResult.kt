package com.circadia.healthsync.data.model

/**
 * Result of change detection from Health Connect Changes API.
 * Contains upserted records (new/updated) and deleted record IDs.
 */
data class ChangeResult(
    /** Records that were added or updated */
    val upsertedRecords: List<StepRecord>,

    /** IDs of records that were deleted */
    val deletedRecordIds: List<String>,

    /** New changes token to use for next sync */
    val nextToken: String,

    /** Whether there are more changes to fetch */
    val hasMoreChanges: Boolean = false
) {
    /** Total number of changes detected */
    val totalChanges: Int
        get() = upsertedRecords.size + deletedRecordIds.size

    /** Whether any changes were detected */
    val hasChanges: Boolean
        get() = totalChanges > 0
}

