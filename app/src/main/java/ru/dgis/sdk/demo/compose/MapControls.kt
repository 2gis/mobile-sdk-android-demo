package ru.dgis.sdk.demo.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.controls.compass.CompassComposable
import ru.dgis.sdk.compose.map.controls.compass.DefaultCompassViewModel
import ru.dgis.sdk.compose.map.controls.indoor.DefaultIndoorViewModel
import ru.dgis.sdk.compose.map.controls.indoor.IndoorComposable
import ru.dgis.sdk.compose.map.controls.mylocation.DefaultMyLocationViewModel
import ru.dgis.sdk.compose.map.controls.mylocation.MyLocationComposable
import ru.dgis.sdk.compose.map.controls.traffic.DefaultTrafficViewModel
import ru.dgis.sdk.compose.map.controls.traffic.TrafficComposable
import ru.dgis.sdk.compose.map.controls.zoom.DefaultZoomViewModel
import ru.dgis.sdk.compose.map.controls.zoom.ZoomComposable
import ru.dgis.sdk.map.Map

@Composable
fun MapControls(map: Map) {
    val compassViewModel = remember(map) { DefaultCompassViewModel(map) }
    val zoomViewModel = remember(map) { DefaultZoomViewModel(map) }
    val trafficViewModel = remember(map) { DefaultTrafficViewModel(map) }
    val myLocationViewModel = remember(map) { DefaultMyLocationViewModel(map) }
    val indoorViewModel = remember(map) { DefaultIndoorViewModel(map) }

    DisposableEffect(map) {
        onDispose {
            compassViewModel.onCleared()
            zoomViewModel.onCleared()
            trafficViewModel.onCleared()
            myLocationViewModel.onCleared()
            indoorViewModel.onCleared()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrafficComposable(viewModel = trafficViewModel)
        }
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZoomComposable(viewModel = zoomViewModel)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompassComposable(viewModel = compassViewModel)
            MyLocationComposable(viewModel = myLocationViewModel)
        }
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IndoorComposable(viewModel = indoorViewModel)
        }
    }
}
