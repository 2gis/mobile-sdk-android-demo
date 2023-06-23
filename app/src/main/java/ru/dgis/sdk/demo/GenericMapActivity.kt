package ru.dgis.sdk.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.dgis.sdk.Context
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map


class GenericMapActivity : AppCompatActivity() {
    lateinit var sdkContext: Context
    lateinit var mapSource: MyLocationMapObjectSource

    private val closeables = mutableListOf<AutoCloseable?>()

    private var map: Map? = null

    private lateinit var mapView: MapView
    private lateinit var root: View
    private lateinit var settingsDrawerInnerLayout: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext

        setContentView(R.layout.activity_map_generic)

        root = findViewById(R.id.content)
        settingsDrawerInnerLayout = findViewById(R.id.settingsDrawerInnerLayout)
        mapView = findViewById<MapView>(R.id.mapView).also {
            it.getMapAsync(this::onMapReady)
            it.showApiVersionInCopyrightView = true
        }

        BottomSheetBehavior.from(findViewById(R.id.settingsDrawerInnerLayout)).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    mapView.updateMapCopyrightPosition(root, settingsDrawerInnerLayout)
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeables.forEach { it?.close() }
    }

    private fun onMapReady(map: Map) {
        this.map = map
        closeables.add(map)

        val gestureManager = checkNotNull(mapView.gestureManager)
        subscribeGestureSwitches(gestureManager)

        mapSource = MyLocationMapObjectSource(
            sdkContext,
            MyLocationDirectionBehaviour.FOLLOW_MAGNETIC_HEADING,
            createSmoothMyLocationController()
        )
        map.addSource(mapSource)

        closeables.add(map.camera.paddingChannel.connect { _ ->
            mapView.updateMapCopyrightPosition(root, settingsDrawerInnerLayout)
        })
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
