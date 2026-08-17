package com.canvas.android.app.utils

import java.io.BufferedReader
import java.io.InputStreamReader

class DisplayManager {
    private fun exec(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            process.destroy()
            if (exitCode != 0) "ERROR: $error" else output
        } catch (e: Exception) {
            "EXCEPTION: ${e.message}"
        }
    }

    fun getCurrentResolution(): String {
        val output = exec("wm size")
        // output format: "Physical size: 1080x1920" or "Override size: ..."
        val lines = output.split("\n")
        for (line in lines) {
            if (line.contains("Override size:")) {
                return line.substringAfter("Override size:").trim()
            }
        }
        for (line in lines) {
            if (line.contains("Physical size:")) {
                return line.substringAfter("Physical size:").trim()
            }
        }
        return ""
    }

    fun getCurrentDensity(): String {
        val output = exec("wm density")
        val lines = output.split("\n")
        for (line in lines) {
            if (line.contains("Override density:")) {
                return line.substringAfter("Override density:").trim()
            }
        }
        for (line in lines) {
            if (line.contains("Physical density:")) {
                return line.substringAfter("Physical density:").trim()
            }
        }
        return ""
    }

    fun applyResolution(width: Int, height: Int) = exec("wm size ${width}x${height}")
    fun applyDensity(dpi: Int) = exec("wm density $dpi")
    fun resetSize() = exec("wm size reset")
    fun resetDensity() = exec("wm density reset")
}
