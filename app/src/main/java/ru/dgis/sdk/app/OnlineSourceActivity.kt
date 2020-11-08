package ru.dgis.sdk.app

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*

class OnlineSourceActivity : AppCompatActivity() {
    private lateinit var sdkContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_online_source)

        val mapContainer = findViewById<LinearLayout>(R.id.map_container)
        val mapOptions = MapOptions().apply {
            position = CameraPosition(
                GeoPoint(Arcdegree(55.740444), Arcdegree(37.619524)),
                Zoom(9.0f),
                Tilt(0.0f),
                Arcdegree(0.0)
            )
            source = DgisSourceCreator.createOnlineDgisSource(sdkContext)
        }
        val mapView = MapView(this, mapOptions)
        mapContainer.addView(mapView)
        lifecycle.addObserver(mapView)
    }
}