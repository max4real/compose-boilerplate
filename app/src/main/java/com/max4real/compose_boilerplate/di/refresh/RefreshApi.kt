package com.max4real.compose_boilerplate.di.refresh

import com.max4real.compose_boilerplate.shared.model.AuthTokenResponse
import com.max4real.compose_boilerplate.shared.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<AuthTokenResponse>>
}