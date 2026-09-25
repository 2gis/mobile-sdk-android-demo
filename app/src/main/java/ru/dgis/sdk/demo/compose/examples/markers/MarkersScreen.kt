package ru.dgis.sdk.demo.compose.examples.markers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.dgis.sdk.Context
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.imageFromResource

private fun createMarker(sdkContext: Context, position: GeoPoint): Marker {
    val options = MarkerOptions(
        position = GeoPointWithElevation(point = position),
        icon = imageFromResource(sdkContext, R.drawable.ic_marker)
    )

    return Marker(options).apply {
        text = "Text text text\nText text"
    }
}

@Composable
private fun Marker(map: Map, sdkContext: Context, modifier: Modifier = Modifier) {
    data class State(
        val marker: Marker,
        val mapObjectManager: MapObjectManager
    )

    val state = remember {
        val marker = createMarker(sdkContext, map.camera.position.point)

        val mapObjectManager = MapObjectManager(map).apply {
            addObject(marker)
        }

        State(marker, mapObjectManager)
    }

    DisposableEffect(state) {
        onDispose {
            state.mapObjectManager.removeObject(state.marker)
        }
    }

    MarkerConfigurator(
        modifier = modifier,
        markerViewModel = MarkerViewModel(state.marker)
    )
}

@Composable
fun MarkersScreen(
    mapViewModel: ReadyMapControllerViewModel,
    sdkContext: Context,
    mapOptions: ComposeExampleMapOptions
) {
    val mapController = mapViewModel.mapController
    Box(modifier = Modifier.fillMaxSize()) {
        MapComposable(
            viewModel = mapViewModel,
            renderOptions = mapOptions.renderOptions,
            copyrightOptions = mapOptions.copyrightOptions
        )

        Marker(
            map = mapController.map,
            sdkContext = sdkContext,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
