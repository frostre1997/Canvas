package com.canvas.android.app.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canvas.android.app.units.ApiCaller
import kotlinx.coroutines.launch

class ResolutionViewModel : ViewModel() {

    private val apiCaller = ApiCaller()

    private val _physicalResolutionMap = MutableLiveData<Map<String, Float>?>()
    val physicalResolutionMap: LiveData<Map<String, Float>?> = _physicalResolutionMap

    private val _resolutionMap = MutableLiveData<Map<String, Float>?>()
    val resolutionMap: LiveData<Map<String, Float>?> = _resolutionMap

    private val _usersList = MutableLiveData<List<Map<String, Any>>>()
    val usersList: LiveData<List<Map<String, Any>>> = _usersList

    fun fetchScreenResolution() {
        viewModelScope.launch {
            val resolutionMap = apiCaller.fetchScreenResolution()
            _physicalResolutionMap.postValue(resolutionMap["physical"])
            _resolutionMap.postValue(resolutionMap["override"])
            _usersList.postValue(apiCaller.fetchUsers())
        }
    }
}
