package com.max4real.compose_boilerplate.shared.devices

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun currentDevice(fcmToken: String? = null): DeviceInfo {
        return DeviceInfo(
            deviceId = deviceId(),
            deviceModel = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            appVersion = appVersion(),
            fcmToken = fcmToken?.takeIf { it.isNotBlank() }
        )
    }

    fun deviceId(): String {
        val current = prefs.getString(DEVICE_ID, null)
        if (!current.isNullOrBlank()) return current

        val next = UUID.randomUUID().toString()
        prefs.edit { putString(DEVICE_ID, next) }
        return next
    }

    private fun appVersion(): String? {
        return runCatching {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName
        }.getOrNull()
    }

    private companion object {
        const val PREF_NAME = "device_info_prefs"
        const val DEVICE_ID = "device_id"
    }
}
