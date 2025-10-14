package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.common.updateMapCopyrightPosition
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraChangeReason
import ru.dgis.sdk.map.GestureManager
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.MyLocationControllerSettings
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.TransformGesture
import ru.dgis.sdk.map.statefulChanges

class GenericMapActivity : AppCompatActivity() {
    private val sdkContext: Context by lazy { application.sdkContext }
    lateinit var mapSource: MyLocationMapObjectSource

    private val closeables = mutableListOf<AutoCloseable?>()

    private var map: Map? = null

    private lateinit var mapView: MapView
    private lateinit var root: View
    private lateinit var settingsDrawerInnerLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            MyLocationControllerSettings(BearingSource.MAGNETIC)
        )
        map.addSource(mapSource)

        val paddingChannel = map.camera
            .statefulChanges(CameraChangeReason.PADDING) { map.camera.padding }
        closeables.add(
            paddingChannel.connect { _ ->
                mapView.updateMapCopyrightPosition(root, settingsDrawerInnerLayout)
            }
        )
    }

    private fun subscribeGestureSwitches(gm: GestureManager) {
        val enabledGestures = gm.enabledGestures
        val options = listOf(
            Pair(R.id.rotationSwitch, TransformGesture.ROTATION),
            Pair(R.id.shiftSwitch, TransformGesture.SHIFT),
            Pair(R.id.scaleSwitch, TransformGesture.SCALING),
            Pair(R.id.tiltSwitch, TransformGesture.TILT)
        )

        options.forEach { (viewId, gesture) ->
            findViewById<SwitchCompat>(viewId).apply {
                isEnabled = true
                isChecked = enabledGestures.contains(gesture)

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        gm.enableGesture(gesture)
                    } else {
                        gm.disableGesture(gesture)
                    }
                }
            }
        }
    }
}
