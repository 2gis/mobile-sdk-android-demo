package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityGesturesSettingsBinding
import ru.dgis.sdk.map.Gesture
import kotlin.reflect.KProperty1
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter

/**
 * Sample activity for demonstration of maps's Gesture Manager possibilities in terms of enabling / disabling gestures and modifying their settings
 * For further info check [SDK Documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GestureManager)
 *
 * Some reflection used here to reduce boilerplate code, see [readSettingsPropertyByName] and [copySettingsByName]
 * Lets see on example:
 *
 * 1. readSettingsPropertyByName() is equivalent for property accessor in data class, hence
 *     settings = TiltSettings(...)
 *     assertEquals(settings.lenOnDegreeMm, readSettingsPropertyByName(settings, "lenOnDegreeMm")) // will pass
 *
 * 2. copySettingsByName() is equivalent for copy() function of data class, hence
 *     settings = TiltSettings(...)
 *     assertEquals(settings.copy(lenOnDegreeMm = 42.0), copySettingsByName((settings), mapOf("lenOnDegreeMm" to 42.0)) // will pass
 *
 * Be sure to not use this in production code because reflection is slow. Use strict data class methods instead.
 */
class GesturesActivity : AppCompatActivity() {
    private val binding: ActivityGesturesBinding by lazy { ActivityGesturesBinding.inflate(layoutInflater) }
    private val mapView by lazy { binding.mapView }
    private val gestureManager by lazy { mapView.gestureManager }
    private val settingsBinding by lazy { prepareSettingsBinding() }
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
            initSwitches()
            initRotationSettings()
            initTiltSettings()
            initScailingSettings()
            initMultiTouchShiftSettings()
        }
    }

    private fun prepareSettingsBinding(): ActivityGesturesSettingsBinding {
        return ActivityGesturesSettingsBinding.inflate(layoutInflater)
    }

    private fun initSwitches() {
        val gestureManager = this.gestureManager!!

        mapOf(
            settingsBinding.scailingSwitch to Gesture.SCALING,
            settingsBinding.shiftSwitch to Gesture.SHIFT,
            settingsBinding.multiTouchShiftSwitch to Gesture.MULTI_TOUCH_SHIFT,
            settingsBinding.tiltSwitch to Gesture.TILT,
            settingsBinding.rotationSwitch to Gesture.ROTATION
        ).forEach { (switchMaterial, gesture) ->
            switchMaterial.isChecked = gestureManager.gestureEnabled(gesture)
            switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                /**
                 * Here is main functions for enabling / disabling gestures
                 */
                if (isChecked) {
                    gestureManager.enableGesture(gesture)
                } else {
                    gestureManager.disableGesture(gesture)
                }
            }
        }
    }

    private fun initRotationSettings() {
        val gestureManager = this.gestureManager!!
        val rotationSettings = gestureManager.rotationSettings

        mapOf(
            settingsBinding.rotationAnglediffinscalingdegEditText to "angleDiffInScalingDeg",
            settingsBinding.rotationDistancediffmmEditText to "distanceDiffMm",
            settingsBinding.rotationAnglediffdegEditText to "angleDiffDeg",
            settingsBinding.rotationDistancediffinscalingmmEditText to "distanceDiffInScalingMm"
        ).forEach { (editText, setting) ->
            val propertyValue = readSettingsPropertyByName<Float>(rotationSettings, setting)
            editText.apply {
                setText(propertyValue.toString())
                addTextChangedListener {
                    gestureManager.rotationSettings = copySettingsByName(
                        gestureManager.rotationSettings,
                        mapOf(setting to it.toString().toFloat())
                    )
                }
            }
        }
    }

    private fun initTiltSettings() {
        val gestureManager = this.gestureManager!!
        val tiltSettings = gestureManager.tiltSettings

        mapOf(
            settingsBinding.tiltHorizontalswervedegEditText to "horizontalSwerveDeg",
            settingsBinding.tiltLenondegreemmEditText to "lenOnDegreeMm",
            settingsBinding.tiltThresholdmmEditText to "thresholdMm",
            settingsBinding.tiltVerticalswervedegEditText to "verticalSwerveDeg"
        ).forEach { (editText, setting) ->
            val propertyValue = readSettingsPropertyByName<Float>(tiltSettings, setting)
            editText.apply {
                setText(propertyValue.toString())
                addTextChangedListener {
                    gestureManager.tiltSettings = copySettingsByName(
                        gestureManager.tiltSettings,
                        mapOf(setting to it.toString().toFloat())
                    )
                }
            }
        }
    }

    private fun initScailingSettings() {
        val gestureManager = this.gestureManager!!
        val scalingSettings = gestureManager.scalingSettings

        mapOf(
            settingsBinding.scailingScaleratiothresholdEditText to "scaleRatioThreshold",
            settingsBinding.scailingScaleratiothresholdinrotationEditText to "scaleRatioThresholdInRotation"
        ).forEach { (editText, setting) ->
            val propertyValue = readSettingsPropertyByName<Float>(scalingSettings, setting)
            editText.apply {
                setText(propertyValue.toString())
                addTextChangedListener {
                    gestureManager.scalingSettings = copySettingsByName(
                        gestureManager.scalingSettings,
                        mapOf(setting to it.toString().toFloat())
                    )
                }
            }
        }
    }
    private fun initMultiTouchShiftSettings() {
        val gestureManager = this.gestureManager!!
        val multiTouchShiftSettings = gestureManager.multitouchShiftSettings

        mapOf(
            settingsBinding.multiTouchShiftThresholdmmEditText to "thresholdMm"
        ).forEach { (editText, setting) ->
            val propertyValue = readSettingsPropertyByName<Float>(multiTouchShiftSettings, setting)
            editText.apply {
                setText(propertyValue.toString())
                addTextChangedListener {
                    gestureManager.multitouchShiftSettings = copySettingsByName(
                        gestureManager.multitouchShiftSettings,
                        mapOf(setting to it.toString().toFloat())
                    )
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> readSettingsPropertyByName(instance: Any, propertyName: String): T {
        val property = instance::class.members.first { it.name == propertyName } as KProperty1<Any, *>
        return property.get(instance) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> copySettingsByName(instance: T, newValues: Map<String, Any>): T {
        val clazz = instance::class
        val copyFunction = clazz.functions.first { it.name == "copy" }
        val args = copyFunction.parameters
            .filter { param -> newValues.keys.contains(param.name) }
            .map { param -> param to newValues[param.name] }

        return copyFunction.callBy(mapOf(copyFunction.instanceParameter!! to instance) + args) as? T ?: error("error")
    }
}
