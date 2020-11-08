package ru.dgis.sdk.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import ru.dgis.sdk.positioning.DesiredAccuracy
import ru.dgis.sdk.positioning.LocationChangeListener
import ru.dgis.sdk.positioning.LocationSource

class ManagerLocationSource(private val appContext: Context) : LocationSource {
    private val locationManager: LocationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationListener: LocationListener? = null

    override fun activate(listener: LocationChangeListener) {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                listener.onLocationChanged(arrayOf(location))
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {
                listener.onAvailabilityChanged(true)
            }

            override fun onProviderDisabled(provider: String) {
                listener.onAvailabilityChanged(false)
            }
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            locationListener!!
        )
    }

    override fun deactivate() {
        if (locationListener != null) locationManager.removeUpdates(locationListener!!)
        locationListener = null
    }

    override fun setDesiredAccuracy(accuracy: DesiredAccuracy) {}
}