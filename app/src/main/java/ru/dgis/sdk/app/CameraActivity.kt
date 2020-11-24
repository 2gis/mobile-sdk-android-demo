package ru.dgis.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.sdk.Duration
import ru.dgis.sdk.seconds
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map


private data class MovePoint(
    val position: CameraPosition,
    val duration: Duration = 4.seconds,
    val animationType: CameraAnimationType = CameraAnimationType.DEFAULT
)

class CameraActivity: AppCompatActivity() {
    private lateinit var sdkContext : Context
    private var map: Map? = null

    private val points = listOf(
        MovePoint( // Театральная площадь, Большой театр
            position = CameraPosition(
                point = GeoPoint(Arcdegree(55.759909), Arcdegree(37.618806)),
                zoom = Zoom(15.0f),
                tilt = Tilt(15.0f),
                bearing = Arcdegree(115.0)
            ),
            animationType = CameraAnimationType.LINEAR
        ),
        MovePoint(
            position = CameraPosition(
                point = GeoPoint(Arcdegree(55.759909), Arcdegree(37.618806)),
                zoom = Zoom(16.0f),
                tilt = Tilt(15.0f),
                bearing = Arcdegree(0.0)
            ),
            animationType = CameraAnimationType.DEFAULT
        ),
        MovePoint(
            position = CameraPosition( // Дом на Котельнической
                point = GeoPoint(Arcdegree(55.746962), Arcdegree(37.643073)),
                zoom = Zoom(16.0f),
                tilt = Tilt(55.0f),
                bearing = Arcdegree(0.0)
            ),
            animationType = CameraAnimationType.SHOW_BOTH_POSITIONS,
            duration = 9.seconds
        ),
        MovePoint(
            position = CameraPosition(
                point = GeoPoint(Arcdegree(55.746962), Arcdegree(37.643073)),
                zoom = Zoom(16.5f),
                tilt = Tilt(45.0f),
                bearing = Arcdegree(40.0)
            ),
            animationType = CameraAnimationType.LINEAR
        ),
        MovePoint( // Кремль
            position = CameraPosition(
                point = GeoPoint(Arcdegree(55.752425), Arcdegree(37.613983)),
                zoom = Zoom(16.0f),
                tilt = Tilt(25.0f),
                bearing = Arcdegree(85.0)
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = initializeDGis(applicationContext)
        setContentView(R.layout.activity_camera)

        val trigger = findViewById<FloatingActionButton>(R.id.startMoving)?.apply {
            isEnabled = false

            setOnClickListener {
                moveTo(0)
            }
        }

        val mapContainer = findViewById<LinearLayout>(R.id.map_container)
        val mapOptions = MapOptions().apply {
            position = points.last().position
            // source = DgisSourceCreator.createOnlineDgisSource(sdkContext)
        }
        val mapView = MapView(this, mapOptions).apply {
            lifecycle.addObserver(this)

            getMapAsync { map ->
                this@CameraActivity.map = map
                trigger?.isEnabled = true
            }
        }
        mapContainer.addView(mapView)
    }

    private fun moveTo(idx: Int) {
        points.getOrNull(idx)?.let { point ->
            map
                ?.camera
                ?.move(point.position, point.duration, point.animationType)
                ?.onResult {
                    moveTo(idx + 1)
                }
        }
    }
}