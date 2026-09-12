package com.max4real.compose_boilerplate.di

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BoilerplateApplication : Application(), ImageLoaderFactory {
    @Inject
    lateinit var appImageLoader: ImageLoader

    override fun newImageLoader(): ImageLoader {
        return appImageLoader
    }
}
