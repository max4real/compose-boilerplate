package com.max4real.compose_boilerplate.di

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.max4real.compose_boilerplate.di.refresh.AuthInterceptor
import com.max4real.compose_boilerplate.di.refresh.AuthenticatedClient
import com.max4real.compose_boilerplate.di.refresh.AuthenticatedRetrofit
import com.max4real.compose_boilerplate.di.refresh.ForbiddenTokenRefreshInterceptor
import com.max4real.compose_boilerplate.di.refresh.RefreshApi
import com.max4real.compose_boilerplate.di.refresh.RefreshClient
import com.max4real.compose_boilerplate.di.refresh.RefreshRetrofit
import com.max4real.compose_boilerplate.di.refresh.TokenAuthenticator
import com.max4real.compose_boilerplate.shared.config.AppEnvironment
import com.max4real.compose_boilerplate.shared.managers.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalModule {

    @Provides
    fun baseUrl(): String {
        return AppEnvironment.API_BASE_URL
    }

    // ---------------------------
    // Refresh client
    // No AuthInterceptor
    // No TokenAuthenticator
    // Used only for auth/refresh
    // ---------------------------

    @RefreshClient
    @Provides
    @Singleton
    fun provideRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @RefreshRetrofit
    @Provides
    @Singleton
    fun provideRefreshRetrofit(
        baseUrl: String,
        @RefreshClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRefreshApi(
        @RefreshRetrofit retrofit: Retrofit
    ): RefreshApi {
        return retrofit.create(RefreshApi::class.java)
    }

    // ---------------------------
    // Normal authenticated client
    // Adds access token
    // Refreshes token on 403
    // ---------------------------

    @AuthenticatedClient
    @Provides
    @Singleton
    fun provideAuthenticatedOkHttpClient(
        authInterceptor: AuthInterceptor,
        forbiddenTokenRefreshInterceptor: ForbiddenTokenRefreshInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(AUTH_CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AUTH_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AUTH_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(forbiddenTokenRefreshInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @AuthenticatedRetrofit
    @Provides
    @Singleton
    fun provideAuthenticatedRetrofit(
        baseUrl: String,
        @AuthenticatedClient okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ---------------------------
    // Managers
    // ---------------------------

    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context
    ): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideApplication(
        @ApplicationContext app: Context
    ): BoilerplateApplication {
        return app as BoilerplateApplication
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    private const val AUTH_CONNECT_TIMEOUT_SECONDS = 30L
    private const val AUTH_READ_TIMEOUT_SECONDS = 120L
    private const val AUTH_WRITE_TIMEOUT_SECONDS = 120L
}
