package com.canvas.android.app.utils

import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedReader
import java.io.InputStreamReader

class DisplayManager {

    private fun exec(command: String): String {
        return try {
            // Use reflection to access the private newProcess method
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true

            val remoteProcess = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as ShizukuRemoteProcess

            val output = remoteProcess.inputStream.bufferedReader().use { it.readText() }
            val error = remoteProcess.errorStream.bufferedReader().use { it.readText() }
            remoteProcess.destroy()

            if (error.isNotEmpty()) "ERROR: $error" else output
        } catch (e: Exception) {
            // Fallback to root shell if available
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                val exitCode = process.waitFor()
                val output = process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()
                process.destroy()
                if (exitCode != 0) "ERROR (root): $error" else output
            } catch (e2: Exception) {
                "EXCEPTION: ${e2.message}"
            }
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
