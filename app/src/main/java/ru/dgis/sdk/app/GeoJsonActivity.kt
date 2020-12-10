package ru.dgis.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.sdk.DGis
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

class GeoJsonActivity : AppCompatActivity() {
    private lateinit var sdkContext: Context
    private lateinit var mapView: MapView
    private var map: Map? = null
    private val geojsonDemo: GeoJsonDemo by lazy {
        map?.let { GeoJsonDemo(DGis.context(), it) }!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_geojson)

        val geojsonButton = findViewById<FloatingActionButton>(R.id.createGeoJsonButton).apply {
            isEnabled = false
        }
        val geojsonPopupMenu = PopupMenu(this, geojsonButton, Gravity.END, 0, R.style.GeometryPopupTheme)

        val mapContainer = findViewById<LinearLayout>(R.id.map_container)
        val mapOptions = MapOptions().apply {
            position = CameraPosition(
                GeoPoint(Arcdegree(54.85109), Arcdegree( 83.122475)),
                Zoom(12.0f),
                Tilt(0.0f),
                Arcdegree(0.0)
            )
            source = DgisSource.createOnlineDgisSource(sdkContext)
        }

        mapView = MapView(this, mapOptions)
        mapContainer.addView(mapView)
        lifecycle.addObserver(mapView)
        mapView.getMapAsync {
            map = it
            geojsonButton.isEnabled = true
        }

        geojsonPopupMenu.inflate(R.menu.geojson_menu)

        geojsonPopupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.drawSberLogo -> geojsonDemo.drawSberLogo()
                R.id.drawSomeGeoJsonObjects -> geojsonDemo.drawSomeGeoJsonObjects()
                R.id.removeAll -> geojsonDemo.removeAllObjects()
                R.id.toggleVisibility -> geojsonDemo.toggleVisibility()
            }
            true
        }

        geojsonButton.setOnClickListener{
            geojsonPopupMenu.show()
        }
    }
}
