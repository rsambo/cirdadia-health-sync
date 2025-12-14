package com.circadia.healthsync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.circadia.healthsync.ui.sync.SyncScreen
import com.circadia.healthsync.ui.sync.SyncViewModel
import com.circadia.healthsync.ui.theme.CircadiaHealthSyncTheme

class MainActivity : ComponentActivity() {

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Permission result will be checked by ViewModel on resume
    }

    // Health Connect install launcher
    private val installLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Health Connect availability will be checked by ViewModel on resume
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CircadiaHealthSyncTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val syncViewModel: SyncViewModel = viewModel()

                    SyncScreen(
                        viewModel = syncViewModel,
                        onRequestPermission = { intent ->
                            permissionLauncher.launch(intent)
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