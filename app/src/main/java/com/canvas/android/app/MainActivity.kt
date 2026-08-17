package com.canvas.android.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canvas.android.app.ui.theme.CanvasTheme
import com.canvas.android.app.utils.ShizukuHelper
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shizuku permission listener
        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                // permission granted – UI will react via ViewModel
            }
        }

        setContent {
            CanvasTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ResolutionScreen(
                        activity = this@MainActivity
                    )
                }
            }
        }
    }
}

@Composable
fun ResolutionScreen(
    activity: ComponentActivity,
    viewModel: ResolutionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shizukuReady = ShizukuHelper.isReady() && ShizukuHelper.hasPermission()

    // Fetch screen info when Shizuku is ready
    LaunchedEffect(shizukuReady) {
        if (shizukuReady) {
            viewModel.fetchScreenResolution()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Canvas",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "v0.100.0",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Shizuku status
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (shizukuReady)
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
                    text = if (shizukuReady) "Shizuku Connected" else "Shizuku Required",
                    fontWeight = FontWeight.Medium
                )
                if (!shizukuReady) {
                    Button(onClick = { ShizukuHelper.requestPermission(activity) }) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // Current resolution info
        val physical = uiState.physicalResolution
        if (physical != null) {
            Text(
                text = "Physical: ${physical["height"]}x${physical["width"]} @ ${physical["dpi"]}dpi",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Height input
        OutlinedTextField(
            value = uiState.height,
            onValueChange = { viewModel.updateHeight(it) },
            label = { Text("Height") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Width input
        OutlinedTextField(
            value = uiState.width,
            onValueChange = { viewModel.updateWidth(it) },
            label = { Text("Width") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // DPI input
        OutlinedTextField(
            value = uiState.dpi,
            onValueChange = { viewModel.updateDpi(it) },
            label = { Text("DPI") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Scale slider (0-100)
        Text("Scale: ${uiState.scale}%", fontSize = 14.sp)
        Slider(
            value = uiState.scale.toFloat(),
            onValueChange = { viewModel.updateScale(it.toInt()) },
            valueRange = 50f..150f,
            steps = 10,
            modifier = Modifier.fillMaxWidth()
        )

        // Apply and Reset buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (shizukuReady) {
                        viewModel.applyResolution()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
            Button(
                onClick = {
                    if (shizukuReady) {
                        viewModel.resetResolution()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Reset")
            }
        }

        // Countdown / revert UI
        if (uiState.isReverting) {
            Text(
                text = "Reverting in ${uiState.countdown} seconds...",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            LinearProgressIndicator(
                progress = { uiState.countdown / 10f },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.keepChanges() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Keep")
                }
                Button(
                    onClick = { viewModel.revertNow() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Revert")
                }
            }
        }
    }
}
