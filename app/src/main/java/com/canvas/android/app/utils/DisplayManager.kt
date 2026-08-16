package com.canvas.android.app.utils

import rikka.shizuku.Shizuku

class DisplayManager {

    private fun exec(command: String): String {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.destroy()
            if (exitCode != 0) "ERROR: $error" else output
        } catch (e: Exception) {
            "EXCEPTION: ${e.message}"
        }
    }

    fun applyResolution(width: Int, height: Int): String = exec("wm size ${width}x${height}")
    fun applyDensity(dpi: Int): String = exec("wm density $dpi")
    fun applyRefreshRate(hz: Int): String = exec("settings put system peak_refresh_rate $hz")
    fun resetSize(): String = exec("wm size reset")
    fun resetDensity(): String = exec("wm density reset")

    fun resetAll(): String {
        val r1 = resetSize()
        val r2 = resetDensity()
        return "$r1\n$r2"
    }
}
