package com.circadia.healthsync.data.model

/**
 * Data class for cached sync data.
 * Holds the last synced step information for display while fetching new data.
 */
data class CachedSyncData(
    /** Total step count from the last sync */
    val totalStepCount: Long,
    /** Number of records synced */
    val recordCount: Int,
    /** ISO 8601 timestamp of when the data was synced */
    val syncTimestamp: String,
    /** Formatted timestamp for display (e.g., "Dec 15, 2025 at 2:30 PM") */
    val formattedTimestamp: String
)

