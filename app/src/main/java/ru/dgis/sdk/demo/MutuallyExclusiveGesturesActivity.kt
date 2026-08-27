package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.awaitMapControllerOrShowError
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityMutuallyExclusiveGesturesSettingsBinding
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.GestureManager
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.TransformGesture
import ru.dgis.sdk.map.Zoom
import java.util.EnumSet

/**
 * Sample activity for demonstration of maps's Gesture Manager possibilities in terms of setting rules for gestures
 *
 * For further details check [SDK Documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GestureManager#nav-lvl1--setMutuallyExclusiveGestures)
 */
class MutuallyExclusiveGesturesActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGesturesBinding.inflate(layoutInflater) }
    private val settingsBinding by lazy { ActivityMutuallyExclusiveGesturesSettingsBinding.inflate(layoutInflater) }
    private lateinit var gestureManager: GestureManager
    private val mapOwner by demoMapOwner(
        CameraPosition(
            point = GeoPoint(25.09608, 55.132429),
            zoom = Zoom(13.5f),
            tilt = Tilt(25f)
        )
    )

    private var checkedGestures = EnumSet.noneOf(TransformGesture::class.java)
    private var addedRules = mutableListOf<EnumSet<TransformGesture>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapView = binding.mapContainer.attachMapView(mapOwner.mapViewModel)
        binding.addSettingsLayout(mapView) {
            addView(settingsBinding.root)
        }

        /**
         * Using hack here to delay settings initialization until map is ready and gestureManager is not null for sure
         */
        lifecycleScope.launch {
            val controller = awaitMapControllerOrShowError(mapOwner.mapViewModel) ?: return@launch
            gestureManager = checkNotNull(
                controller.gestureRecognizer.gestureManager
            )
            initSettings()
        }
    }

    private fun initSettings() {
        val gestureManager = this.gestureManager
        settingsBinding.apply {
            val checkboxes = listOf(
                tiltCheckbox,
                rotationCheckbox,
                scailingCheckbox,
                multiTouchShiftCheckbox
            )
            checkboxes.forEach { checkBox ->
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkedGestures.add(TransformGesture.entries.first { it.name == checkBox.tag })
                    } else {
                        checkedGestures.remove(TransformGesture.entries.first { it.name == checkBox.tag })
                    }
                }
            }

            applyRuleButton.setOnClickListener {
                addedRules += listOf(checkedGestures)
                gestureManager.setMutuallyExclusiveGestures(addedRules)
                checkedGestures = EnumSet.noneOf(TransformGesture::class.java)
                checkboxes.forEach {
                    it.isChecked = false
                }
            }

            cleanRulesButton.setOnClickListener {
                gestureManager.setMutuallyExclusiveGestures(listOf())
                addedRules.clear()
                checkedGestures = EnumSet.noneOf(TransformGesture::class.java)
                checkboxes.forEach {
                    it.isChecked = false
                }
            }
        }
    }
}
