package ru.dgis.sdk.demo

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.dgis.sdk.coordinates.GeoRect
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.demo.databinding.ActivityTakeSnapshotBinding
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.map.toBitmap
import kotlin.random.Random

/**
 * Activity demonstrating how to take a snapshot of the map. This includes capturing the current
 * camera view and any dynamic objects (like markers) present on the map. The resulting image is
 * displayed in an ImageView.
 */
class TakeSnapshotActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTakeSnapshotBinding.inflate(layoutInflater) }
    private lateinit var mapObjectManager: MapObjectManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.snapshotButton.setOnClickListener {
            createSnapshot()
        }
    }

    /**
     * Initiates the snapshot process of the current map view. This method first synchronizes the
     * state of the map, updates marker positions, and then captures a bitmap image of the map,
     * which is subsequently displayed in the ImageView within this activity.
     */
    private fun createSnapshot() {
        binding.mapView.getMapAsync { map ->
            if (!::mapObjectManager.isInitialized) {
                mapObjectManager = MapObjectManager(map)
            }
            mapObjectManager.apply {
                removeAll()
                addObjects(
                    RandomMarkerGenerator(
                        imageFromResource(application.sdkContext, R.drawable.ic_marker),
                        map.camera.visibleRect
                    ).generate(10)
                )
            }

            /**
             * This is main method MapView for taking snapshot. It return future with ImageData, which is suitable for
             * ImageView
             */
            binding.mapView.takeSnapshot().onComplete(
                { imgData ->
                    binding.snapshotView.setImageBitmap(imgData.toBitmap())
                },
                { exc ->
                    Log.e("TakeSnapshotActivity", "Error when creating snapshot: ${exc.message}")
                }
            )
        }
    }
}

/**
 * Utility class for generating markers with random geographic positions within a specified
 * rectangular area. These markers are used to demonstrate dynamic object integration in map snapshots.
 */
private class RandomMarkerGenerator(
    private val markerIcon: Image,
    private val rect: GeoRect
) {
    /**
     * Generates a list of markers with random positions within the bounding rectangle specified
     * by [rect].
     *
     * @param quantity The number of markers to generate.
     * @return A list of [Marker] instances with random positions.
     */
    fun generate(quantity: Int) = List(quantity) {
        Marker(
            MarkerOptions(
                position = randomPosition(),
                icon = markerIcon
            )
        )
    }

    /**
     * Generates a random geographic position within the bounds of the rectangle provided during class instantiation.
     *
     * @return A [GeoPointWithElevation] representing the random geographic position.
     */
    private fun randomPosition(): GeoPointWithElevation {
        val latitudeMax = rect.northEastPoint.latitude
        val longitudeMax = rect.northEastPoint.longitude
        val latitudeMin = rect.southWestPoint.latitude
        val longitudeMin = rect.southWestPoint.longitude

        return GeoPointWithElevation(
            latitude = Latitude(Random.nextFloat() * (latitudeMax.value - latitudeMin.value) + latitudeMin.value),
            longitude = Longitude(Random.nextFloat() * (longitudeMax.value - longitudeMin.value) + longitudeMin.value)
        )
    }
}
