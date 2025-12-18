package com.circadia.healthsync.ui.sync

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.circadia.healthsync.data.model.SyncStatus
import com.circadia.healthsync.ui.utils.TimeFormatters
import kotlinx.coroutines.flow.collectLatest

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
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe sync events and show snackbar
    LaunchedEffect(Unit) {
        viewModel.syncEvents.collectLatest { event ->
            snackbarHostState.showSnackbar(
                message = event.getMessage(),
                duration = SnackbarDuration.Short
            )
        }
    }

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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
            StatusCard(
                uiState = uiState,
                onRetry = { viewModel.sync() }
            )
        }

        // Snackbar host for sync event notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
private fun StatusCard(
    uiState: SyncUiState,
    onRetry: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is SyncUiState.Ready -> {
                    val cachedData = uiState.cachedData
                    if (cachedData != null) {
                        SimplifiedStatusDisplay(
                            status = SyncStatus.UP_TO_DATE,
                            lastSyncTimestamp = cachedData.syncTimestamp
                        )
                    } else {
                        SimplifiedStatusDisplay(
                            status = SyncStatus.UP_TO_DATE,
                            lastSyncTimestamp = null
                        )
                    }
                }
                is SyncUiState.Syncing -> {
                    SimplifiedStatusDisplay(
                        status = SyncStatus.SYNCING,
                        lastSyncTimestamp = null
                    )
                }
                is SyncUiState.Refreshing -> {
                    SimplifiedStatusDisplay(
                        status = SyncStatus.SYNCING,
                        lastSyncTimestamp = uiState.cachedData.syncTimestamp
                    )
                }
                is SyncUiState.Success -> {
                    SimplifiedStatusDisplay(
                        status = SyncStatus.UP_TO_DATE,
                        lastSyncTimestamp = uiState.cachedData.syncTimestamp
                    )
                }
                is SyncUiState.Error -> {
                    SimplifiedStatusDisplay(
                        status = SyncStatus.NEEDS_ATTENTION,
                        lastSyncTimestamp = uiState.cachedData?.syncTimestamp,
                        errorMessage = uiState.message,
                        onRetry = onRetry
                    )
                }
                is SyncUiState.NoPermission -> {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Permission Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Health Connect Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Not Supported",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
 * Simplified status display showing only sync status and last sync time.
 */
@Composable
private fun SimplifiedStatusDisplay(
    status: SyncStatus,
    lastSyncTimestamp: String?,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (status == SyncStatus.NEEDS_ATTENTION) {
            Modifier.clickable { onRetry() }
        } else {
            Modifier
        }
    ) {
        // Status icon
        when (status) {
            SyncStatus.UP_TO_DATE -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Up to date",
                    tint = status.color,
                    modifier = Modifier.size(48.dp)
                )
            }
            SyncStatus.SYNCING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            SyncStatus.NEEDS_ATTENTION -> {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Needs attention",
                    tint = status.color,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status text
        Text(
            text = status.displayText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Error hint for needs attention state
        if (status == SyncStatus.NEEDS_ATTENTION) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to retry",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last synced timestamp
        if (lastSyncTimestamp != null) {
            val relativeTime = TimeFormatters.formatRelativeTimestamp(lastSyncTimestamp)
            Text(
                text = "Last synced: $relativeTime",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (status == SyncStatus.SYNCING) {
            Text(
                text = "Syncing with Health Connect...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "Not yet synced",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


