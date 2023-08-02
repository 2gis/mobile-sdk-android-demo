package ru.dgis.sdk.demo

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityGesturesMapPointSettingsBinding
import ru.dgis.sdk.map.EventsProcessingSettings
import ru.dgis.sdk.map.RotationCenter
import ru.dgis.sdk.map.ScalingCenter

/**
 * Sample activity for demonstration of maps's Gesture Manager possibilities in terms of setting map point, which gestures will be relative to
 * It's hard to test these cases on an emulator since all multitouch gestures will use center of screen as a center of segment between 2 touch points,
 * so we recommend to use real smartphone here.
 *
 * For further details check [SDK Documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GestureManager#nav-lvl1--setSettingsAboutMapPositionPoint)
 */
class GesturesMapPointActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGesturesBinding.inflate(layoutInflater) }
    private val mapView by lazy { binding.mapView }
    private val gestureManager by lazy { mapView.gestureManager }
    private val settingsBinding by lazy { prepareSettingsBinding() }
    private var eventProcessingSettings = EventsProcessingSettings(
        RotationCenter.MAP_POSITION,
        ScalingCenter.MAP_POSITION
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.addSettingsLayout().apply {
            settingsDrawerInnerLayout.addView(settingsBinding.root)
        }

        /**
         * Using hack here to delay settings initialization until map is ready and gestureManager is not null for sure
         */
        mapView.getMapAsync {
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
        val gestureManager = this.gestureManager!!

        settingsBinding.rotationCenterTextView.setText(eventProcessingSettings.rotationCenter.toString(), false)
        settingsBinding.rotationCenterTextView.doAfterTextChanged { editable ->
            val newRotationCenter = RotationCenter.entries.first { it.name == editable.toString() }
            eventProcessingSettings = eventProcessingSettings.copy(rotationCenter = newRotationCenter)
            gestureManager.setSettingsAboutMapPositionPoint(eventProcessingSettings)
        }

        settingsBinding.scailingCenterTextView.setText(eventProcessingSettings.scalingCenter.toString(), false)
        settingsBinding.scailingCenterTextView.doAfterTextChanged { editable ->
            val newScailingCenter = ScalingCenter.entries.first { it.name == editable.toString() }
            eventProcessingSettings = eventProcessingSettings.copy(scalingCenter = newScailingCenter)
            gestureManager.setSettingsAboutMapPositionPoint(eventProcessingSettings)
        }
    }
}
