package com.canvas.android.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _shizukuPermissionGranted = MutableLiveData(false)
    val shizukuPermissionGranted: LiveData<Boolean> = _shizukuPermissionGranted
}
