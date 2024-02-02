package ru.dgis.sdk.demo.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.HandlerThread
import android.util.Log
import androidx.core.content.ContextCompat
import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.huawei.hms.location.LocationServices
import ru.dgis.sdk.positioning.DesiredAccuracy
import ru.dgis.sdk.positioning.LocationChangeListener
import ru.dgis.sdk.positioning.LocationService
import ru.dgis.sdk.positioning.LocationSource

/**
 * Sample implementation of [LocationSource](https://docs.2gis.com/en/android/sdk/reference/10.1/ru.dgis.sdk.positioning.LocationSource)
 * and [LocationService](https://docs.2gis.com/en/android/sdk/reference/10.1/ru.dgis.sdk.positioning.LocationService) interfaces based
 * Huawei location services.
 * In general, it's not needed to implement these interfaces manually if app targeting device with Google services: in this case
 * you can use our default implementation: [DefaultLocationSource](https://docs.2gis.com/en/android/sdk/reference/10.1/ru.dgis.sdk.positioning.DefaultLocationSource)
 *
 * Go to Application.kt to see example of detection HMS on device.
 */
class HMSLocationSource(context: Context) : LocationSource, LocationService {
    // Use application context to avoid memory leaks associated with activity contexts.
    // In general, we suggest to always pass applicationContext to constructor.
    private val appContext = context.applicationContext

    private var listener: LocationChangeListener? = null

    // Default accuracy level, can be adjusted at runtime.
    private var accuracy: DesiredAccuracy = DesiredAccuracy.MEDIUM

    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    private var _lastLocation: Location? = null

    // Background thread for handling location updates to avoid blocking the UI thread
    private var thread: HandlerThread? = null

    private var callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            synchronized(this@HMSLocationSource) {
                listener?.onLocationChanged(result.locations.toTypedArray())
                _lastLocation = result.lastLocation
            }
        }
    }

    override val lastLocation: Location?
        get() = _lastLocation

    // Activate the location source with a specified listener, ensuring single activation.
    // Using @Synchronized to ensure thread safety since method can be called from different threads
    @Synchronized
    override fun activate(listener: LocationChangeListener) {
        if (this.listener != null) {
            throw IllegalStateException("This source is already activated")
        }
        this.listener = listener
        this.listener?.onAvailabilityChanged(true)
        startLocationUpdates()
    }

    // Deactivate the location source, cleaning up resources and nullifying the listener
    // Using @Synchronized to ensure thread safety since method can be called from different threads
    @Synchronized
    override fun deactivate() {
        client.removeLocationUpdates(callback)
        thread?.let {
            it.quitSafely()
            try {
                it.join()
            } catch (e: InterruptedException) {
                Log.e("HMSLocationSource", "Error stopping thread", e)
            }
        }
        thread = null
        listener?.onAvailabilityChanged(false)
        listener = null
    }

    // Adjust the desired accuracy for location updates, restarting updates if necessary.
    // Using @Synchronized to ensure thread safety since method can be called from different threads
    @Synchronized
    override fun setDesiredAccuracy(accuracy: DesiredAccuracy) {
        if (this.accuracy == accuracy) {
            return
        }
        this.accuracy = accuracy
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("HMSLocationSource", "Location permissions are not granted")
            listener?.onAvailabilityChanged(false)
            return
        }

        // Configure the request based on the desired accuracy and interval.
        // Here are recommended settings from us, but you can adjust them if needed.
        val priority = when (accuracy) {
            DesiredAccuracy.HIGH -> LocationRequest.PRIORITY_HIGH_ACCURACY
            DesiredAccuracy.MEDIUM -> LocationRequest.PRIORITY_HIGH_ACCURACY
            DesiredAccuracy.LOW -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val interval = when (accuracy) {
            DesiredAccuracy.LOW -> 5000L
            DesiredAccuracy.MEDIUM -> 1000L
            DesiredAccuracy.HIGH -> 100L
        }
        val request = LocationRequest().apply {
            this.priority = priority
            this.interval = interval
        }

        val thread = thread ?: HandlerThread("DgisLocation").also {
            it.start()
            this.thread = it
        }

        client.removeLocationUpdates(callback)
        client.requestLocationUpdates(request, callback, thread.looper)
    }
}
