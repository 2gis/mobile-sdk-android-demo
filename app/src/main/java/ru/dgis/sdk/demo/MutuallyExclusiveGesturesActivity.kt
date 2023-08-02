package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityMutuallyExclusiveGesturesSettingsBinding
import ru.dgis.sdk.map.Gesture
import java.util.EnumSet

/**
 * Sample activity for demonstration of maps's Gesture Manager possibilities in terms of setting rules for gestures
 *
 * For further details check [SDK Documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GestureManager#nav-lvl1--setMutuallyExclusiveGestures)
 */
class MutuallyExclusiveGesturesActivity : AppCompatActivity() {

    private val binding by lazy { ActivityGesturesBinding.inflate(layoutInflater) }
    private val settingsBinding by lazy { ActivityMutuallyExclusiveGesturesSettingsBinding.inflate(layoutInflater) }
    private val mapView by lazy { binding.mapView }
    private val gestureManager by lazy { mapView.gestureManager }

    private var checkedGestures = EnumSet.noneOf(Gesture::class.java)
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

    private fun initSettings() {
        val gestureManager = this.gestureManager!!
        settingsBinding.apply {
            val checkboxes = listOf(
                tiltCheckbox,
                rotationCheckbox,
                scailingCheckbox,
                shiftCheckbox,
                multiTouchShiftCheckbox
            )
            checkboxes.forEach { checkBox ->
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        checkedGestures.add(ru.dgis.sdk.map.Gesture.entries.first { it.name == checkBox.tag })
                    } else {
                        checkedGestures.remove(ru.dgis.sdk.map.Gesture.entries.first { it.name == checkBox.tag })
                    }
                }
            }

            applyRuleButton.setOnClickListener {
                gestureManager.setMutuallyExclusiveGestures(listOf(checkedGestures))
                checkedGestures = EnumSet.noneOf(Gesture::class.java)
                checkboxes.forEach {
                    it.isChecked = false
                }
            }

            cleanRulesButton.setOnClickListener {
                gestureManager.setMutuallyExclusiveGestures(listOf())
                checkedGestures = EnumSet.noneOf(Gesture::class.java)
                checkboxes.forEach {
                    it.isChecked = false
                }
            }
        }
    }
}
