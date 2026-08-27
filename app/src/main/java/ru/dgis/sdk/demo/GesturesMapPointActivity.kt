package ru.dgis.sdk.demo

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.awaitMapControllerOrShowError
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityGesturesMapPointSettingsBinding
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.GestureActionEventCenter
import ru.dgis.sdk.map.GestureActionMapPosition
import ru.dgis.sdk.map.GestureActionPoint
import ru.dgis.sdk.map.GestureManager
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom

/**
 * Sample activity for demonstration of maps's Gesture Manager possibilities in terms of setting map point, which gestures will be relative to
 * It's hard to test these cases on an emulator since all multitouch gestures will use center of screen as a center of segment between 2 touch points,
 * so we recommend to use real smartphone here.
 *
 * For further details check [SDK Documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GestureManager#nav-lvl1--setSettingsAboutMapPositionPoint)
 */
class GesturesMapPointActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGesturesBinding.inflate(layoutInflater) }
    private lateinit var gestureManager: GestureManager
    private val settingsBinding by lazy { prepareSettingsBinding() }
    private val mapOwner by demoMapOwner(
        CameraPosition(
            point = GeoPoint(25.09608, 55.132429),
            zoom = Zoom(13.5f),
            tilt = Tilt(25f)
        )
    )
    private var rotationCenterValue: String = "MAP_POSITION"
    private var scalingCenterValue: String = "MAP_POSITION"
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

    private fun prepareSettingsBinding(): ActivityGesturesMapPointSettingsBinding {
        return ActivityGesturesMapPointSettingsBinding.inflate(layoutInflater).apply {
            val options = resources.getStringArray(R.array.events_processing_settings)
            val adapter = ArrayAdapter(this@GesturesMapPointActivity, R.layout.dropdown_item, options)
            rotationCenterTextView.setAdapter(adapter)
            scailingCenterTextView.setAdapter(adapter)
        }
    }

    private fun initSettings() {
        val gestureManager = this.gestureManager

        settingsBinding.rotationCenterTextView.setText(rotationCenterValue, false)
        settingsBinding.rotationCenterTextView.doAfterTextChanged { editable ->
            val name = editable?.toString() ?: return@doAfterTextChanged
            rotationCenterValue = name
            gestureManager.rotationSettings.rotationCenter = when (name) {
                "EVENT_CENTER" -> GestureActionPoint(GestureActionEventCenter())
                else -> GestureActionPoint(GestureActionMapPosition())
            }
        }

        settingsBinding.scailingCenterTextView.setText(scalingCenterValue, false)
        settingsBinding.scailingCenterTextView.doAfterTextChanged { editable ->
            val name = editable?.toString() ?: return@doAfterTextChanged
            scalingCenterValue = name
            gestureManager.scalingSettings.scalingCenter = when (name) {
                "EVENT_CENTER" -> GestureActionPoint(GestureActionEventCenter())
                else -> GestureActionPoint(GestureActionMapPosition())
            }
        }
    }
}
