package com.circadia.healthsync.data.model

import androidx.compose.ui.graphics.Color

/**
 * Enum representing the sync status.
 */
enum class SyncStatus(
    val displayText: String,
    val color: Color
) {
    /** Last sync completed successfully, data is up to date */
    UP_TO_DATE("Up to date", Color(0xFF4CAF50)),

    /** Sync is in progress */
    SYNCING("Syncing...", Color(0xFF9E9E9E)),

    /** Last sync failed or needs attention */
    NEEDS_ATTENTION("Needs attention", Color(0xFFFF9800))
}

