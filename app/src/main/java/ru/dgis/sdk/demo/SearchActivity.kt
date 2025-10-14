package ru.dgis.sdk.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.GeoRect
import ru.dgis.sdk.demo.databinding.ActivitySearchBinding
import ru.dgis.sdk.directory.DirectoryObject
import ru.dgis.sdk.directory.SearchViewCallback
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Padding
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.calcPosition
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.platform.Locale
import ru.dgis.sdk.platform.LocaleManager

// Spatial restriction for search in this activity.
private val dubaiGeoRect = GeoRect(GeoPoint(25.140595, 55.240626), GeoPoint(25.226267, 55.318421))

/**
 * Showcase for search UI control.
 *
 * Demonstration: open activity, tap the search bar at the top of screen, type "cafe" or any other search query.
 * See documentation on Search UI: https://docs.2gis.com/en/android/sdk/examples/directory#nav-lvl1--Search_UI
 */
class SearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private val locationService by lazy { application.locationService }
    private var mapObjectManager: MapObjectManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Setting locale for English in test purposes, so search results will be in english.
        LocaleManager.instance(DGis.context()).overrideLocales(listOf(Locale("en", "EN")))

        binding.mapView.getMapAsync { map ->
            mapObjectManager = MapObjectManager(map)
            map.camera.position = CameraPosition(
                point = GeoPoint(latitude = 25.200194699171405, longitude = 55.27539446018636),
                zoom = Zoom(16.856537f),
                tilt = Tilt(50.0f),
                bearing = Bearing(19.00000166708803)
            )
        }

        // Configure search engine behind search control. In this case we only set restrictions (to search in Dubai only)
        // and location provider (to show distance to search result object)
        // You can further modify search engine in this block, see documentation on search engine: https://docs.2gis.com/en/android/sdk/reference/10.0/ru.dgis.sdk.directory.SearchLayout#nav-lvl1--configureSearchEngine
        binding.searchLayout.configureSearchEngine {
            setLocationProvider(locationService)
            setAreaOfInterest(dubaiGeoRect)
        }

        // See documentation on SearhViewCallback: https://docs.2gis.com/en/android/sdk/reference/10.0/ru.dgis.sdk.directory.SearchViewCallback
        binding.searchLayout.addSearchViewCallback(object : SearchViewCallback {
            override fun directoryObjectChosen(obj: DirectoryObject) {
                if (obj.markerPosition != null) {
                    binding.searchLayout.hideResults()
                    val marker = Marker(
                        MarkerOptions(
                            position = obj.markerPosition!!,
                            icon = imageFromResource(DGis.context(), R.drawable.ic_marker)
                        )
                    )
                    mapObjectManager?.removeAll()
                    mapObjectManager?.addObject(marker)

                    binding.mapView.getMapAsync {
                        val position = calcPosition(it.camera, listOf(marker), screenArea = Padding(20, 0, 10, 0))
                        it.camera.move(position)
                    }
                } else {
                    Toast.makeText(
                        this@SearchActivity,
                        "This object hasn't marker position :(",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun searchCompletedSuccessfully(items: List<DirectoryObject>) {
                binding.searchLayout.hideResults()
                val markerIcon = imageFromResource(DGis.context(), R.drawable.ic_marker)
                val markers: List<Marker> = items.asSequence().filter { it.markerPosition != null }.map {
                    Marker(MarkerOptions(position = it.markerPosition!!, icon = markerIcon))
                }.toList()

                mapObjectManager?.removeAll()
                mapObjectManager?.addObjects(markers)

                binding.mapView.getMapAsync {
                    val position = calcPosition(it.camera, markers)
                    it.camera.move(position)
                }
            }

            override fun searchAborted() {
                mapObjectManager?.removeAll()
            }

            override fun searchClosed() {
                mapObjectManager?.removeAll()
            }
        })
    }
}
