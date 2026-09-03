package ru.dgis.sdk.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.awaitMapControllerOrShowError
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.databinding.ActivityMapFpsBinding
import ru.dgis.sdk.demo.databinding.ActivityMapFpsSettingsBinding
import ru.dgis.sdk.hours
import ru.dgis.sdk.map.CameraMoveController
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Fps
import ru.dgis.sdk.map.MapRenderer
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom
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
    private lateinit var renderer: MapRenderer
    private val closeables = mutableListOf<AutoCloseable>()
    private val mapOwner by demoMapOwner(
        CameraPosition(
            point = GeoPoint(25.09608, 55.132429),
            zoom = Zoom(13.5f),
            tilt = Tilt(25f)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapView = binding.mapContainer.attachMapView(mapOwner.mapViewModel)

        lifecycleScope.launch {
            val controller = awaitMapControllerOrShowError(mapOwner.mapViewModel) ?: return@launch
            renderer = controller.renderer
            binding.addSettingsLayout(mapView) {
                addView(prepareSettingsListView())
            }
            closeables.add(
                renderer.fpsChannel.connect {
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

    @SuppressLint("SetTextI18n")
    private fun prepareSettingsListView(): View {
        return ActivityMapFpsSettingsBinding.inflate(layoutInflater).apply {
            maxFpsSetter.setText(renderer.maxFps?.value?.toString().orEmpty())
            maxFpsSetter.doAfterTextChanged {
                val input = it.toString()
                val maxFps = if (input.isEmpty()) {
                    null
                } else {
                    input.toIntOrNull()?.let(::Fps) ?: return@doAfterTextChanged
                }
                if (::renderer.isInitialized) {
                    renderer.setMaxFps(maxFps, renderer.powerSavingMaxFps)
                }
            }

            powerSaveFpsSetter.setText(renderer.powerSavingMaxFps?.value?.toString().orEmpty())
            powerSaveFpsSetter.doAfterTextChanged {
                val input = it.toString()
                val powerSavingMaxFps = if (input.isEmpty()) {
                    null
                } else {
                    input.toIntOrNull()?.let(::Fps) ?: return@doAfterTextChanged
                }
                if (::renderer.isInitialized) {
                    renderer.setMaxFps(renderer.maxFps, powerSavingMaxFps)
                }
            }

            startButton.setOnClickListener {
                lifecycleScope.launch {
                    val map = awaitMapControllerOrShowError(mapOwner.mapViewModel)?.map ?: return@launch
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
