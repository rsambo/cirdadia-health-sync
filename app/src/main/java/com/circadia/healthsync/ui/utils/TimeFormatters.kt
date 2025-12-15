package com.circadia.healthsync.ui.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Utility object for formatting timestamps in a user-friendly way.
 */
object TimeFormatters {

    private val absoluteFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    /**
     * Formats a timestamp as a relative time string.
     *
     * - "just now" (< 1 minute)
     * - "X min ago" (< 60 minutes)
     * - "X hours ago" (< 24 hours)
     * - "Yesterday" (1 day ago)
     * - "X days ago" (2-5 days ago)
     * - "MMM d, h:mm a" (> 5 days ago)
     *
     * @param timestamp The instant to format
     * @return A human-friendly relative time string
     */
    fun formatRelativeTimestamp(timestamp: Instant): String {
        val now = Instant.now()
        val zoneId = ZoneId.systemDefault()

        val minutesAgo = ChronoUnit.MINUTES.between(timestamp, now)
        val hoursAgo = ChronoUnit.HOURS.between(timestamp, now)
        val daysAgo = ChronoUnit.DAYS.between(timestamp, now)

        return when {
            minutesAgo < 1 -> "just now"
            minutesAgo < 60 -> "$minutesAgo min ago"
            hoursAgo < 24 -> "$hoursAgo hours ago"
            daysAgo == 1L -> "Yesterday"
            daysAgo <= 5 -> "$daysAgo days ago"
            else -> {
                // Absolute format for dates > 5 days old
                val localDateTime = timestamp.atZone(zoneId).toLocalDateTime()
                localDateTime.format(absoluteFormatter)
            }
        }
    }

    /**
     * Formats a timestamp string (ISO 8601) as a relative time string.
     *
     * @param isoTimestamp The ISO 8601 timestamp string
     * @return A human-friendly relative time string, or the original string if parsing fails
     */
    fun formatRelativeTimestamp(isoTimestamp: String): String {
        return try {
            val instant = Instant.parse(isoTimestamp)
            formatRelativeTimestamp(instant)
        } catch (e: Exception) {
            isoTimestamp
        }
    }
}

