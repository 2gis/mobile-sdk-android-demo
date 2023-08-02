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
        mapView.getMapAsync {
            initSwitches(settingsBinding)
            initRotationSettings()
            initTiltSettings()
            initScailingSettings()
            initMultiTouchShiftSettings()
        }
    }

    private fun prepareSettingsBinding(): ActivityGesturesSettingsBinding {
        return ActivityGesturesSettingsBinding.inflate(layoutInflater)
    }

    private fun initSwitches(settingsView: ActivityGesturesSettingsBinding) {
        val gestureManager = this.gestureManager!!

        mapOf(
            settingsView.scailingSwitch to Gesture.SCALING,
            settingsView.shiftSwitch to Gesture.SHIFT,
            settingsView.multiTouchShiftSwitch to Gesture.MULTI_TOUCH_SHIFT,
            settingsView.tiltSwitch to Gesture.TILT,
            settingsView.rotationSwitch to Gesture.ROTATION
        ).forEach { (switchMaterial, gesture) ->
            switchMaterial.isChecked = gestureManager.gestureEnabled(gesture)
            switchMaterial.setOnCheckedChangeListener { _, isChecked ->
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
            settingsBinding.rotationAnglediffinscalingdeg to "angleDiffInScalingDeg",
            settingsBinding.rotationDistancediffmm to "distanceDiffMm",
            settingsBinding.rotationAnglediffdegEditText to "angleDiffDeg",
            settingsBinding.rotationDistancediffinscalingmm to "distanceDiffInScalingMm"
        ).forEach { (editText, setting) ->
            val propertyValue = readDataClassPropertyByName<Float>(rotationSettings, setting)
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
            settingsBinding.tiltHorizontalswervedeg to "horizontalSwerveDeg",
            settingsBinding.tiltLenondegreemm to "lenOnDegreeMm",
            settingsBinding.tiltThresholdmm to "thresholdMm",
            settingsBinding.tiltVerticalswervedeg to "verticalSwerveDeg"
        ).forEach { (editText, setting) ->
            val propertyValue = readDataClassPropertyByName<Float>(tiltSettings, setting)
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
            settingsBinding.scailingScaleratiothreshold to "scaleRatioThreshold",
            settingsBinding.scailingScaleratiothresholdinrotation to "scaleRatioThresholdInRotation"
        ).forEach { (editText, setting) ->
            val propertyValue = readDataClassPropertyByName<Float>(scalingSettings, setting)
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
            settingsBinding.multiTouchShiftThresholdmm to "thresholdMm"
        ).forEach { (editText, setting) ->
            val propertyValue = readDataClassPropertyByName<Float>(multiTouchShiftSettings, setting)
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
    private fun <T> readDataClassPropertyByName(instance: Any, propertyName: String): T {
        val property = instance::class.members.first { it.name == propertyName } as KProperty1<Any, *>
        return property.get(instance) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> copySettingsByName(instance: Any, newValues: Map<String, Any>): T {
        val clazz = instance::class
        val copyFunction = clazz.functions.first { it.name == "copy" }
        val args = copyFunction.parameters
            .filter { param -> newValues.keys.contains(param.name) }
            .map { param -> param to newValues[param.name] }

        return copyFunction.callBy(mapOf(copyFunction.instanceParameter!! to instance) + args) as? T ?: error("error")
    }
}
