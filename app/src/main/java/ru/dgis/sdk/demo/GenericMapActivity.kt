package ru.dgis.sdk.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import ru.dgis.sdk.Context
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map


class GenericMapActivity : AppCompatActivity(), TouchEventsObserver {
    lateinit var sdkContext: Context
    lateinit var mapSource: MyLocationMapObjectSource

    private var map: Map? = null

    private val objectManager: MapObjectManager  by lazy { MapObjectManager(map!!) }

    private lateinit var mapView: MapView

    override fun onTap(point: ScreenPoint) {
        Toast.makeText(this, "tap point: $point", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext

        setContentView(R.layout.activity_map_generic)

        mapView = findViewById<MapView>(R.id.mapView).also {
            it.getMapAsync(this::onMapReady)
            it.showApiVersionInCopyrightView = true
            it.setTouchEventsObserver(this)
            it.addObjectTappedCallback {
                Log.e("TAPTAPTAP", "We tap something")
            }
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

        val circle1 = Circle(
            CircleOptions(
                position = GeoPoint(50.0, 50.0),
                radius = 10000.meter,
                color = Color(255, 0, 0)
            ))
        val circle2 = Circle(
            CircleOptions(
                position = GeoPoint(55.0, 55.0),
                radius = 10000.meter,
                color = Color(0, 255, 0)
            ))
        objectManager.addObjects(listOf(circle1, circle2))
        map.camera.setPadding(Padding(100, 100, 100, 100))
        map.camera.position = calcPosition(map.camera, listOf(circle1, circle2))
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
