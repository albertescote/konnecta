package com.konnecta.app

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import timber.log.Timber

class KonnectaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        OneSignal.Debug.logLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.NONE
    }
}
