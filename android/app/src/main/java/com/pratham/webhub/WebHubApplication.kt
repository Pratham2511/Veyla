package com.pratham.webhub

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WebHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize any app-wide components here (e.g., Timber, WorkManager, etc.)
    }
}
