package ru.dgis.sdk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dgis.sdk.Duration
import ru.dgis.sdk.await
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.databinding.ActivityCameraMovesBinding
import ru.dgis.sdk.map.CameraAnimatedMoveResult
import ru.dgis.sdk.map.CameraAnimationType
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.seconds

/**
 * Sample activity for camera's move demonstration.
 * See it as an tour over some Dubai's most known sights
 *
 * See also: [Controlling the camera](https://docs.2gis.com/en/android/sdk/examples/map#nav-lvl1--Controlling_the_camera)
 *
 */
class CameraMovesActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraMovesBinding.inflate(layoutInflater) }
    private val mapView by lazy { binding.mapView }
    private val cameraPoints = LoopedPoints(predefinedPoints)
    private lateinit var map: Map

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        mapView.getMapAsync {
            this.map = it
            lifecycleScope.launch {
                startMoves(this)
            }
        }
    }

    private suspend fun startMoves(scope: CoroutineScope) {
        Toast.makeText(this@CameraMovesActivity, "Launching camera moves...", Toast.LENGTH_SHORT).show()
        delay(1000)
        cameraPoints.forEach { point ->
            scope.launch(Dispatchers.Main) {
                when (move(point)) {
                    CameraAnimatedMoveResult.CANCELLED_BY_APPLICATION -> {}
                    CameraAnimatedMoveResult.FINISHED -> {}
                    CameraAnimatedMoveResult.CANCELLED_BY_EVENT -> {
                        Toast.makeText(
                            this@CameraMovesActivity,
                            "Move have been interrupted :(",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.cancel()
                    }
                }
            }.join()
        }
    }

    private suspend fun move(point: MovePoint): CameraAnimatedMoveResult {
        return lifecycleScope.async {
            map
                .camera
                .move(point.position, point.duration, point.animationType)
                .await()
        }.await()
    }
}

/**
 * Helper class to encapsulate parameters related to camera's move
 * Useful links:
 * - [CameraPosition](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.CameraPosition)
 * - [CameraAnimationType](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.CameraAnimationType)
 */
private data class MovePoint(
    val position: CameraPosition,
    val duration: Duration = 4.seconds,
    val animationType: CameraAnimationType = CameraAnimationType.DEFAULT
)

/**
 * Helper class to create infinite looped list of predefinedPoints
 */
private class LoopedPoints(private val points: List<MovePoint>) : Iterable<MovePoint> {
    override fun iterator(): Iterator<MovePoint> {
        return LoopedPointsIterator(points)
    }
}

private class LoopedPointsIterator(private val points: List<MovePoint>) : Iterator<MovePoint> {
    var position = 0

    override fun hasNext(): Boolean {
        return true
    }

    override fun next(): MovePoint {
        val next = position
        position++
        if (position == points.size) {
            position = 0
        }

        return points[next]
    }
}

private val predefinedPoints = listOf(
    MovePoint( // Burj Khalifa area
        position = CameraPosition(
            point = GeoPoint(25.195156740425006, 55.27509422041476),
            zoom = Zoom(16.856598f),
            tilt = Tilt(50.0f),
            bearing = Bearing(19.0)
        ),
        duration = 8.seconds
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.198019466636268, 55.27326302602887),
            zoom = Zoom(16.856598f),
            tilt = Tilt(50.0f),
            bearing = Bearing(151.0)
        )
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.196089399669532, 55.277449786663055),
            zoom = Zoom(16.856598f),
            tilt = Tilt(50.0f),
            bearing = Bearing(279.0)
        )
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.195156740425006, 55.27509422041476),
            zoom = Zoom(16.856598f),
            tilt = Tilt(50.0f),
            bearing = Bearing(19.0)
        )
    ),
    MovePoint( // Burj al Arab
        position = CameraPosition(
            point = GeoPoint(25.141770980652794, 55.1848682295531),
            zoom = Zoom(17.36f),
            tilt = Tilt(50.0f),
            bearing = Bearing(308.0)
        ),
        duration = 8.seconds,
        animationType = CameraAnimationType.SHOW_BOTH_POSITIONS
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.14142232093819, 55.18481366336346),
            zoom = Zoom(17.36f),
            tilt = Tilt(50.0f),
            bearing = Bearing(212.8)
        ),
        duration = 3.seconds,
        animationType = CameraAnimationType.LINEAR
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.141178296484654, 55.18569124862552),
            zoom = Zoom(17.36f),
            tilt = Tilt(50.0f),
            bearing = Bearing(126.2)
        ),
        duration = 3.seconds,
        animationType = CameraAnimationType.LINEAR
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.14185550866867, 55.18535630777478),
            zoom = Zoom(17.36f),
            tilt = Tilt(50.0f),
            bearing = Bearing(32.0)
        ),
        duration = 3.seconds,
        animationType = CameraAnimationType.LINEAR
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.141770980652794, 55.1848682295531),
            zoom = Zoom(17.36f),
            tilt = Tilt(50.0f),
            bearing = Bearing(308.0)
        ),
        duration = 3.seconds,
        animationType = CameraAnimationType.LINEAR
    ),
    MovePoint( // Dubai Marina
        position = CameraPosition(
            point = GeoPoint(25.08599463003054, 55.146588580682874),
            zoom = Zoom(17.0f),
            tilt = Tilt(60.0f),
            bearing = Bearing(357.0)
        ),
        duration = 10.seconds,
        animationType = CameraAnimationType.SHOW_BOTH_POSITIONS
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.08984599462832, 55.147762801498175),
            zoom = Zoom(16.857f),
            tilt = Tilt(60.0f),
            bearing = Bearing(170.0)
        ),
        duration = 6.seconds
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.083035293801373, 55.14531117863953),
            zoom = Zoom(16.857f),
            tilt = Tilt(60.0f),
            bearing = Bearing(222.0)
        ),
        duration = 3.seconds
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.068350513840016, 55.12947675772011),
            zoom = Zoom(16.857f),
            tilt = Tilt(60.0f),
            bearing = Bearing(222.0)
        ),
        duration = 6.seconds
    ),
    MovePoint( // Ain Dubai
        position = CameraPosition(
            point = GeoPoint(25.079562766951582, 55.121345054358244),
            zoom = Zoom(16.858f),
            tilt = Tilt(60.0f),
            bearing = Bearing(260.0)
        ),
        duration = 4.seconds,
        animationType = CameraAnimationType.LINEAR
    ),
    MovePoint(
        position = CameraPosition(
            point = GeoPoint(25.08061876199403, 55.127019099891186),
            zoom = Zoom(16.858f),
            tilt = Tilt(60.0f),
            bearing = Bearing(260.0)
        ),
        duration = 4.seconds
    ),
    MovePoint( // End: Panoramic Dubai
        position = CameraPosition(
            point = GeoPoint(25.166905317928993, 55.24470638483763),
            zoom = Zoom(10.0f),
            tilt = Tilt(0.0f),
            bearing = Bearing(10.0)
        ),
        duration = 6.seconds,
        animationType = CameraAnimationType.SHOW_BOTH_POSITIONS
    )
)
