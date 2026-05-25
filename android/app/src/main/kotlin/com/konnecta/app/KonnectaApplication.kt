package com.konnecta.app

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class KonnectaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // OneSignal initialization
        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        
        // Verbose logging for development (remove in production)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
    }
}
