package com.circadia.healthsync.ui.sync

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.circadia.healthsync.data.DailyStepCount
import com.circadia.healthsync.data.HealthConnectAvailability
import com.circadia.healthsync.data.HealthConnectManager
import com.circadia.healthsync.data.SyncRepository
import com.circadia.healthsync.data.SyncResult
import com.circadia.healthsync.data.local.SyncPreferences
import com.circadia.healthsync.data.model.CachedSyncData
import com.circadia.healthsync.data.model.StepRecord
import com.circadia.healthsync.data.model.StepRecordData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private const val TAG = "SyncViewModel"

/**
 * Represents the various states of the sync UI.
 */
sealed class SyncUiState {
    /** Initial state, ready to sync, optionally with cached data */
    data class Ready(val cachedData: CachedSyncData? = null) : SyncUiState()

    /** Sync is in progress (no cached data to show) */
    object Syncing : SyncUiState()

    /** Sync is in progress, but we have cached data to display */
    data class Refreshing(val cachedData: CachedSyncData) : SyncUiState()

    /** Sync completed successfully */
    data class Success(
        val cachedData: CachedSyncData
    ) : SyncUiState()

    /** Sync failed with an error, optionally with cached data to show */
    data class Error(
        val message: String,
        val cachedData: CachedSyncData? = null
    ) : SyncUiState()

    /** Health Connect permission not granted */
    object NoPermission : SyncUiState()

    /** Health Connect not installed on device */
    object NoHealthConnect : SyncUiState()

    /** Health Connect not supported on this device */
    object NotSupported : SyncUiState()
}

/**
 * ViewModel for the Sync screen.
 * Manages Health Connect integration and sync operations.
 */
