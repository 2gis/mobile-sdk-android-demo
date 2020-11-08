package ru.dgis.sdk.app

import android.location.Location
import ru.dgis.sdk.positioning.DesiredAccuracy
import ru.dgis.sdk.positioning.LocationChangeListener
import ru.dgis.sdk.positioning.LocationSource

class ManualLocationSource : LocationSource {
    private var listener: LocationChangeListener? = null

    private var isAvailable = true

    var location: Location = Location("")
        set(value) {
            field = value
            if (isAvailable) {
                listener?.onLocationChanged(arrayOf(value))
            }
        }

    override fun activate(listener: LocationChangeListener) {
        isAvailable = true
        listener.onAvailabilityChanged(true)
        listener.onLocationChanged(arrayOf(location))
        this.listener = listener
    }

    override fun deactivate() {
        isAvailable = false
        listener?.onAvailabilityChanged(false)
    }

    override fun setDesiredAccuracy(accuracy: DesiredAccuracy) {
    }

    fun reactivate() {
        isAvailable = true
        listener?.onAvailabilityChanged(true)
        listener?.onLocationChanged(arrayOf(location))
    }
}