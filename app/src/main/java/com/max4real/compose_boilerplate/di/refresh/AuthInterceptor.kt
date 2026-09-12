package com.max4real.compose_boilerplate.di.refresh

import com.max4real.compose_boilerplate.shared.managers.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken = tokenManager.getAccessToken()

        val newRequest = originalRequest.newBuilder().apply {
            if (!accessToken.isNullOrBlank()) {
                header("Authorization", "Bearer $accessToken")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
