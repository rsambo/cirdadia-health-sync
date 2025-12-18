package com.circadia.healthsync.ui.sync

/**
 * Sealed class representing one-time sync events for UI notifications.
 */
sealed class SyncEvent {
    /** Steps were added or updated */
    data class StepsUpdated(val count: Int) : SyncEvent()

    /** Steps were deleted */
    data class StepsDeleted(val count: Int) : SyncEvent()

    /** Multiple types of records were changed */
    data class MultipleChanges(val total: Int) : SyncEvent()

    /**
     * Get the display message for this event.
     */
    fun getMessage(): String {
        return when (this) {
            is StepsUpdated -> if (count == 1) "Steps updated" else "$count step records updated"
            is StepsDeleted -> if (count == 1) "Steps deleted" else "$count step records deleted"
            is MultipleChanges -> "$total records updated"
        }
    }
}

