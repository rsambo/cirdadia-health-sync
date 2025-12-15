package com.circadia.healthsync.data

import android.util.Log
import com.circadia.healthsync.data.api.ApiClient
import com.circadia.healthsync.data.local.SyncPreferences
import com.circadia.healthsync.data.model.CachedSyncData
import com.circadia.healthsync.data.model.StepRecord
import com.circadia.healthsync.data.model.SyncRequest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "SyncRepository"

/**
 * Result class for sync operations.
 */
sealed class SyncResult {
    /** Sync completed successfully */
    data class Success(
        val cachedData: CachedSyncData
    ) : SyncResult()

    /** Sync failed with an error */
    data class Error(
        val message: String,
        val cachedData: CachedSyncData? = null
    ) : SyncResult()
}

/**
 * Repository that coordinates sync operations.
 * Combines HealthConnectManager, API client, and SyncPreferences.
 */
class SyncRepository(
    private val healthConnectManager: HealthConnectManager,
    private val syncPreferences: SyncPreferences
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

    /**
     * Get cached sync data immediately from SharedPreferences.
     * @return The cached sync data, or null if no data has been synced yet
     */
    fun getCachedData(): CachedSyncData? {
        return syncPreferences.getCachedSyncData()
    }

    /**
     * Perform a sync operation.
     * Fetches steps incrementally since last sync, sends to API, and caches result.
     * @return SyncResult indicating success or failure
     */
    suspend fun performSync(): SyncResult {
        Log.d(TAG, "performSync: Starting sync operation")
        val cachedData = getCachedData()
        Log.d(TAG, "performSync: Cached data exists: ${cachedData != null}")

        return try {
            // Step 1: Get last sync timestamp for incremental fetch
            val lastSyncTimestamp = syncPreferences.getLastSyncTimestamp()
            Log.d(TAG, "performSync: Last sync timestamp: $lastSyncTimestamp")

            // Step 2: Read steps from Health Connect (incremental or full 7 days)
            Log.d(TAG, "performSync: Reading steps from Health Connect...")
            val dailySteps = healthConnectManager.readStepsSince(lastSyncTimestamp)
            Log.d(TAG, "performSync: Got ${dailySteps.size} daily step records")

            if (dailySteps.isEmpty()) {
                Log.d(TAG, "performSync: No new step data since last sync")
                // If we have cached data and just no NEW steps, that's not an error
                // Return success with existing cached data
                if (cachedData != null) {
                    Log.d(TAG, "performSync: Returning cached data (no new steps to sync)")
                    return SyncResult.Success(cachedData = cachedData)
                } else {
                    // First sync with no data - this is actually an error
                    Log.w(TAG, "performSync: First sync but no step data found in Health Connect")
                    return SyncResult.Error(
                        message = "No step data found in Health Connect",
                        cachedData = null
                    )
                }
            }

            // Step 3: Transform to API format
            val stepRecords = transformToApiFormat(dailySteps)
            val request = SyncRequest(records = stepRecords)
            Log.d(TAG, "performSync: Sending ${stepRecords.size} records to API")

            // Step 4: Send to backend
            val response = ApiClient.getApi().syncHealthData(request)
            Log.d(TAG, "performSync: API response code: ${response.code()}")

            // Step 5: Handle response
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val now = Instant.now()
                val totalStepCount = dailySteps.sumOf { it.count }
                Log.d(TAG, "performSync: Success! Total steps: $totalStepCount, records: ${body.recordCount}")

                // Calculate step diff from previous sync
                val previousStepCount = cachedData?.totalStepCount
                val stepDiff = if (previousStepCount != null) {
                    val diff = totalStepCount - previousStepCount
                    if (diff != 0L) diff else null  // Only store non-zero diff
                } else null
                Log.d(TAG, "performSync: Previous count: $previousStepCount, Diff: $stepDiff")

                // Create cached data with diff
                val newCachedData = CachedSyncData(
                    totalStepCount = totalStepCount,
                    recordCount = body.recordCount,
                    syncTimestamp = now.toString(),
                    formattedTimestamp = formatTimestamp(now),
                    previousStepCount = previousStepCount,
                    stepDiff = stepDiff
                )

                // Step 6: Store sync timestamp and cached data
                syncPreferences.saveLastSyncTimestamp(now)
                syncPreferences.saveCachedSyncData(newCachedData)
                Log.d(TAG, "performSync: Cached data saved")

                SyncResult.Success(cachedData = newCachedData)
            } else {
                val errorMessage = response.body()?.message
                    ?: "Server error: ${response.code()}"
                Log.e(TAG, "performSync: API error - $errorMessage")
                SyncResult.Error(
                    message = errorMessage,
                    cachedData = cachedData
                )
            }

        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "performSync: Network error - UnknownHostException", e)
            SyncResult.Error(
                message = "Network error: Cannot reach server",
                cachedData = cachedData
            )
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "performSync: Network error - SocketTimeoutException", e)
            SyncResult.Error(
                message = "Network error: Connection timed out",
                cachedData = cachedData
            )
        } catch (e: Exception) {
            SyncResult.Error(
                message = "Error: ${e.message ?: "Unknown error"}",
                cachedData = cachedData
            )
        }
    }

    /**
     * Transform daily step counts to API record format.
     */
    private fun transformToApiFormat(dailySteps: List<DailyStepCount>): List<StepRecord> {
        return dailySteps.map { daily ->
            StepRecord(
                type = "steps",
                date = daily.date.format(dateFormatter),
                count = daily.count
            )
        }
    }

    /**
     * Format an Instant for display.
     */
    private fun formatTimestamp(instant: Instant): String {
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        return localDateTime.format(displayFormatter)
    }
}

