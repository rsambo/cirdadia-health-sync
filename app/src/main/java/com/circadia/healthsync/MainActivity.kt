package com.circadia.healthsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.circadia.healthsync.data.HealthConnectManager
import com.circadia.healthsync.ui.sync.SyncScreen
import com.circadia.healthsync.ui.sync.SyncUiState
import com.circadia.healthsync.ui.sync.SyncViewModel
import com.circadia.healthsync.ui.theme.CircadiaHealthSyncTheme

class MainActivity : ComponentActivity() {

    // Health Connect permission request launcher - uses the correct contract
    private val permissionLauncher = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        // Permission result will be checked by ViewModel on resume
    }

    // Health Connect install launcher
    private val installLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Health Connect availability will be checked by ViewModel on resume
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CircadiaHealthSyncTheme {
                val syncViewModel: SyncViewModel = viewModel()
                val uiState by syncViewModel.uiState.collectAsState()
                val context = LocalContext.current

                // Check if currently syncing
                val isSyncing = uiState is SyncUiState.Syncing || uiState is SyncUiState.Refreshing

                // Rotation animation for refresh icon
                val infiniteTransition = rememberInfiniteTransition(label = "refresh")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing)
                    ),
                    label = "rotation"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Circadia",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                // Refresh icon with long-press for force full sync
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync (long press for full sync)",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(12.dp)
                                        .then(
                                            if (isSyncing) Modifier.rotate(rotation) else Modifier
                                        )
                                        .combinedClickable(
                                            enabled = !isSyncing,
                                            onClick = { syncViewModel.sync() },
                                            onLongClick = {
                                                Toast.makeText(context, "Forcing full sync...", Toast.LENGTH_SHORT).show()
                                                syncViewModel.forceFullSync()
                                            }
                                        ),
                                    tint = if (isSyncing) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { innerPadding ->
                    SyncScreen(
                        viewModel = syncViewModel,
                        onRequestPermission = {
                            try {
                                permissionLauncher.launch(HealthConnectManager.PERMISSIONS)
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Failed to launch permission request", e)
                                Toast.makeText(context, "Failed to open Health Connect: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        onInstallHealthConnect = { intent ->
                            try {
                                installLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Fallback to web browser if Play Store not available
                                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                                }
                                startActivity(webIntent)
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ViewModel will recheck Health Connect status when composable recomposes
    }
}