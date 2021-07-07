package ru.dgis.sdk.demo

import android.app.Application
import ru.dgis.sdk.Context
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import ru.dgis.sdk.positioning.registerPlatformMagneticSource


class Application : Application() {
    lateinit var sdkContext: Context

    override fun onCreate() {
        super.onCreate()

        sdkContext = initializeDGis(this)
    }

    fun registerServices() {
        val compassSource = CustomCompassManager(applicationContext)
        registerPlatformMagneticSource(sdkContext, compassSource)

        val locationSource = CustomLocationManager(applicationContext)
        registerPlatformLocationSource(sdkContext, locationSource)
    }
}