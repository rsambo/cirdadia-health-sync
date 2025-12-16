package com.circadia.healthsync.data.model

import androidx.compose.ui.graphics.Color

/**
 * Enum representing the upload/sync status.
 */
enum class UploadStatus(
    val displayText: String,
    val color: Color
) {
    /** Last sync completed successfully */
    OK("OK", Color(0xFF4CAF50)),

    /** Sync is in progress */
    PENDING("Pending", Color(0xFFFFC107)),

    /** Last sync failed */
    ERROR("Error", Color(0xFFF44336))
}

