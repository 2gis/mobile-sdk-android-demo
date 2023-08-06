package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import ru.dgis.sdk.Duration
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityMapFpsBinding
import ru.dgis.sdk.demo.databinding.ActivityMapFpsSettingsBinding
import ru.dgis.sdk.hours
import ru.dgis.sdk.map.CameraMoveController
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Fps
import kotlin.math.sin

/**
 * Sample activity for demonstration MapView's FPS limiting possibilities
 * For further info check [SDK documentation](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.MapView)
 *
 * Demonstration: open activity, slide up the bottom sheet, push "Start map moves button"
 * Map will start instant updates, you can adjust limits in EditText fields to see the effect of increased / decreased FPS
 */
class MapFpsActivity : AppCompatActivity() {

    private val binding: ActivityMapFpsBinding by lazy {
        ActivityMapFpsBinding.inflate(
            layoutInflater
        )
    }
    private val mapView by lazy { binding.mapView }
    private val closeables = mutableListOf<AutoCloseable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.addSettingsLayout {
            addView(prepareSettingsListView())
        }

        mapView.getMapAsync {
            closeables.add(
                binding.mapView.fpsChannel.connect {
                    binding.fpsControl.text = it.toString()
                }
            )
        }
    }

    override fun onDestroy() {
        closeables.forEach {
            it.close()
        }
        closeables.clear()
        super.onDestroy()
    }

    private fun prepareSettingsListView(): View {
        return ActivityMapFpsSettingsBinding.inflate(layoutInflater).apply {
            maxFpsSetter.setText(mapView.maxFps?.value?.toString() ?: "")
            maxFpsSetter.doAfterTextChanged {
                if (it.toString() == "") {
                    mapView.maxFps = null
                }
                try {
                    mapView.maxFps = Fps(it.toString().toInt())
                } catch (_: NumberFormatException) {
                }
            }

            powerSaveFpsSetter.setText(mapView.maxFps?.value?.toString() ?: "")
            powerSaveFpsSetter.doAfterTextChanged {
                if (it.toString() == "") {
                    mapView.powerSavingMaxFps = null
                }
                try {
                    mapView.powerSavingMaxFps = Fps(it.toString().toInt())
                } catch (_: NumberFormatException) {
                }
            }

            startButton.setOnClickListener {
                mapView.getMapAsync { map ->
                    map.camera.use { camera ->
                        camera.move(FpsMoveController(camera.position))
                    }
                }
            }
        }.root
    }
}

private class FpsMoveController(private val initialPosition: CameraPosition) :
    CameraMoveController {
    override fun position(time: Duration): CameraPosition {
        val offset = sin(time.inMilliseconds * 0.001).toFloat()
        return initialPosition.run {
            copy(zoom = zoom.copy(zoom.value + offset))
        }
    }

    override fun animationTime(): Duration {
        return 100.hours
    }
}
