package ru.dgis.sdk.demo.compose

import androidx.lifecycle.ViewModel
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.Zoom

private fun createMapOptions(): MapOptions {
    // Position for testing controls such as Indoor и Compass.
    val cameraPosition = CameraPosition(
        point = GeoPoint(
            latitude = 55.760898,
            longitude = 37.620242
        ),
        bearing = Bearing(20.0),
        zoom = Zoom(17f)
    )

    return MapOptions().apply {
        position = cameraPosition
    }
}

class MapViewModel() : ViewModel() {
    val state by lazy {
        MapComposableState(
            mapOptions = createMapOptions()
        )
    }
}