class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val healthConnectManager = HealthConnectManager(application)
    private val syncPreferences = SyncPreferences(application)
    private val syncRepository = SyncRepository(healthConnectManager, syncPreferences)

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Ready())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    // One-time events for showing notifications
    private val _syncEvents = MutableSharedFlow<SyncEvent>()
    val syncEvents: SharedFlow<SyncEvent> = _syncEvents.asSharedFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

    // Flag to prevent multiple simultaneous syncs
    private var isSyncing = false

    init {
        // Load cached data immediately on init
        loadCachedData()
        checkHealthConnectAvailability()
    }

    /**
     * Load cached data on ViewModel init and emit to UI immediately.
     */
    private fun loadCachedData() {
        val cachedData = syncRepository.getCachedData()
        _uiState.value = SyncUiState.Ready(cachedData)
    }

    /**
     * Check if Health Connect is available and permissions are granted.
     */
    fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            val cachedData = syncRepository.getCachedData()
            when (healthConnectManager.getHealthConnectAvailability()) {
                is HealthConnectAvailability.Available -> {
                    checkPermissions(cachedData)
                }
                is HealthConnectAvailability.NotInstalled -> {
                    _uiState.value = SyncUiState.NoHealthConnect
                }
                is HealthConnectAvailability.NotSupported -> {
                    _uiState.value = SyncUiState.NotSupported
                }
            }
        }
    }

    /**
     * Check if required permissions are granted.
     */
    private suspend fun checkPermissions(cachedData: CachedSyncData?) {
        if (healthConnectManager.hasAllPermissions()) {
            _uiState.value = SyncUiState.Ready(cachedData)
        } else {
            _uiState.value = SyncUiState.NoPermission
        }
    }

    /**
     * Called after permissions are requested.
     * Rechecks permission status.
     */
    fun onPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            val cachedData = syncRepository.getCachedData()
            if (granted) {
                _uiState.value = SyncUiState.Ready(cachedData)
            } else {
                _uiState.value = SyncUiState.NoPermission
            }
        }
    }

    /**
     * Get the intent to request Health Connect permissions.
     */
    fun getPermissionRequestIntent() = healthConnectManager.createPermissionRequestIntent()

    /**
     * Get the intent to install Health Connect.
     */
    fun getInstallHealthConnectIntent() = healthConnectManager.getHealthConnectInstallIntent()

    /**
     * Auto-sync method that loads cache first, then fetches new data.
     * Used when app comes to foreground.
     * Only syncs if Health Connect is available and permissions are granted.
     */
    fun autoSync() {
        Log.d(TAG, "autoSync: Called")
        // Prevent multiple simultaneous syncs
        if (isSyncing) {
            Log.d(TAG, "autoSync: Already syncing, skipping")
            return
        }

        viewModelScope.launch {
            // First check Health Connect availability and permissions
            Log.d(TAG, "autoSync: Checking Health Connect availability")
            when (healthConnectManager.getHealthConnectAvailability()) {
                is HealthConnectAvailability.Available -> {
                    Log.d(TAG, "autoSync: Health Connect is available, checking permissions")
                    if (!healthConnectManager.hasAllPermissions()) {
                        Log.d(TAG, "autoSync: Permissions not granted")
                        // Don't auto-sync without permissions, but show cached data
                        val cachedData = syncRepository.getCachedData()
                        _uiState.value = SyncUiState.NoPermission
                        return@launch
                    }
                    Log.d(TAG, "autoSync: Permissions granted")
                }
                is HealthConnectAvailability.NotInstalled -> {
                    Log.d(TAG, "autoSync: Health Connect not installed")
                    _uiState.value = SyncUiState.NoHealthConnect
                    return@launch
                }
                is HealthConnectAvailability.NotSupported -> {
                    Log.d(TAG, "autoSync: Health Connect not supported")
                    _uiState.value = SyncUiState.NotSupported
                    return@launch
                }
            }

            // Proceed with sync
            isSyncing = true
            Log.d(TAG, "autoSync: Starting sync")

            val cachedData = syncRepository.getCachedData()
            Log.d(TAG, "autoSync: Cached data exists: ${cachedData != null}")

            // Set appropriate loading state based on cached data
            _uiState.value = if (cachedData != null) {
                Log.d(TAG, "autoSync: Setting state to Refreshing")
                SyncUiState.Refreshing(cachedData)
            } else {
                Log.d(TAG, "autoSync: Setting state to Syncing")
                SyncUiState.Syncing
            }

            // Perform sync
            Log.d(TAG, "autoSync: Calling performSync")
            when (val result = syncRepository.performSync()) {
                is SyncResult.Success -> {
                    Log.d(TAG, "autoSync: Sync successful")
                    _uiState.value = SyncUiState.Success(cachedData = result.cachedData)

                    // Emit event if changes were detected
                    emitSyncEvent(result.changes)
                }
                is SyncResult.Error -> {
                    Log.e(TAG, "autoSync: Sync failed - ${result.message}")
                    _uiState.value = SyncUiState.Error(
                        message = result.message,
                        cachedData = result.cachedData
                    )
                }
            }

            isSyncing = false
            Log.d(TAG, "autoSync: Complete")
        }
    }

    /**
     * Perform the manual sync operation.
     * Reads steps from Health Connect and sends to backend.
     */
    fun sync() {
        Log.d(TAG, "sync: Called")
        // Prevent multiple syncs
        if (isSyncing) {
            Log.d(TAG, "sync: Already syncing, skipping")
            return
        }

        viewModelScope.launch {
            Log.d(TAG, "sync: Checking permissions")
            // Check permissions first
            if (!healthConnectManager.hasAllPermissions()) {
                Log.d(TAG, "sync: Permissions not granted, showing permission UI")
                _uiState.value = SyncUiState.NoPermission
                return@launch
            }
            Log.d(TAG, "sync: Permissions OK, starting sync")

            isSyncing = true

            val cachedData = syncRepository.getCachedData()
            Log.d(TAG, "sync: Got cached data: ${cachedData != null}")

            // Set appropriate loading state based on cached data
            _uiState.value = if (cachedData != null) {
                SyncUiState.Refreshing(cachedData)
            } else {
                SyncUiState.Syncing
            }
            Log.d(TAG, "sync: Set UI state, calling performSync...")

            // Perform sync using repository
            try {
                when (val result = syncRepository.performSync()) {
                    is SyncResult.Success -> {
                        Log.d(TAG, "sync: Success!")
                        _uiState.value = SyncUiState.Success(cachedData = result.cachedData)

                        // Emit event if changes were detected
                        emitSyncEvent(result.changes)
                    }
                    is SyncResult.Error -> {
                        Log.e(TAG, "sync: Error - ${result.message}")
                        _uiState.value = SyncUiState.Error(
                            message = result.message,
                            cachedData = result.cachedData
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sync: Exception during performSync", e)
                _uiState.value = SyncUiState.Error(
                    message = "Sync failed: ${e.message}",
                    cachedData = cachedData
                )
            }

            isSyncing = false
            Log.d(TAG, "sync: Complete")
        }
    }

    /**
     * Emit a sync event based on the changes detected.
     */
    private fun emitSyncEvent(changes: com.circadia.healthsync.data.SyncChanges) {
        if (!changes.hasChanges) return

        viewModelScope.launch {
            val event = when {
                changes.stepsUpdated > 0 && changes.stepsDeleted > 0 -> {
                    SyncEvent.MultipleChanges(changes.totalChanges)
                }
                changes.stepsUpdated > 0 -> {
                    SyncEvent.StepsUpdated(changes.stepsUpdated)
                }
                changes.stepsDeleted > 0 -> {
                    SyncEvent.StepsDeleted(changes.stepsDeleted)
                }
                else -> return@launch
            }
            Log.d(TAG, "emitSyncEvent: Emitting ${event.getMessage()}")
            _syncEvents.emit(event)
        }
    }

    /**
     * Transform daily step counts to API record format.
     * @deprecated Use SyncRepository which now handles record IDs
     */
    private fun transformToApiFormat(dailySteps: List<DailyStepCount>): List<StepRecord> {
        return dailySteps.map { daily ->
            StepRecord(
                id = "", // Deprecated method, no ID available
                type = "steps",
                date = daily.date.format(dateFormatter),
                count = daily.count,
                data = StepRecordData(
                    startTime = "", // Deprecated method, no time available
                    endTime = ""
                )
            )
        }
    }

    /**
     * Format the timestamp for display.
     */
    private fun formatTimestamp(isoTimestamp: String): String {
        return try {
            val instant = java.time.Instant.parse(isoTimestamp)
            val localDateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            localDateTime.format(displayFormatter)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    /**
     * Reset to ready state (e.g., to try syncing again).
     */
    fun resetToReady() {
        if (!isSyncing) {
            val cachedData = syncRepository.getCachedData()
            _uiState.value = SyncUiState.Ready(cachedData)
        }
    }

    /**
     * Force a full sync by clearing the changes token and then syncing.
     * Use this when the backend data is out of sync with the app.
     */
    fun forceFullSync() {
        if (isSyncing) return

        viewModelScope.launch {
            // Check permissions first
            if (!healthConnectManager.hasAllPermissions()) {
                Log.d(TAG, "forceFullSync: Permissions not granted, showing permission UI")
                _uiState.value = SyncUiState.NoPermission
                return@launch
            }

            Log.d(TAG, "forceFullSync: Clearing token and forcing full sync")
            syncRepository.clearChangesToken()
            sync()
        }
    }
}

