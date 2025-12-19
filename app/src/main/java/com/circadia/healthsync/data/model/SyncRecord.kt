package com.circadia.healthsync.data.model

/**
 * Marker interface for all sync record types.
 * Both StepRecord and ExerciseSessionRecord implement this interface
 * to allow them to be mixed in the same records list.
 */
interface SyncRecord {
    val id: String
    val type: String
}

