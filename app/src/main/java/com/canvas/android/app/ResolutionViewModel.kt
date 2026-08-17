package com.canvas.android.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvas.android.app.units.ApiCaller
import com.canvas.android.app.utils.DisplayManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResolutionUiState(
    val height: String = "",
    val width: String = "",
    val dpi: String = "",
    val scale: Int = 100,
    val physicalResolution: Map<String, Float>? = null,
    val isReverting: Boolean = false,
    val countdown: Int = 10
)

class ResolutionViewModel : ViewModel() {

    private val apiCaller = ApiCaller()
    private val displayManager = DisplayManager()
    private var revertJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(ResolutionUiState())
    val uiState: StateFlow<ResolutionUiState> = _uiState.asStateFlow()

    fun updateHeight(value: String) {
        _uiState.update { it.copy(height = value) }
    }

    fun updateWidth(value: String) {
        _uiState.update { it.copy(width = value) }
    }

    fun updateDpi(value: String) {
        _uiState.update { it.copy(dpi = value) }
    }

    fun updateScale(value: Int) {
        _uiState.update { it.copy(scale = value) }
    }

    fun fetchScreenResolution() {
        viewModelScope.launch {
            try {
                val resolutionMap = apiCaller.fetchScreenResolution()
                val physical = resolutionMap["physical"]
                val override = resolutionMap["override"]
                _uiState.update {
                    it.copy(
                        physicalResolution = physical,
                        height = override?.get("height")?.toInt()?.toString() ?: "",
                        width = override?.get("width")?.toInt()?.toString() ?: "",
                        dpi = override?.get("dpi")?.toInt()?.toString() ?: ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun applyResolution() {
        val height = _uiState.value.height.toFloatOrNull()
        val width = _uiState.value.width.toFloatOrNull()
        val dpi = _uiState.value.dpi.toFloatOrNull()
        if (height == null || width == null || dpi == null) return

        viewModelScope.launch {
            apiCaller.applyResolution(height, width, dpi)
            startRevertTimer()
        }
    }

    fun resetResolution() {
        viewModelScope.launch {
            apiCaller.resetResolution()
            fetchScreenResolution()
        }
    }

    private fun startRevertTimer() {
        cancelRevertTimer()
        _uiState.update { it.copy(isReverting = true, countdown = 10) }
        revertJob = viewModelScope.launch {
            var count = 10
            while (count > 0) {
                delay(1000)
                count--
                _uiState.update { it.copy(countdown = count) }
            }
            // Auto-revert
            apiCaller.resetResolution()
            _uiState.update { it.copy(isReverting = false, countdown = 10) }
            fetchScreenResolution()
        }
    }

    fun keepChanges() {
        cancelRevertTimer()
        _uiState.update { it.copy(isReverting = false, countdown = 10) }
    }

    fun revertNow() {
        cancelRevertTimer()
        viewModelScope.launch {
            apiCaller.resetResolution()
            _uiState.update { it.copy(isReverting = false, countdown = 10) }
            fetchScreenResolution()
        }
    }

    private fun cancelRevertTimer() {
        revertJob?.cancel()
        revertJob = null
    }
}
