package com.canvas.android.app.ui.resolution

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class ConfirmationDialogViewModel : ViewModel() {

    private val _confirmCountdown = MutableLiveData(10)
    val confirmCountdown: LiveData<Int> = _confirmCountdown

    var confirmCountdownJob: Job? = null
}
