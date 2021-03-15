package ru.dgis.sdk.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import imageFromResource
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

private class MarkerData(val onClick: () -> Unit)

/*
Пример демонстрирует добавление маркеров на карту и обработку нажатий на них.
Добавляем маркера с помощью MapObjectManager.addMarker, задав позицию и иконку.
К маркерам в качестве пользовательских данных(MapObject.userData) прикрепляем объект,
содержащий обработчик нажатия - MarkerData.onClick.
По событию нажатия на карту(TouchEventsObserver.onTap) с помощью Map.getRenderedObjects
получаем объект в точке нажатия и, если он содержит обработчик нажатия, вызываем его.
 */
class MarkersActivity : AppCompatActivity(), TouchEventsObserver {
    lateinit var sdkContext: Context
    private var map: Map? = null
    private val objectsManager by lazy { createMapObjectManager(map!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext
        setContentView(R.layout.activity_markers)
        findViewById<MapView>(R.id.mapView).let { mapView ->
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
            val markerData = mapObject.userData() as? MarkerData ?: return@onResult
            markerData.onClick()
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
        val image1 = imageFromResource(sdkContext, R.drawable.ic_nav_point)
        val image2 = imageFromResource(sdkContext, R.drawable.ic_blue_nav_pin)

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

        points.forEachIndexed { i, point ->
            objectsManager.addMarker(
                MarkerOptions(
                    position = point,
                    icon = if (i < 5) image1 else image2,
                    userData = MarkerData(onClick = {
                        Toast.makeText(this, "Marker #${i + 1} clicked", Toast.LENGTH_SHORT).show()
                    })
                )
            )
        }
    }
}