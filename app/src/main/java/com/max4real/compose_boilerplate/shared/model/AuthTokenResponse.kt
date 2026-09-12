package com.max4real.compose_boilerplate.shared.model

import com.google.gson.annotations.SerializedName

data class AuthTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)
