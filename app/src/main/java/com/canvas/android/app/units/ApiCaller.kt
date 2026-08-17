package com.canvas.android.app.units

import android.graphics.Point
import android.view.Display
import com.canvas.android.app.utils.DisplayManager
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Field

class ApiCaller {

    private var iWindowManager: Any? = null
    private var iUserManager: Any? = null
    private val displayManager = DisplayManager()

    init {
        try {
            iWindowManager = asInterface("android.view.IWindowManager", "window")
            iUserManager = asInterface("android.os.IUserManager", "user")
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to shell commands
        }
    }

    private fun asInterface(className: String, serviceName: String): Any? {
        return try {
            val binder = SystemServiceHelper.getSystemService(serviceName)
            if (binder == null) return null
            ShizukuBinderWrapper(binder).let {
                Class.forName("$className\$Stub").run {
                    HiddenApiBypass.invoke(this, null, "asInterface", it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun fetchUsers(): List<Map<String, Any>> {
        if (iUserManager == null) return emptyList()
        try {
            val users = HiddenApiBypass.invoke(
                iUserManager!!::class.java,
                iUserManager,
                "getUsers",
                true,
                true,
                true
            ) as List<*>
            val userInfoFields =
                HiddenApiBypass.getInstanceFields(Class.forName("android.content.pm.UserInfo")) as List<Field>

            val idField = userInfoFields.first { it.name == "id" }
            val nameField = userInfoFields.first { it.name == "name" }

            return users.map { userInfo ->
                mapOf(
                    "id" to idField.get(userInfo)!!,
                    "name" to nameField.get(userInfo)!!
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun fetchScreenResolution(): Map<String, Map<String, Float>> {
        // Try using hidden APIs first
        if (iWindowManager != null) {
            try {
                val physicalSize = Point()
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java,
                    iWindowManager,
                    "getInitialDisplaySize",
                    Display.DEFAULT_DISPLAY,
                    physicalSize
                )
                val physicalDpi = HiddenApiBypass.invoke(
                    iWindowManager!!::class.java,
                    iWindowManager,
                    "getInitialDisplayDensity",
                    Display.DEFAULT_DISPLAY
                ) as Int

                val overrideSize = Point()
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java,
                    iWindowManager,
                    "getBaseDisplaySize",
                    Display.DEFAULT_DISPLAY,
                    overrideSize
                )
                val overrideDpi = HiddenApiBypass.invoke(
                    iWindowManager!!::class.java,
                    iWindowManager,
                    "getBaseDisplayDensity",
                    Display.DEFAULT_DISPLAY
                ) as Int

                return mapOf(
                    "physical" to mapOf(
                        "height" to physicalSize.y.toFloat(),
                        "width" to physicalSize.x.toFloat(),
                        "dpi" to physicalDpi.toFloat()
                    ),
                    "override" to mapOf(
                        "height" to overrideSize.y.toFloat(),
                        "width" to overrideSize.x.toFloat(),
                        "dpi" to overrideDpi.toFloat()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback: use shell commands
        return try {
            val sizeOutput = displayManager.getCurrentResolution()
            val dpiOutput = displayManager.getCurrentDensity()
            val dpi = dpiOutput.toFloatOrNull() ?: 240f
            val parts = sizeOutput.split("x")
            if (parts.size == 2) {
                val w = parts[0].toFloatOrNull() ?: 1080f
                val h = parts[1].toFloatOrNull() ?: 1920f
                mapOf(
                    "physical" to mapOf(
                        "height" to h,
                        "width" to w,
                        "dpi" to dpi
                    ),
                    "override" to mapOf(
                        "height" to h,
                        "width" to w,
                        "dpi" to dpi
                    )
                )
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun applyResolution(height: Float, width: Float, dpi: Float) {
        if (iWindowManager != null) {
            try {
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java, iWindowManager,
                    "setForcedDisplaySize", Display.DEFAULT_DISPLAY, width.toInt(), height.toInt()
                )
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java, iWindowManager,
                    "setForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, dpi.toInt(), 0
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        displayManager.applyResolution(width.toInt(), height.toInt())
        displayManager.applyDensity(dpi.toInt())
    }

    fun resetResolution() {
        if (iWindowManager != null) {
            try {
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java, iWindowManager,
                    "clearForcedDisplaySize", Display.DEFAULT_DISPLAY
                )
                HiddenApiBypass.invoke(
                    iWindowManager!!::class.java, iWindowManager,
                    "clearForcedDisplayDensityForUser", Display.DEFAULT_DISPLAY, 0
                )
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        displayManager.resetSize()
        displayManager.resetDensity()
    }
}
