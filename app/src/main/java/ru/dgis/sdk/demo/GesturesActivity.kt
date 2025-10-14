package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityGesturesBinding
import ru.dgis.sdk.demo.databinding.ActivityGesturesSettingsBinding
import ru.dgis.sdk.map.TransformGesture
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
    private val binding: ActivityGesturesBinding by lazy {
        ActivityGesturesBinding.inflate(
            layoutInflater
        )
    }
    private val mapView by lazy { binding.mapView }
    private val gestureManager by lazy { mapView.gestureManager }
    private val settingsBinding by lazy { prepareSettingsBinding() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.addSettingsLayout {
            addView(settingsBinding.root)
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
            settingsBinding.scailingSwitch to TransformGesture.SCALING,
            settingsBinding.shiftSwitch to TransformGesture.SHIFT,
            settingsBinding.multiTouchShiftSwitch to TransformGesture.MULTI_TOUCH_SHIFT,
            settingsBinding.tiltSwitch to TransformGesture.TILT,
            settingsBinding.rotationSwitch to TransformGesture.ROTATION
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
        val rotationSettings = gestureManager.rotationSettings.recognizeSettings
        settingsBinding.rotationAnglediffinscalingdegEditText.setText(
            rotationSettings.rotationThresholdInScaling.angleDiffDeg.toString()
        )
        settingsBinding.rotationDistancediffmmEditText.setText(
            rotationSettings.rotationThreshold.distanceDiffMm.toString()
        )
        settingsBinding.rotationAnglediffdegEditText.setText(
            rotationSettings.rotationThreshold.angleDiffDeg.toString()
        )
        settingsBinding.rotationDistancediffinscalingmmEditText.setText(
            rotationSettings.rotationThresholdInScaling.distanceDiffMm.toString()
        )

        settingsBinding.rotationAnglediffinscalingdegEditText.addTextChangedListener { text ->
            text?.toString()?.toFloatOrNull()?.let { value ->
                val rotationRecognizeSettings = gestureManager.rotationSettings.recognizeSettings
                gestureManager.rotationSettings.recognizeSettings = rotationRecognizeSettings.copy(
                    rotationThresholdInScaling = rotationRecognizeSettings.rotationThresholdInScaling.copy(
                        angleDiffDeg = value
                    )
                )
            }
        }
        settingsBinding.rotationDistancediffmmEditText.addTextChangedListener { text ->
            text?.toString()?.toFloatOrNull()?.let { value ->
                val rotationRecognizeSettings = gestureManager.rotationSettings.recognizeSettings
                gestureManager.rotationSettings.recognizeSettings = rotationRecognizeSettings.copy(
                    rotationThreshold = rotationRecognizeSettings.rotationThreshold.copy(
                        distanceDiffMm = value
                    )
                )
            }
        }
        settingsBinding.rotationAnglediffdegEditText.addTextChangedListener { text ->
            text?.toString()?.toFloatOrNull()?.let { value ->
                val rotationRecognizeSettings = gestureManager.rotationSettings.recognizeSettings
                gestureManager.rotationSettings.recognizeSettings = rotationRecognizeSettings.copy(
                    rotationThreshold = rotationRecognizeSettings.rotationThreshold.copy(
                        angleDiffDeg = value
                    )
                )
            }
        }
        settingsBinding.rotationDistancediffinscalingmmEditText.addTextChangedListener { text ->
            text?.toString()?.toFloatOrNull()?.let { value ->
                val rotationRecognizeSettings = gestureManager.rotationSettings.recognizeSettings
                gestureManager.rotationSettings.recognizeSettings = rotationRecognizeSettings.copy(
                    rotationThresholdInScaling = rotationRecognizeSettings.rotationThresholdInScaling.copy(
                        distanceDiffMm = value
                    )
                )
            }
        }
    }

    private fun initTiltSettings() {
        val gestureManager = this.gestureManager!!
        val tiltSettings = gestureManager.tiltSettings.recognizeSettings
        settingsBinding.tiltHorizontalswervedegEditText.setText(
            tiltSettings.horizontalSwerveDeg.toString()
        )
        settingsBinding.tiltLenondegreemmEditText.setText(
            tiltSettings.lenOnDegreeMm.toString()
        )
        settingsBinding.tiltThresholdmmEditText.setText(
            tiltSettings.thresholdMm.toString()
        )
        settingsBinding.tiltVerticalswervedegEditText.setText(
            tiltSettings.verticalSwerveDeg.toString()
        )

        settingsBinding.tiltHorizontalswervedegEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.tiltSettings.recognizeSettings
                gestureManager.tiltSettings.recognizeSettings = cur.copy(horizontalSwerveDeg = v)
            }
        }
        settingsBinding.tiltLenondegreemmEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.tiltSettings.recognizeSettings
                gestureManager.tiltSettings.recognizeSettings = cur.copy(
                    lenOnDegreeMm = if (v == 0f) 1f else v
                )
            }
        }
        settingsBinding.tiltThresholdmmEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.tiltSettings.recognizeSettings
                gestureManager.tiltSettings.recognizeSettings = cur.copy(thresholdMm = v)
            }
        }
        settingsBinding.tiltVerticalswervedegEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.tiltSettings.recognizeSettings
                gestureManager.tiltSettings.recognizeSettings = cur.copy(verticalSwerveDeg = v)
            }
        }
    }

    private fun initScailingSettings() {
        val gestureManager = this.gestureManager!!
        val scalingSettings = gestureManager.scalingSettings.recognizeSettings
        settingsBinding.scailingScaleratiothresholdEditText.setText(
            scalingSettings.scaleRatioThreshold.toString()
        )
        settingsBinding.scailingScaleratiothresholdinrotationEditText.setText(
            scalingSettings.scaleRatioThresholdInRotation.toString()
        )

        settingsBinding.scailingScaleratiothresholdEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.scalingSettings.recognizeSettings
                gestureManager.scalingSettings.recognizeSettings = cur.copy(
                    scaleRatioThreshold = if (v < 0f) 0f else v
                )
            }
        }
        settingsBinding.scailingScaleratiothresholdinrotationEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.scalingSettings.recognizeSettings
                gestureManager.scalingSettings.recognizeSettings = cur.copy(
                    scaleRatioThresholdInRotation = if (v < 0f) 0f else v
                )
            }
        }
    }

    private fun initMultiTouchShiftSettings() {
        val gestureManager = this.gestureManager!!
        val multitouchShiftSettings = gestureManager.multitouchShiftSettings.recognizeSettings
        settingsBinding.multiTouchShiftThresholdmmEditText.setText(
            multitouchShiftSettings.multitouchShiftThresholdMm.toString()
        )

        settingsBinding.multiTouchShiftThresholdmmEditText.addTextChangedListener { t ->
            t?.toString()?.toFloatOrNull()?.let { v ->
                val cur = gestureManager.multitouchShiftSettings.recognizeSettings
                gestureManager.multitouchShiftSettings.recognizeSettings = cur.copy(
                    multitouchShiftThresholdMm = if (v < 0f) 0f else v
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> readSettingsPropertyByName(instance: Any, propertyName: String): T {
        val property =
            instance::class.members.first { it.name == propertyName } as KProperty1<Any, *>
        return property.get(instance) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> copySettingsByName(instance: T, newValues: Map<String, Any>): T {
        val clazz = instance::class
        val copyFunction = clazz.functions.first { it.name == "copy" }
        val args = copyFunction.parameters
            .filter { param -> newValues.keys.contains(param.name) }
            .map { param -> param to newValues[param.name] }

        return copyFunction.callBy(mapOf(copyFunction.instanceParameter!! to instance) + args) as? T
            ?: error("error")
    }
}
