package com.max4real.compose_boilerplate.di.refresh

import com.max4real.compose_boilerplate.shared.devices.DeviceInfoProvider
import com.max4real.compose_boilerplate.shared.managers.TokenManager
import com.max4real.compose_boilerplate.shared.model.AuthTokenResponse
import com.max4real.compose_boilerplate.shared.util.mylog
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshService @Inject constructor(
    private val tokenManager: TokenManager,
    private val refreshApi: RefreshApi,
    private val deviceInfoProvider: DeviceInfoProvider
) {

    @Synchronized
    fun refreshTokensBlocking(): AuthTokenResponse? {
        return runBlocking {
            val refreshToken = tokenManager.getRefreshToken()

            if (refreshToken.isNullOrBlank()) {
                mylog("TokenRefreshService: refresh skipped. Missing refresh token.")
                return@runBlocking null
            }

            try {
                val refreshResponse = refreshApi.refreshToken(
                    RefreshTokenRequest(
                        refreshToken = refreshToken,
                        device = deviceInfoProvider.currentDevice()
                    )
                )

                val body = refreshResponse.body()
                val newTokens = body?.data

                if (
                    refreshResponse.isSuccessful &&
                    body != null &&
                    body.success &&
                    newTokens != null
                ) {
                    tokenManager.saveTokens(
                        accessToken = newTokens.accessToken,
                        refreshToken = newTokens.refreshToken
                    )
                    mylog("TokenRefreshService: refresh success.")
                    newTokens
                } else {
                    // Only an explicit rejection of this refresh token justifies clearing the
                    // session. A 5xx, a maintenance window or a malformed body is transient —
                    // failing this one call is right, logging the user out is not.
                    val isRejected = refreshResponse.code() == HTTP_UNAUTHORIZED ||
                            refreshResponse.code() == HTTP_FORBIDDEN
                    mylog(
                        "TokenRefreshService: refresh failed code=${refreshResponse.code()} " +
                                "rejected=$isRejected"
                    )
                    if (isRejected) {
                        tokenManager.clearTokens()
                    }
                    null
                }
            } catch (e: IOException) {
                mylog("TokenRefreshService: refresh network failed: ${e.localizedMessage}")
                null
            } catch (e: Exception) {
                mylog("TokenRefreshService: refresh failed: ${e.localizedMessage}")
                null
            }
        }
    }
}
