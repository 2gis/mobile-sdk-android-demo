package ru.dgis.sdk.demo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import ru.dgis.sdk.positioning.MagneticChangeListener
import ru.dgis.sdk.positioning.MagneticHeadingSource

class CustomCompassManager(private val applicationContext: Context): MagneticHeadingSource {
    private var sdkListener: MagneticChangeListener? = null
    private var sensorManager: SensorManager? = null
    private var sensorListener: SensorEventListener? = null

    override fun activate(listener: MagneticChangeListener) {
        deactivate()

        sdkListener = listener

        val manager = (applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager).also {
            sensorManager = it
        }
        val callback = (object: SensorEventListener {
            private var accuracy: Int = 0
            private val accelerometerReading = FloatArray(3)
            private val magnetometerReading = FloatArray(3)
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            private var lastSin = 0.0
            private var lastCos = 1.0
            private var lastDegrees = 0.0

            override fun onSensorChanged(newEvent: SensorEvent?) {
                val event = newEvent ?: return
                val timestamp = System.currentTimeMillis()

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                }
                SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)

                val angle = orientationAngles[0].toDouble()
                val smoothingFactor = 0.9

                lastSin = smoothingFactor * lastSin + (1-smoothingFactor) * kotlin.math.sin(angle)
                lastCos = smoothingFactor * lastCos + (1-smoothingFactor) * kotlin.math.cos(angle)

                val degrees = Math.toDegrees(kotlin.math.atan2(lastSin, lastCos))
                if (kotlin.math.abs(lastDegrees - degrees) < 5.0)
                    return

                lastDegrees = degrees

                listener.onValueChanged(degrees.toFloat(), accuracy, timestamp)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    this.accuracy = accuracy
                }
            }
        }).also { sensorListener = it }

        val samplingPeriod = 100

        manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let { sensor ->
            manager.registerListener(
                callback,
                sensor,
                samplingPeriod)
        }
        manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
            manager.registerListener(
                callback,
                sensor,
                samplingPeriod)
        }

        listener.onAvailabilityChanged(true)
    }

    override fun deactivate() {
        sensorManager?.let {
            sensorListener?.let(it::unregisterListener)
        }
        sensorManager = null
        sensorListener = null
        sdkListener = null
    }
}