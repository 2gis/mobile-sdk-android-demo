package ru.dgis.sdk.demo

import android.app.Application
import com.huawei.hms.api.HuaweiApiAvailability
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.location.HMSLocationSource
import ru.dgis.sdk.positioning.DefaultLocationSource
import ru.dgis.sdk.positioning.LocationService
import ru.dgis.sdk.positioning.LocationSource
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import kotlin.properties.Delegates

class Application : Application() {
    lateinit var sdkContext: Context
    lateinit var locationSource: LocationSource
    private var hasHms by Delegates.notNull<Boolean>()

    override fun onCreate() {
        super.onCreate()

        sdkContext = initializeDGis(this)
        hasHms = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this) ==
            com.huawei.hms.api.ConnectionResult.SUCCESS
    }

    fun registerServices() {
        locationSource = if (hasHms) {
            HMSLocationSource(applicationContext)
        } else {
            DefaultLocationSource(
                applicationContext
            )
        }
        registerPlatformLocationSource(sdkContext, locationSource)
    }
}

val Application.sdkContext: Context
    get() = (this as ru.dgis.sdk.demo.Application).sdkContext

val Application.locationService: LocationService
    get() = (this as ru.dgis.sdk.demo.Application).locationSource as LocationService
