package com.canvas.android.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvas.android.app.ui.theme.CanvasTheme
import com.canvas.android.app.utils.ShizukuHelper
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shizuku permission result listener
        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                // Permission granted – UI will update via state
            }
        }

        setContent {
            CanvasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Check Shizuku status on each recomposition
                    LaunchedEffect(Unit) {
                        val isReady = ShizukuHelper.isReady() && ShizukuHelper.hasPermission()
                        viewModel.updateShizukuStatus(isReady)
                    }

                    CanvasDashboard(
                        uiState = uiState,
                        onRequestShizuku = {
                            ShizukuHelper.requestPermission(this@MainActivity)
                        },
                        onApplyPreset = { resolution, dpi, hz ->
                            viewModel.applyPreset(resolution, dpi, hz)
                        },
                        onKeepChanges = {
                            viewModel.keepChanges()
                        },
                        onRevertNow = {
                            viewModel.revertNow()
                        },
                        onReset = {
                            viewModel.resetToDefaults()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CanvasDashboard(
    uiState: CanvasUiState,
    onRequestShizuku: () -> Unit,
    onApplyPreset: (String, Int, Int) -> Unit,
    onKeepChanges: () -> Unit,
    onRevertNow: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        // App Title
        Text(
            text = "Canvas",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "v0.100.1",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Shizuku Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.shizukuConnected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.shizukuConnected) "Shizuku Connected" else "Shizuku Required",
                    fontWeight = FontWeight.Medium
                )
                if (!uiState.shizukuConnected) {
                    Button(onClick = onRequestShizuku) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Preset Cards
        PresetCard(
            title = "FHD+ Smooth",
            resolution = "1080x2400",
            dpi = 480,
            hz = 90,
            onClick = {
                if (uiState.shizukuConnected) {
                    onApplyPreset("1080x2400", 480, 90)
                }
            }
        )

        PresetCard(
            title = "Battery Saver",
            resolution = "720x1600",
            dpi = 320,
            hz = 60,
            onClick = {
                if (uiState.shizukuConnected) {
                    onApplyPreset("720x1600", 320, 60)
                }
            }
        )

        PresetCard(
            title = "Gaming Mode",
            resolution = "1440x3120",
            dpi = 560,
            hz = 120,
            onClick = {
                if (uiState.shizukuConnected) {
                    onApplyPreset("1440x3120", 560, 120)
                }
            }
        )

        // Countdown & Action Buttons
        if (uiState.isReverting) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Reverting in ${uiState.countdown} seconds...",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                LinearProgressIndicator(
                    progress = { uiState.countdown / 10f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onKeepChanges,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Keep Changes")
                    }
                    Button(
                        onClick = onRevertNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Revert Now")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onReset,
            enabled = !uiState.isReverting
        ) {
            Text("Reset to Defaults")
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    resolution: String,
    dpi: Int,
    hz: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(
                    text = "$resolution / ${dpi}dpi / ${hz}Hz",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Apply",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
