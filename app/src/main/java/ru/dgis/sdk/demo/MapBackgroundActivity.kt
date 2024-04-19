package ru.dgis.sdk.demo

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.databinding.ActivityMapBackgroundBinding
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraAnimatedMoveResult
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.MyLocationController
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.toBitmap
import ru.dgis.sdk.positioning.DesiredAccuracy

/**
 * Demonstrates how to create a snapshot of a map drawn offscreen. This is useful in scenarios
 * where a visual representation of a map is required without displaying the MapView in the UI hierarchy.
 *
 * Prerequisites:
 * - Location permissions must be granted at the start of the application.
 * - For emulator testing, ensure that a realistic geographical location is simulated.
 */
class MapBackgroundActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMapBackgroundBinding.inflate(layoutInflater) }
    private val locationSource by lazy {
        MyLocationMapObjectSource(
            application.sdkContext,
            MyLocationController(BearingSource.AUTO)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        (application as Application).locationSource.setDesiredAccuracy(DesiredAccuracy.HIGH)

        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                val screenHeight = resources.displayMetrics.heightPixels
                val screenWidth = resources.displayMetrics.widthPixels

                // Prepare an offscreen MapView inside a PopupWindow to capture the map snapshot.
                val mapView = MapView(this@MapBackgroundActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        screenWidth,
                        (screenHeight * 0.25).toInt()
                    )
                }
                val popup = PopupWindow(
                    mapView,
                    screenWidth,
                    (screenHeight * 0.25).toInt()
                ).also {
                    it.isClippingEnabled = false // this is crucial, allows overflow outside screen bounds
                }

                // Changing dispatcher here since it's mandatory to work with UI from main thread
                withContext(Dispatchers.Main.immediate) {
                    popup.showAtLocation(binding.main, Gravity.NO_GRAVITY, -screenWidth, -screenHeight)
                    mapView.getMapAsync { map ->
                        map.addSource(locationSource)
                        lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                takeSnapshotWithGeo(map, mapView, popup)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Takes a snapshot of the map centered on the last known location. If successful,
     * the image is set to an ImageView and the PopupWindow is dismissed.
     */
    private suspend fun takeSnapshotWithGeo(map: Map, mapView: MapView, popup: PopupWindow) {
        val timeoutMillis = 20000L
        val snapshotResult = withTimeoutOrNull(timeoutMillis) {
            var snapshotTaken = false
            while (!snapshotTaken) {
                application.locationService.lastLocation?.let { location ->
                    val cameraPosition = CameraPosition(GeoPoint(location.latitude, location.longitude), Zoom(16.2f))
                    map.camera.move(cameraPosition, Duration.ZERO).onResult { result ->
                        if (result == CameraAnimatedMoveResult.FINISHED) {
                            mapView.takeSnapshot().onResult { imgData ->
                                binding.mapSnapshotContainer.setImageBitmap(imgData.toBitmap())
                                popup.dismiss()
                                snapshotTaken = true
                            }
                        }
                    }
                }
                if (!snapshotTaken) delay(200)
            }
        }

        if (snapshotResult == null) {
            Log.w("MAP", "Failed to take a snapshot within the timeout period.")
        }
    }
}
