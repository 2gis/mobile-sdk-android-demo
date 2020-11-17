package ru.dgis.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.sdk.DGis
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

class GeometryObjectsActivity : AppCompatActivity(), TouchEventsObserver {
    private lateinit var sdkContext: Context
    private lateinit var mapView: MapView
    private var map: Map? = null
    private val geometryDemo: GeometryDemo? by lazy {
        map?.let { GeometryDemo(DGis.context(), mapView.width, mapView.height, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_geometry_objects)

        val geometryButton = findViewById<FloatingActionButton>(R.id.createGeometryButton).apply {
            isEnabled = false
        }
        val geometryPopupMenu = PopupMenu(this, geometryButton, Gravity.END, 0, R.style.GeometryPopupTheme)

        val mapContainer = findViewById<LinearLayout>(R.id.map_container)
        val mapOptions = MapOptions().apply {
            position = CameraPosition(
                GeoPoint(Arcdegree(55.740444), Arcdegree(37.619524)),
                Zoom(9.0f),
                Tilt(0.0f),
                Arcdegree(0.0)
            )
        }

        mapView = MapView(this, mapOptions)
        mapContainer.addView(mapView)
        lifecycle.addObserver(mapView)
        mapView.setTouchEventsObserver(this)
        mapView.getMapAsync {
            map = it
            geometryButton.isEnabled = true
        }

        geometryPopupMenu.inflate(R.menu.geometry_menu)

        geometryPopupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.addPolygon -> geometryDemo?.addPolygon()
                R.id.addPolyline -> geometryDemo?.addPolyline()
                R.id.addPoint -> geometryDemo?.addPoint()
                R.id.addPoi -> geometryDemo?.addPoi()
                R.id.addPoint3d -> geometryDemo?.addPointWithElevation()
                R.id.addPoi3d -> geometryDemo?.addPoiWithElevation()
                R.id.addComplexObject -> geometryDemo?.addComplexObject()
                R.id.shiftObjects -> geometryDemo?.shiftObjects()
                R.id.removeLast -> geometryDemo?.removeLastObject()
                R.id.removeAll -> geometryDemo?.removeAllObjects()
                R.id.toggleVisibility -> geometryDemo?.toggleVisibility()
                R.id.toggleColor -> geometryDemo?.toggleColor()
            }
            true
        }

        geometryButton.setOnClickListener{
            geometryPopupMenu.show()
        }
    }

    override fun onTap(point: ViewportPoint) {
        mapView.getRenderedObjects(point, ScreenDistance(5f)).onResult {
            var message = "Ничего не нашли"
            if(it.isEmpty().not()) {
                message = "Нашли"
                val geometryMapObjectsCount = it.count { item -> tryCastToGeometryMapObject(item.item.item) != null }
                if(geometryMapObjectsCount > 0) message +=  "\nGeometryMapObjects: $geometryMapObjectsCount"
                val dgisMapObjectsCount = it.count { item -> tryCastToDgisMapObject(item.item.item) != null }
                if(dgisMapObjectsCount > 0) message +=  "\nDgisMapObjects: $dgisMapObjectsCount"
                val anotherObjectsCount = it.size - geometryMapObjectsCount - dgisMapObjectsCount
                if(anotherObjectsCount > 0) message +=  "\nUnknownObjects: $anotherObjectsCount"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLongTouch(point: ViewportPoint) {

    }
}