package ru.dgis.sdk.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import ru.dgis.sdk.Context
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map


class GenericMapActivity : AppCompatActivity() {
    lateinit var sdkContext: Context
    lateinit var mapSource: MyLocationMapObjectSource

    private var map: Map? = null

    private lateinit var mapView: MapView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext

        setContentView(R.layout.activity_map_generic)

        mapView = findViewById<MapView>(R.id.mapView).also {
            it.getMapAsync(this::onMapReady)
            it.showApiVersionInCopyrightView = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        map?.close()
    }

    private fun onMapReady(map: Map) {
        this.map = map

        val gestureManager = checkNotNull(mapView.gestureManager)
        subscribeGestureSwitches(gestureManager)

        mapSource = MyLocationMapObjectSource(
            sdkContext,
            MyLocationDirectionBehaviour.FOLLOW_MAGNETIC_HEADING,
            createSmoothMyLocationController()
        )
        map.addSource(mapSource)
    }

    private fun subscribeGestureSwitches(gm: GestureManager) {
        val enabledGestures = gm.enabledGestures
        val options = listOf(
            Pair(R.id.rotationSwitch, Gesture.ROTATION),
            Pair(R.id.shiftSwitch, Gesture.SHIFT),
            Pair(R.id.scaleSwitch, Gesture.SCALING),
            Pair(R.id.tiltSwitch, Gesture.TILT),
        )

        options.forEach { (viewId, gesture) ->
            findViewById<SwitchCompat>(viewId).apply {
                isEnabled = true
                isChecked = enabledGestures.contains(gesture)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked)
                        gm.enableGesture(gesture)
                    else
                        gm.disableGesture(gesture)
                }
            }
        }
    }
}
