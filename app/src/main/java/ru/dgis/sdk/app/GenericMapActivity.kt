package ru.dgis.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SwitchCompat
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.positioning.*


private const val NIGHT_MODE_ATTR = "night_on"
private const val DIMENSION_ATTR = "is_2d"


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
        setupMapAttributes(map)

        mapSource = createMyLocationMapObjectSource(sdkContext, MyLocationDirectionBehaviour.FOLLOW_MAGNETIC_HEADING)!!
        map.addSource(mapSource)
    }

    private fun subscribeGestureSwitches(gm: GestureManager) {
        val enabledGestures = gm.enabledGestures()
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

    private fun setupMapAttributes(map: Map) {
        findViewById<SwitchCompat>(R.id.darkModeSwitch).apply {
            isEnabled = true

            setOnCheckedChangeListener { _, isChecked ->
                map.setStyleAttribute(NIGHT_MODE_ATTR, isChecked)
            }
        }

        findViewById<SwitchCompat>(R.id.flatModeSwitch).apply {
            isEnabled = true

            setOnCheckedChangeListener { _, isChecked ->
                map.setStyleAttribute(DIMENSION_ATTR, isChecked)
            }
        }
    }
}