package ru.dgis.sdk.demo.compose

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dgis.sdk.Context
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.createDgisSources
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.MapAppearance
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MapControllerState
import ru.dgis.sdk.map.MapControllerViewModel
import ru.dgis.sdk.map.MapCopyrightOptions
import ru.dgis.sdk.map.MapRenderOptions
import ru.dgis.sdk.map.RoadEventSource
import ru.dgis.sdk.map.Zoom

val demoMapRenderOptions = MapRenderOptions()
val demoMapCopyrightOptions = MapCopyrightOptions()

fun createComposeMapControllerOptions(sdkContext: Context): MapControllerOptions {
    val cameraPosition = CameraPosition(
        point = GeoPoint(
            latitude = 55.760898,
            longitude = 37.620242
        ),
        bearing = Bearing(20.0),
        zoom = Zoom(17f)
    )

    return MapControllerOptions(
        position = cameraPosition,
        sources = createDgisSources(sdkContext) + RoadEventSource(sdkContext),
        mapAppearance = MapAppearance.defaultAppearance()
    )
}

fun previewMapViewModel(): MapControllerViewModel = object : MapControllerViewModel {
    override val state: StateFlow<MapControllerState> = MutableStateFlow(
        MapControllerState.Creating(MapControllerOptions())
    )
}
