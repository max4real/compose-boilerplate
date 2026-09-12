package com.max4real.compose_boilerplate.di.refresh

import com.google.gson.annotations.SerializedName
import com.max4real.compose_boilerplate.shared.devices.DeviceInfo

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String,
    val device: DeviceInfo? = null
)
