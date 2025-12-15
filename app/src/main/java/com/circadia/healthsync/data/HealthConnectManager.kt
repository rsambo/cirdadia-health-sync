package com.circadia.healthsync.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Manager class for interacting with Health Connect API.
 * Handles permission checking, step data reading, and aggregation.
 */
class HealthConnectManager(private val context: Context) {

    private var healthConnectClient: HealthConnectClient? = null

    companion object {
        private const val TAG = "HealthConnectManager"

        // Required permissions for this app
        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class)
        )
    }

    /**
     * Check if Health Connect is available on this device.
     */
    fun isAvailable(): Boolean {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
                true
            }
            else -> false
        }
    }

    /**
     * Check the availability status of Health Connect on this device.
     */
    fun getHealthConnectAvailability(): HealthConnectAvailability {
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> {
                healthConnectClient = HealthConnectClient.getOrCreate(context)
                HealthConnectAvailability.Available
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectAvailability.NotInstalled
            }
            else -> HealthConnectAvailability.NotSupported
        }
    }

    /**
     * Check if all required permissions are already granted.
     */
    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        val granted = client.permissionController.getGrantedPermissions()
        return PERMISSIONS.all { it in granted }
    }

    /**
     * Alias for hasPermissions for backwards compatibility.
     */
    suspend fun hasAllPermissions(): Boolean = hasPermissions()

    /**
     * Create an intent to request Health Connect permissions.
     */
    fun createPermissionRequestIntent(): Intent {
        return PermissionController.createRequestPermissionResultContract()
            .createIntent(context, PERMISSIONS)
    }

    /**
     * Read daily step counts for the last 7 days.
     * Returns a map of date string (ISO format) to step count.
     */
    suspend fun readDailySteps(): Map<String, Long> {
        val client = healthConnectClient ?: return emptyMap()

        val endTime = Instant.now()
        val startTime = endTime.minus(7, ChronoUnit.DAYS)

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        return try {
            val response = client.readRecords(request)
            aggregateDailyStepsToMap(response.records)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Read step count records for the last 7 days.
     * Returns a list of daily step counts aggregated by date.
     */
    suspend fun readStepsForLast7Days(): List<DailyStepCount> {
        val client = healthConnectClient ?: throw IllegalStateException("Health Connect not available")

        val endTime = Instant.now()
        val startTime = endTime.minus(7, ChronoUnit.DAYS)

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        val response = client.readRecords(request)
        return aggregateDailySteps(response.records)
    }

    /**
     * Read step count records since a given timestamp (incremental sync).
     * If no timestamp is provided, falls back to last 7 days.
     * @param since The timestamp to fetch steps from, or null for default 7 days
     * @return A list of daily step counts aggregated by date
     */
    suspend fun readStepsSince(since: Instant? = null): List<DailyStepCount> {
        val client = healthConnectClient ?: throw IllegalStateException("Health Connect not available")

        val endTime = Instant.now()
        val startTime = since ?: endTime.minus(7, ChronoUnit.DAYS)

        Log.d(TAG, "readStepsSince: Querying from $startTime to $endTime")
        Log.d(TAG, "readStepsSince: since parameter was ${if (since != null) "provided" else "null (using 7 day default)"}")

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        val response = client.readRecords(request)
        Log.d(TAG, "readStepsSince: Got ${response.records.size} raw step records from Health Connect")

        // Log each raw record for debugging
        response.records.forEach { record ->
            Log.d(TAG, "  Record: ${record.count} steps from ${record.startTime} to ${record.endTime}")
        }

        val aggregated = aggregateDailySteps(response.records)
        Log.d(TAG, "readStepsSince: Aggregated into ${aggregated.size} daily totals")
        aggregated.forEach { daily ->
            Log.d(TAG, "  ${daily.date}: ${daily.count} steps")
        }

        return aggregated
    }

    /**
     * Aggregate raw step records into a map of date strings to totals.
     */
    private fun aggregateDailyStepsToMap(records: List<StepsRecord>): Map<String, Long> {
        val zoneId = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        return records
            .groupBy { record ->
                LocalDate.ofInstant(record.startTime, zoneId)
            }
            .mapValues { (_, dayRecords) ->
                dayRecords.sumOf { it.count }
            }
            .mapKeys { (date, _) ->
                date.format(formatter)
            }
    }

    /**
     * Aggregate raw step records into daily totals.
     */
    private fun aggregateDailySteps(records: List<StepsRecord>): List<DailyStepCount> {
        val zoneId = ZoneId.systemDefault()

        return records
            .groupBy { record ->
                record.startTime.atZone(zoneId).toLocalDate()
            }
            .map { (date, dayRecords) ->
                DailyStepCount(
                    date = date,
                    count = dayRecords.sumOf { it.count }
                )
            }
            .sortedByDescending { it.date }
    }

    /**
     * Get the intent to install Health Connect from Play Store.
     */
    fun getHealthConnectInstallIntent(): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
    }
}

/**
 * Represents the availability status of Health Connect.
 */
sealed class HealthConnectAvailability {
    object Available : HealthConnectAvailability()
    object NotInstalled : HealthConnectAvailability()
    object NotSupported : HealthConnectAvailability()
}

/**
 * Data class representing daily step count.
 */
data class DailyStepCount(
    val date: LocalDate,
    val count: Long
)
