package ru.dgis.sdk.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Zoom

/*
Пример демонстриует управление камерой (Camera.move)
и получение ее состояния (Camera.position, Camera.state)
*/
class CameraActivity : AppCompatActivity() {
    private var map: Map? = null
    private val closeables = mutableListOf<AutoCloseable>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)
        findViewById<MapView>(R.id.mapView).let { mapView ->
            mapView.getMapAsync {
                this.map = it
                closeables.add(it)
                setupButtons()
                setupViews()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeables.forEach(AutoCloseable::close)
        closeables.clear()
    }

    private fun setupButtons() {
        val camera = map!!.camera

        fun addButton(buttonId: Int, position: CameraPosition) {
            findViewById<Button>(buttonId).setOnClickListener {
                camera.move(position, Duration.ofMilliseconds(1500)).onResult {
                    Toast.makeText(this, "Movement $it", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addButton(R.id.positionButton1, CameraPosition(
                GeoPoint(Arcdegree(55.751599), Arcdegree(37.620886)),
                zoom = Zoom(12.0f)
        ))
        addButton(R.id.positionButton2, CameraPosition(
                GeoPoint(Arcdegree(55.76201904911264), Arcdegree(37.577500781044364)),
                zoom = Zoom(15.5f)
        ))
        addButton(R.id.positionButton3, CameraPosition(
                GeoPoint(Arcdegree(55.76974051395856), Arcdegree(37.649533934891224)),
                zoom = Zoom(17.2f)
        ))
    }

    private fun setupViews() {
        val camera = map!!.camera

        val positionTextView = findViewById<TextView>(R.id.positionTextView)
        closeables.add(camera.position().connect {
            positionTextView.text = "position: %.3f, %.3f x %.1f".format(
                    it.point.latitude.value, it.point.longitude.value, it.zoom.value)
        })

        val stateTextView = findViewById<TextView>(R.id.stateTextView)
        closeables.add(camera.state().connect {
            stateTextView.text = "state: $it"
        })
    }
}