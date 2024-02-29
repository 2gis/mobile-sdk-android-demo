package ru.dgis.sdk.demo.markers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.Context
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.markers.model.ui.MapMarkerData
import ru.dgis.sdk.demo.sdkContext
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.Anchor
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.SimpleClusterObject
import ru.dgis.sdk.map.SimpleClusterOptions
import ru.dgis.sdk.map.SimpleClusterRenderer
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.imageFromBitmap
import ru.dgis.sdk.map.imageFromResource

class MarkersMapActivity : AppCompatActivity() {
    private val sdkContext: Context by lazy { application.sdkContext }
    private val viewModel: MapMarkersViewModel by viewModels()
    private val closeables = mutableListOf<AutoCloseable?>()
    private var map: Map? = null
    private val clusterRenderer = object : SimpleClusterRenderer {
        override fun renderCluster(cluster: SimpleClusterObject): SimpleClusterOptions {
            val objectCount = cluster.objectCount
            return SimpleClusterOptions(
                icon = imageFromBitmap(sdkContext, getClusterIcon(objectCount.toString())),
                iconWidth = LogicalPixel(30.0f),
                animatedAppearance = true
            )
        }
    }
    private lateinit var mapView: MapView
    private lateinit var mapObjectManager: MapObjectManager
    private lateinit var root: View

    @SuppressLint("InflateParams")
    private fun getClusterIcon(size: String): Bitmap {
        val view = LayoutInflater.from(this).inflate(R.layout.w_cluster, null)
        val clusterSize = view.findViewById<TextView>(R.id.clusterSize)
        clusterSize.text = size

        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                resources.getDimensionPixelSize(R.dimen.cluster_height),
                View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                resources.getDimensionPixelSize(R.dimen.cluster_height),
                View.MeasureSpec.EXACTLY
            )
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map_markers)

        root = findViewById(R.id.content)

        mapView = findViewById<MapView>(R.id.mapView).also {
            lifecycle.addObserver(it)
            it.getMapAsync(this::onMapReady)
        }

        viewModel.markers.observe(this, this::onMarkersChange)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeables.forEach { it?.close() }
    }

    private fun onMapReady(map: Map) {
        this.map = map
        closeables.add(map)

        mapObjectManager = MapObjectManager.withClustering(
            map = map,
            logicalPixel = LogicalPixel(60.0f),
            maxZoom = Zoom(18.0f),
            clusterRenderer = clusterRenderer
        )
        closeables.add(mapObjectManager)
        viewModel.loadMarkers(this)
    }

    private fun onMarkersChange(markers: List<MapMarkerData>) {
        val markersWithIcons = markers.map {
            val markerIcon = imageFromResource(sdkContext, R.drawable.ic_map_marker)
            Marker(
                MarkerOptions(
                    icon = markerIcon,
                    anchor = Anchor(INFO_ANCHOR_X_DEFAULT, 0f),
                    position = GeoPointWithElevation(
                        latitude = it.markerPosition.latitude,
                        longitude = it.markerPosition.longitude
                    ),
                    animatedAppearance = true
                )
            )
        }

        mapObjectManager.addObjects(markersWithIcons)

        val cameraPosition = CameraPosition(
            point = DEFAULT_LAT_LANG,
            zoom = Zoom(ZOOM_SEARCH)
        )
        map?.camera?.move(cameraPosition)
    }

    companion object {
        private const val INFO_ANCHOR_X_DEFAULT = 0.5f
        private const val ZOOM_SEARCH = 10f
        private val DEFAULT_LAT_LANG = GeoPoint(59.93863, 30.31413)
    }
}
