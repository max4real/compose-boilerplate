package com.max4real.compose_boilerplate.shared.config

import com.max4real.compose_boilerplate.BuildConfig

object AppEnvironment {
    const val API_BASE_URL: String = BuildConfig.API_BASE_URL
    const val MEDIA_BASE_URL: String = BuildConfig.MEDIA_BASE_URL

    const val VERSION_NAME: String = BuildConfig.VERSION_NAME
    const val VERSION_CODE: Int = BuildConfig.VERSION_CODE

    // Add other environment-driven constants here as the app needs them.
}
