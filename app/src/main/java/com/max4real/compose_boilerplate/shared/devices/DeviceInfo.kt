package com.max4real.compose_boilerplate.shared.devices

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("device_id")
    val deviceId: String,
    val platform: String = PLATFORM_ANDROID,
    @SerializedName("device_model")
    val deviceModel: String? = null,
    @SerializedName("os_version")
    val osVersion: String? = null,
    @SerializedName("app_version")
    val appVersion: String? = null,
    @SerializedName("fcm_token")
    val fcmToken: String? = null
) {
    companion object {
        const val PLATFORM_ANDROID = "android"
    }
}
