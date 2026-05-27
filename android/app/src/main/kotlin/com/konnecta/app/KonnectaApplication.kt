package com.konnecta.app

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class KonnectaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        OneSignal.initWithContext(this, BuildConfig.ONESIGNAL_APP_ID)
        OneSignal.Debug.logLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.NONE
    }
}
