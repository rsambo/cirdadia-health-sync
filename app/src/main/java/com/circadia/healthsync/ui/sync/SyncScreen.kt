package com.circadia.healthsync.ui.sync

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.circadia.healthsync.data.model.CachedSyncData
import com.circadia.healthsync.data.model.UploadStatus
import com.circadia.healthsync.ui.utils.TimeFormatters

/**
 * Main sync screen composable.
 * Displays the status area with sync information.
 */
@Composable
fun SyncScreen(
    viewModel: SyncViewModel,
    onRequestPermission: (Intent) -> Unit,
    onInstallHealthConnect: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Trigger auto-sync when app comes to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.autoSync()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Action buttons for permission/install (only when needed)
        ActionButton(
            uiState = uiState,
            onRequestPermission = { onRequestPermission(viewModel.getPermissionRequestIntent()) },
            onInstallHealthConnect = { onInstallHealthConnect(viewModel.getInstallHealthConnectIntent()) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Area
        StatusCard(uiState = uiState)
    }
}

/**
 * Action button for permission/install actions only.
 * Only shows when user action is required.
 */
@Composable
private fun ActionButton(
    uiState: SyncUiState,
    onRequestPermission: () -> Unit,
    onInstallHealthConnect: () -> Unit
) {
    when (uiState) {
        is SyncUiState.NoPermission -> {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Grant Permission",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        is SyncUiState.NoHealthConnect -> {
            Button(
                onClick = onInstallHealthConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Install Health Connect",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        else -> {
            // No action button needed for other states
        }
    }
}

/**
 * Status card displaying current sync state information.
 */
@Composable
private fun StatusCard(uiState: SyncUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Status",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState) {
                is SyncUiState.Ready -> {
                    val cachedData = uiState.cachedData
                    if (cachedData != null) {
                        CachedDataDisplay(
                            cachedData = cachedData,
                            uploadStatus = UploadStatus.OK
                        )
                    } else {
                        Text(
                            text = "Ready to sync",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is SyncUiState.Syncing -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Syncing health data...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                is SyncUiState.Refreshing -> {
                    // Show cached data with a loading indicator
                    CachedDataDisplay(
                        cachedData = uiState.cachedData,
                        isRefreshing = true,
                        uploadStatus = UploadStatus.PENDING
                    )
                }
                is SyncUiState.Success -> {
                    CachedDataDisplay(
                        cachedData = uiState.cachedData,
                        uploadStatus = UploadStatus.OK
                    )
                }
                is SyncUiState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sync failed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    // Show cached data if available
                    val cachedData = uiState.cachedData
                    if (cachedData != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        CachedDataDisplay(
                            cachedData = cachedData,
                            uploadStatus = UploadStatus.ERROR
                        )
                    }
                }
                is SyncUiState.NoPermission -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Permission Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Grant Health Connect permission to sync your step data",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                is SyncUiState.NoHealthConnect -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Health Connect Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Please install Health Connect from the Play Store",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                is SyncUiState.NotSupported -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Not Supported",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Health Connect is not supported on this device",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Displays cached sync data with optional refreshing indicator.
 */
@Composable
private fun CachedDataDisplay(
    cachedData: CachedSyncData,
    isRefreshing: Boolean = false,
    uploadStatus: UploadStatus = UploadStatus.OK
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Today's step count (prominent)
        Text(
            text = cachedData.todayStepCount.formatWithCommas(),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "steps today",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7-day total (secondary)
        Text(
            text = "${cachedData.totalStepCount.formatWithCommas()} steps (7 days)",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Step diff (if available and non-zero)
        cachedData.stepDiff?.let { diff ->
            Spacer(modifier = Modifier.height(4.dp))
            val diffText = if (diff > 0) "+${diff.formatWithCommas()}" else diff.formatWithCommas()
            val diffColor = when {
                diff > 0 -> Color(0xFF4CAF50)  // Green
                diff < 0 -> Color(0xFFF44336)  // Red
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = "$diffText since last sync",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = diffColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last synced timestamp
        if (isRefreshing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Updating...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val relativeTime = TimeFormatters.formatRelativeTimestamp(cachedData.syncTimestamp)
            Text(
                text = "Last updated: $relativeTime",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Upload status indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Colored dot
            Surface(
                modifier = Modifier.size(8.dp),
                shape = MaterialTheme.shapes.small,
                color = uploadStatus.color
            ) {}
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Upload status: ${uploadStatus.displayText}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Extension function to format a Long with comma separators.
 */
private fun Long.formatWithCommas(): String {
    return String.format(java.util.Locale.US, "%,d", this)
}

