package com.max4real.compose_boilerplate.di.refresh

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import javax.inject.Inject

class ForbiddenTokenRefreshInterceptor @Inject constructor(
    private val tokenRefreshService: TokenRefreshService
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != HTTP_FORBIDDEN || shouldSkipRefresh(request)) {
            return response
        }

//        mylog("ForbiddenTokenRefreshInterceptor: 403 received. Refreshing token.")
        val newTokens = tokenRefreshService.refreshTokensBlocking() ?: return response

        response.close()

        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer ${newTokens.accessToken}")
            .build()

//        mylog("ForbiddenTokenRefreshInterceptor: retrying original request.")
        return chain.proceed(newRequest)
    }

    private fun shouldSkipRefresh(request: Request): Boolean {
        val path = request.url.encodedPath

        return path.endsWith("/auth/refresh") ||
                path.endsWith("/auth/request-otp") ||
                path.endsWith("/auth/verify-otp") ||
                path.endsWith("/auth/register")
    }
}
