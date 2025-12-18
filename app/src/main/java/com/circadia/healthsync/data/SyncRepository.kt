package com.circadia.healthsync.data

import android.util.Log
import com.circadia.healthsync.data.api.ApiClient
import com.circadia.healthsync.data.local.SyncPreferences
import com.circadia.healthsync.data.model.CachedSyncData
import com.circadia.healthsync.data.model.StepRecord
import com.circadia.healthsync.data.model.StepRecordData
import com.circadia.healthsync.data.model.SyncRequest
import com.google.gson.Gson
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "SyncRepository"
private val gson = Gson()

/**
 * Describes what type of changes occurred during sync.
 */
data class SyncChanges(
    val stepsUpdated: Int = 0,
    val stepsDeleted: Int = 0
) {
    val hasChanges: Boolean get() = stepsUpdated > 0 || stepsDeleted > 0
    val totalChanges: Int get() = stepsUpdated + stepsDeleted
}

/**
 * Result class for sync operations.
 */
sealed class SyncResult {
    /** Sync completed successfully */
    data class Success(
        val cachedData: CachedSyncData,
        val changes: SyncChanges = SyncChanges()
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
     * Clear the changes token to force a full sync on next sync operation.
     * Useful when backend data is reset or out of sync.
     */
    fun clearChangesToken() {
        Log.d(TAG, "clearChangesToken: Clearing token to force full sync")
        syncPreferences.clearChangesToken()
    }

    /**
     * Perform a sync operation using Health Connect Changes API.
     * Uses incremental sync if a changes token exists, otherwise performs full sync.
     * @return SyncResult indicating success or failure, with change details
     */
    suspend fun performSync(): SyncResult {
        Log.d(TAG, "performSync: Starting sync operation")
        val cachedData = getCachedData()
        Log.d(TAG, "performSync: Cached data exists: ${cachedData != null}")

        return try {
            val existingToken = syncPreferences.getChangesToken()

            if (existingToken != null) {
                // Try incremental sync using Changes API
                Log.d(TAG, "performSync: Found existing token, attempting incremental sync")
                try {
                    performIncrementalSync(existingToken, cachedData)
                } catch (e: ChangesTokenExpiredException) {
                    // Token expired, fall back to full sync
                    Log.w(TAG, "performSync: Token expired, falling back to full sync")
                    syncPreferences.clearChangesToken()
                    performFullSync(cachedData)
                }
            } else {
                // No token, perform full sync
                Log.d(TAG, "performSync: No existing token, performing full sync")
                performFullSync(cachedData)
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
            Log.e(TAG, "performSync: Unexpected error", e)
            SyncResult.Error(
                message = "Error: ${e.message ?: "Unknown error"}",
                cachedData = cachedData
            )
        }
    }

    /**
     * Perform an incremental sync using the Changes API.
     */
    private suspend fun performIncrementalSync(token: String, cachedData: CachedSyncData?): SyncResult {
        Log.d(TAG, "performIncrementalSync: Getting changes since token")

        val changeResult = healthConnectManager.getChanges(token)

        if (!changeResult.hasChanges) {
            Log.d(TAG, "performIncrementalSync: No changes detected")
            // Save new token even if no changes
            syncPreferences.saveChangesToken(changeResult.nextToken)

            return if (cachedData != null) {
                SyncResult.Success(cachedData = cachedData, changes = SyncChanges())
            } else {
                // First sync with no data - shouldn't happen in incremental
                Log.w(TAG, "performIncrementalSync: No cached data and no changes")
                performFullSync(cachedData)
            }
        }

        Log.d(TAG, "performIncrementalSync: ${changeResult.upsertedRecords.size} upserts, ${changeResult.deletedRecordIds.size} deletions")

        // Send changes to backend
        val request = SyncRequest(
            syncType = "incremental",
            records = changeResult.upsertedRecords,
            deletedRecordIds = changeResult.deletedRecordIds
        )

        // Log the full request payload
        Log.d(TAG, "performIncrementalSync: Request payload: ${gson.toJson(request)}")

        val response = ApiClient.getApi().syncHealthData(request)
        Log.d(TAG, "performIncrementalSync: API response code: ${response.code()}")
        Log.d(TAG, "performIncrementalSync: API response body: ${gson.toJson(response.body())}")

        if (response.isSuccessful && response.body()?.success == true) {
            val now = Instant.now()

            // Save new token
            syncPreferences.saveChangesToken(changeResult.nextToken)

            val newCachedData = CachedSyncData(
                syncTimestamp = now.toString(),
                formattedTimestamp = formatTimestamp(now)
            )

            syncPreferences.saveLastSyncTimestamp(now)
            syncPreferences.saveCachedSyncData(newCachedData)

            val changes = SyncChanges(
                stepsUpdated = changeResult.upsertedRecords.size,
                stepsDeleted = changeResult.deletedRecordIds.size
            )

            Log.d(TAG, "performIncrementalSync: Success! Changes: $changes")
            return SyncResult.Success(cachedData = newCachedData, changes = changes)
        } else {
            val errorMessage = response.body()?.message ?: "Server error: ${response.code()}"
            Log.e(TAG, "performIncrementalSync: API error - $errorMessage")
            return SyncResult.Error(message = errorMessage, cachedData = cachedData)
        }
    }

    /**
     * Perform a full sync (all records from last 7 days).
     */
    private suspend fun performFullSync(cachedData: CachedSyncData?): SyncResult {
        Log.d(TAG, "performFullSync: Reading all steps with IDs")

        val stepRecords = healthConnectManager.readStepsWithIds()

        if (stepRecords.isEmpty()) {
            Log.d(TAG, "performFullSync: No step data found")
            if (cachedData != null) {
                // Get a new token for future syncs even if no data
                val newToken = healthConnectManager.getChangesToken()
                syncPreferences.saveChangesToken(newToken)
                return SyncResult.Success(cachedData = cachedData, changes = SyncChanges())
            } else {
                return SyncResult.Error(
                    message = "No step data found in Health Connect",
                    cachedData = null
                )
            }
        }

        Log.d(TAG, "performFullSync: Sending ${stepRecords.size} records to API")

        val request = SyncRequest(
            syncType = "full",
            records = stepRecords,
            deletedRecordIds = emptyList()
        )

        // Log the full request payload
        Log.d(TAG, "performFullSync: Request payload: ${gson.toJson(request)}")

        val response = ApiClient.getApi().syncHealthData(request)
        Log.d(TAG, "performFullSync: API response code: ${response.code()}")
        Log.d(TAG, "performFullSync: API response body: ${gson.toJson(response.body())}")

        if (response.isSuccessful && response.body()?.success == true) {
            val now = Instant.now()

            // Get a new changes token for future incremental syncs
            val newToken = healthConnectManager.getChangesToken()
            syncPreferences.saveChangesToken(newToken)

            val newCachedData = CachedSyncData(
                syncTimestamp = now.toString(),
                formattedTimestamp = formatTimestamp(now)
            )

            syncPreferences.saveLastSyncTimestamp(now)
            syncPreferences.saveCachedSyncData(newCachedData)

            Log.d(TAG, "performFullSync: Success!")
            return SyncResult.Success(cachedData = newCachedData, changes = SyncChanges())
        } else {
            val errorMessage = response.body()?.message ?: "Server error: ${response.code()}"
            Log.e(TAG, "performFullSync: API error - $errorMessage")
            return SyncResult.Error(message = errorMessage, cachedData = cachedData)
        }
    }

    /**
     * Transform daily step counts to API record format.
     * @deprecated Use readStepsWithIds() instead which includes record IDs
     */
    private fun transformToApiFormat(dailySteps: List<DailyStepCount>): List<StepRecord> {
        return dailySteps.map { daily ->
            StepRecord(
                id = "", // No ID available from aggregated data
                type = "steps",
                date = daily.date.format(dateFormatter),
                count = daily.count,
                data = StepRecordData(
                    startTime = "", // No time available from aggregated data
                    endTime = ""
                )
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

