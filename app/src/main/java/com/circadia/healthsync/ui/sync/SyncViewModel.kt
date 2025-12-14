package com.circadia.healthsync.ui.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.circadia.healthsync.data.DailyStepCount
import com.circadia.healthsync.data.HealthConnectAvailability
import com.circadia.healthsync.data.HealthConnectManager
import com.circadia.healthsync.data.api.ApiClient
import com.circadia.healthsync.data.model.StepRecord
import com.circadia.healthsync.data.model.SyncRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Represents the various states of the sync UI.
 */
sealed class SyncUiState {
    /** Initial state, ready to sync */
    object Ready : SyncUiState()

    /** Sync is in progress */
    object Syncing : SyncUiState()

    /** Sync completed successfully */
    data class Success(
        val recordCount: Int,
        val timestamp: String
    ) : SyncUiState()

    /** Sync failed with an error */
    data class Error(val message: String) : SyncUiState()

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

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Ready)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        checkHealthConnectAvailability()
    }

    /**
     * Check if Health Connect is available and permissions are granted.
     */
    fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            when (healthConnectManager.getHealthConnectAvailability()) {
                is HealthConnectAvailability.Available -> {
                    checkPermissions()
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
    private suspend fun checkPermissions() {
        if (healthConnectManager.hasAllPermissions()) {
            _uiState.value = SyncUiState.Ready
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
            if (granted) {
                _uiState.value = SyncUiState.Ready
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
     * Perform the sync operation.
     * Reads steps from Health Connect and sends to backend.
     */
    fun sync() {
        // Prevent multiple syncs
        if (_uiState.value is SyncUiState.Syncing) return

        viewModelScope.launch {
            _uiState.value = SyncUiState.Syncing

            try {
                // Step 1: Read steps from Health Connect
                val dailySteps = healthConnectManager.readStepsForLast7Days()

                if (dailySteps.isEmpty()) {
                    _uiState.value = SyncUiState.Error("No step data found for the last 7 days")
                    return@launch
                }

                // Step 2: Transform to API format
                val stepRecords = transformToApiFormat(dailySteps)
                val request = SyncRequest(records = stepRecords)

                // Step 3: Send to backend
                val response = ApiClient.getApi().syncHealthData(request)

                // Step 4: Handle response
                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    _uiState.value = SyncUiState.Success(
                        recordCount = body.recordCount,
                        timestamp = formatTimestamp(body.timestamp)
                    )
                } else {
                    val errorMessage = response.body()?.message
                        ?: "Server error: ${response.code()}"
                    _uiState.value = SyncUiState.Error(errorMessage)
                }

            } catch (e: java.net.UnknownHostException) {
                _uiState.value = SyncUiState.Error("Network error: Cannot reach server")
            } catch (e: java.net.SocketTimeoutException) {
                _uiState.value = SyncUiState.Error("Network error: Connection timed out")
            } catch (e: Exception) {
                _uiState.value = SyncUiState.Error("Error: ${e.message ?: "Unknown error"}")
            }
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
     * Format the timestamp for display.
     */
    private fun formatTimestamp(isoTimestamp: String): String {
        return try {
            val instant = java.time.Instant.parse(isoTimestamp)
            val localDateTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
            localDateTime.format(formatter)
        } catch (e: Exception) {
            isoTimestamp
        }
    }

    /**
     * Reset to ready state (e.g., to try syncing again).
     */
    fun resetToReady() {
        if (_uiState.value !is SyncUiState.Syncing) {
            _uiState.value = SyncUiState.Ready
        }
    }
}

