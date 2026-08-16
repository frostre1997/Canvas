package com.canvas.android.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvas.android.app.utils.DisplayManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CanvasUiState(
    val isReverting: Boolean = false,
    val countdown: Int = 10,
    val shizukuConnected: Boolean = false
)

class MainViewModel : ViewModel() {

    private val displayManager = DisplayManager()
    private var revertJob: Job? = null

    private val _uiState = MutableStateFlow(CanvasUiState())
    val uiState: StateFlow<CanvasUiState> = _uiState.asStateFlow()

    fun updateShizukuStatus(connected: Boolean) {
        _uiState.value = _uiState.value.copy(shizukuConnected = connected)
    }

    fun applyPreset(resolution: String, dpi: Int, hz: Int) {
        val parts = resolution.split("x")
        if (parts.size == 2) {
            val w = parts[0].toIntOrNull()
            val h = parts[1].toIntOrNull()
            if (w != null && h != null) {
                // Apply settings
                displayManager.applyResolution(w, h)
                displayManager.applyDensity(dpi)
                displayManager.applyRefreshRate(hz)

                // Start revert timer
                startRevertTimer()
            }
        }
    }

    private fun startRevertTimer() {
        cancelRevertTimer()
        _uiState.value = _uiState.value.copy(isReverting = true, countdown = 10)

        revertJob = viewModelScope.launch {
            var currentCount = 10
            while (currentCount > 0) {
                delay(1000)
                currentCount--
                _uiState.value = _uiState.value.copy(countdown = currentCount)
            }
            // Time's up - revert
            displayManager.resetAll()
            _uiState.value = _uiState.value.copy(isReverting = false, countdown = 10)
        }
    }

    fun keepChanges() {
        cancelRevertTimer()
        _uiState.value = _uiState.value.copy(isReverting = false, countdown = 10)
    }

    fun revertNow() {
        cancelRevertTimer()
        displayManager.resetAll()
        _uiState.value = _uiState.value.copy(isReverting = false, countdown = 10)
    }

    fun resetToDefaults() {
        if (!_uiState.value.isReverting) {
            displayManager.resetAll()
        }
    }

    private fun cancelRevertTimer() {
        revertJob?.cancel()
        revertJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelRevertTimer()
    }
}
