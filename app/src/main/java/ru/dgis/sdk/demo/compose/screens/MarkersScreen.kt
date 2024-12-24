package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.dgis.sdk.DGis
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.MarkerViewModel
import ru.dgis.sdk.demo.compose.configurators.MarkerConfigurator
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.imageFromResource

private fun createMarker(position: GeoPoint): Marker {
    val options = MarkerOptions(
        position = GeoPointWithElevation(point = position),
        icon = imageFromResource(DGis.context(), R.drawable.ic_marker)
    )

    return Marker(options).apply {
        text = "Text text text\nText text"
    }
}

@Composable
private fun Marker(map: Map, modifier: Modifier = Modifier) {
    data class State(
        val marker: Marker,
        val mapObjectManager: MapObjectManager
    )

    val state = remember {
        val marker = createMarker(map.camera.position.point)

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
fun MarkersScreen(mapState: MapComposableState) {
    val map by mapState.map.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MapComposable(state = mapState)

        map?.let {
            Marker(map = it, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
