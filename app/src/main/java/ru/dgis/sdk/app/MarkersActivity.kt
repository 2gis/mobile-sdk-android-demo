package ru.dgis.sdk.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

private class MarkerData(
        val index: Int,
        val pin: Image,
        val selectedPin: Image,
        var selected: Boolean = false
)

private class OnClickListener(
        val customMarker: MarkerData,
        val onClick: (Marker, MarkerData) -> Unit
)

/*
Пример демонстрирует добавление маркеров на карту и обработку нажатий на них.
Добавляем маркера с помощью MapObjectManager.addMarker, задав позицию и иконку.
По событию нажатия на карту(TouchEventsObserver.onTap) с помощью Map.getRenderedObjects
получаем объект в точке нажатия и, если он содержит обработчик нажатия, вызываем его.
 */
class MarkersActivity : AppCompatActivity(), TouchEventsObserver {
    private lateinit var sdkContext: Context
    private var map: Map? = null
    private val objectsManager by lazy { MapObjectManager(map!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext
        setContentView(R.layout.activity_markers)
        findViewById<MapView>(R.id.mapView).let { mapView ->
            lifecycle.addObserver(mapView)
            mapView.getMapAsync {
                this.map = it
                mapView.setTouchEventsObserver(this)
                createMarkers()
            }
        }
    }

    override fun onTap(point: ScreenPoint) {
        map!!.getRenderedObjects(point, ScreenDistance(5f)).onResult { objects ->
            val mapObject = objects.firstOrNull()?.item?.item ?: return@onResult
            val dgisMarker = mapObject as? Marker ?: return@onResult
            // val markerData = dgisMarker.userData as? MarkerData ?: return@onResult
            val listener = dgisMarker.userData as? OnClickListener ?: return@onResult

            listener.onClick(dgisMarker, listener.customMarker)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        map?.close()
    }

    private fun geoPoint(lat: Double, lon: Double): GeoPointWithElevation {
        return GeoPointWithElevation(Arcdegree(lat), Arcdegree(lon))
    }

    private fun createMarkers() {
        val points = listOf(
            geoPoint(55.920520053981384, 37.46420854702592),
            geoPoint(55.920227539812964, 37.68484982661903),
            geoPoint(55.553486118464704, 37.5579992774874),
            geoPoint(55.691960072144575, 37.49265655875206),
            geoPoint(55.74825400089946, 37.79325306415558),
            geoPoint(55.78631800125226, 37.7140749245882),
            geoPoint(55.78890152615578, 37.54570101387799),
            geoPoint(55.809648199608844, 37.76941685937345),
            geoPoint(55.81958715998664, 37.52110465429723)
        )

        val pin = imageFromResource(sdkContext, R.drawable.ic_nav_point)
        val selectedPin = imageFromResource(sdkContext, R.drawable.ic_blue_nav_pin)

        points.forEachIndexed { i, point ->

            val onClickCallback = { dgisMarker: Marker, customMarker: MarkerData ->
                Toast
                    .makeText(this, "Marker #${customMarker.index} clicked", Toast.LENGTH_SHORT)
                    .show()

                customMarker.selected = !customMarker.selected

                dgisMarker.icon = if (customMarker.selected)
                    customMarker.selectedPin
                else
                    customMarker.pin
            }

            val listener = OnClickListener(
                    customMarker = MarkerData(i + 1, pin, selectedPin),
                    onClick = onClickCallback
            )

            val options = MarkerOptions(
                    position = point,
                    icon = pin,
                    userData = listener
            )

            objectsManager.addMarker(options)
        }
    }
}