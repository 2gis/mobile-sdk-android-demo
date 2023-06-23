package ru.dgis.sdk.demo

import android.app.Application
import ru.dgis.sdk.Context
import ru.dgis.sdk.positioning.DefaultLocationSource
import ru.dgis.sdk.positioning.registerPlatformLocationSource


class Application : Application() {
    lateinit var sdkContext: Context

    override fun onCreate() {
        super.onCreate()

        sdkContext = initializeDGis(this)
    }

    fun registerServices() {
        val locationSource = DefaultLocationSource(applicationContext)
        registerPlatformLocationSource(sdkContext, locationSource)
    }
}
