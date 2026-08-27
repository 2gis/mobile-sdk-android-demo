package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.awaitMapControllerOrShowError
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.databinding.ActivityParkingBinding
import ru.dgis.sdk.map.AttributeValue
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.Zoom

private const val PARKING_ATTRIBUTE = "parkingOn"

/**
 * Showcase for visualize parkings on the map.
 * Core principle here is to use "parkingOn" map attribute. To learn more about attributes see
 * [AttributeValue](https://docs.2gis.com/ru/android/sdk/reference/7.0/ru.dgis.sdk.map.AttributeValue)
 *
 * Demonstration: open activity and toggle parking button to see parkings on map
 */
class ParkingActivity : AppCompatActivity() {
    private val binding: ActivityParkingBinding by lazy {
        ActivityParkingBinding.inflate(
            layoutInflater
        )
    }
    private val mapOwner by demoMapOwner(
        CameraPosition(
            point = GeoPoint(25.106908, 55.147874),
            zoom = Zoom(16.4f),
            tilt = Tilt(40f),
            bearing = Bearing(310.0)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapView = binding.mapContainer.attachMapView(mapOwner.mapViewModel)

        lifecycleScope.launch {
            val controller = awaitMapControllerOrShowError(mapOwner.mapViewModel) ?: return@launch
            binding.zoomControl.bindToMap(controller, mapView)
            enableToggleParkings(controller.map)
        }
    }

    /**
     * Core logic is here. We are checking current value of boolean attribute
     * and setting new value, which is opposite.
     * You must pay attention to a nullability of AttributeValue
     */
    private fun enableToggleParkings(map: Map) {
        binding.parkingButton.setOnClickListener {
            val oldValue = map.attributes.getAttributeValue(PARKING_ATTRIBUTE).asBoolean ?: false
            map.attributes.setAttributeValue(PARKING_ATTRIBUTE, AttributeValue(!oldValue))
            it.isActivated = !oldValue
        }
    }
}
