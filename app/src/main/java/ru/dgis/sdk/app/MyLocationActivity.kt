package ru.dgis.sdk.app

import android.animation.ValueAnimator
import android.location.Location
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import kotlinx.android.synthetic.main.activity_my_location.*
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.positioning.*
import kotlin.math.pow

fun GeoPoint.toLocation(): Location {
    val location = Location("")
    location.latitude = latitude.value
    location.longitude = longitude.value
    return location
}

class MyLocationActivity : AppCompatActivity() {
    private lateinit var sdkContext: Context
    private var map: Map? = null
    private lateinit var locationProvider: ManualLocationSource
    private lateinit var myLocationSource: MyLocationMapObjectSource

    private lateinit var currentPoint: GeoPoint
    private var accuracy = 0.0f
    private var course = 0.0f
    private var hasDirection: Boolean = true
    private var distanceAnimator: ValueAnimator? = null

    inner class LongPressListener : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(event: MotionEvent) {
            for (i in 0 until event.pointerCount) {
                if (event.getPointerId(i) != 0)
                    continue
                val x = event.getX(i)
                val y = event.getY(i)
                map?.camera?.projection()?.screenToMap(ScreenPoint(x, y))?.let { toPoint ->
                    distanceAnimator?.cancel()
                    val fromPoint = currentPoint
                    val dLat = toPoint.latitude.value - fromPoint.latitude.value
                    val dLon = toPoint.longitude.value - fromPoint.longitude.value
                    val moveDistance = maxOf(dLat, dLon)
                    course = currentPoint.toLocation().bearingTo(toPoint.toLocation())
                    distanceAnimator = ValueAnimator.ofFloat(0.0f, moveDistance.toFloat()).apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        duration = 2000
                        addUpdateListener {
                            val p = animatedValue as Float
                            currentPoint = GeoPoint(
                                Arcdegree(fromPoint.latitude.value + p * dLat / moveDistance),
                                Arcdegree(fromPoint.longitude.value + p * dLon / moveDistance)
                            )
                            locationProvider.location = getLocation()
                        }
                        start()
                    }
                }
            }
            super.onLongPress(event)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = initializeDGis(applicationContext)

        locationProvider = ManualLocationSource()
        registerPlatformLocationSource(sdkContext, locationProvider)

        setContentView(R.layout.activity_my_location)

        lifecycle.addObserver(mapView)

        currentPoint = mapView.mapOptions.position.point

        mapView.getMapAsync {
            map = it
            myLocationSource = createMyLocationMapObjectSource(sdkContext,
                    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING
            )!!
            map!!.addSource(myLocationSource)
        }

        val gestureDetector = GestureDetectorCompat(this, LongPressListener())
        mapView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        seekBarMyLocationAccuracy.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                accuracy = 1.1f.pow(value) - 1.0f
                textViewMyLocationAccuracyValue.text = "%.1f".format(accuracy)
                locationProvider.location = getLocation()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        seekBarMyLocationAccuracy.progress = 40

        switchMyLocationDarkTheme.setOnCheckedChangeListener { _, value ->
            val textColor = ContextCompat.getColor(this,
                if (value) R.color.text_foreground_night else R.color.text_foreground_day
            )
            textViewMyLocationAccuracy.setTextColor(textColor)
            textViewMyLocationAccuracyValue.setTextColor(textColor)
            switchMyLocationDarkTheme.setTextColor(textColor)
            switchMyLocationPedestrianMode.setTextColor(textColor)
            switchMyLocationDirection.setTextColor(textColor)
            switchMyLocationAvailable.setTextColor(textColor)
            map?.setStyleAttribute("night_on", value)
        }

        switchMyLocationPedestrianMode.setOnCheckedChangeListener { _, value ->
            map?.setStyleAttribute("navigator_on", !value)
        }

        switchMyLocationDirection.setOnCheckedChangeListener { _, value ->
            hasDirection = value
            locationProvider.location = getLocation()
        }

        switchMyLocationAvailable.setOnCheckedChangeListener { _, value ->
            if (value) {
                locationProvider.reactivate()
            } else {
                locationProvider.deactivate()
            }
        }
    }

    private fun getLocation(): Location {
        val result = currentPoint.toLocation()
        result.accuracy = accuracy
        if (hasDirection) {
            result.bearing = course
        }
        return result
    }
}
