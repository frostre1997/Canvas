package com.canvas.android.app.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConfirmationDialogViewModel : ViewModel() {
    private val _confirmCountdown = MutableLiveData(10)
    val confirmCountdown: LiveData<Int> = _confirmCountdown

    fun setCountdown(value: Int) {
        _confirmCountdown.postValue(value)
    }
}
