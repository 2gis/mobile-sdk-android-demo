package ru.dgis.sdk.demo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import ru.dgis.sdk.positioning.DesiredAccuracy
import ru.dgis.sdk.positioning.LocationChangeListener
import ru.dgis.sdk.positioning.LocationSource


class CustomLocationManager(private val applicationContext: Context): LocationSource {
    private var fuseClient: FusedLocationProviderClient? = null
    private var fuseCallback: LocationCallback? = null
    private var sdkListener: LocationChangeListener? = null
    private var lastRequestedAccuracy: DesiredAccuracy = DesiredAccuracy.LOW

    override fun activate(listener: LocationChangeListener) {
        deactivate()

        sdkListener = listener

        if (checkPermission()) {
            // todo: request here
            return
        }
        fuseClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        setDesiredAccuracy(lastRequestedAccuracy)
    }

    override fun deactivate() {
        fuseClient?.let {
            fuseCallback?.let(it::removeLocationUpdates)
        }
        fuseCallback = null
        fuseClient = null
        sdkListener = null
    }

    override fun setDesiredAccuracy(accuracy: DesiredAccuracy) {
        if (accuracy == lastRequestedAccuracy && fuseCallback != null)
            return

        lastRequestedAccuracy = accuracy

        val client = fuseClient ?: return
        val listener = sdkListener ?: return

        fuseCallback?.let(client::removeLocationUpdates)

        val (newPriority, interval) = when (accuracy) {
            DesiredAccuracy.HIGH -> Pair(Priority.PRIORITY_HIGH_ACCURACY, 100L)
            DesiredAccuracy.MEDIUM -> Pair(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            DesiredAccuracy.LOW -> Pair(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 5000L)
        }
        val request = LocationRequest.Builder(newPriority, interval)
            .build()
        val callback = (object: LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                listener.onLocationChanged(result.locations.toTypedArray())
            }
        }).also {
            fuseCallback = it
        }

        if (checkPermission()) {
            return
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        listener.onAvailabilityChanged(true)
    }

    private fun checkPermission(): Boolean = ActivityCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        applicationContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
}