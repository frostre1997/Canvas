package com.canvas.android.app.utils

import android.app.Activity
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object ShizukuHelper {
    fun isReady(): Boolean =
        Shizuku.getVersion() > 0 && Shizuku.getBinder() != null

    fun hasPermission(): Boolean =
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

    fun requestPermission(activity: Activity) {
        if (isReady() && !hasPermission()) {
            Shizuku.requestPermission(0)
        }
    }
}
