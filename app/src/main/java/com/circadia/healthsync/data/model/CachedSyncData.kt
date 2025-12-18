package com.circadia.healthsync.data.model

/**
 * Data class for cached sync data.
 * Holds the timestamp of the last successful sync.
 */
data class CachedSyncData(
    /** ISO 8601 timestamp of when the data was synced */
    val syncTimestamp: String,
    /** Formatted timestamp for display (e.g., "Dec 15, 2025 at 2:30 PM") */
    val formattedTimestamp: String
)

