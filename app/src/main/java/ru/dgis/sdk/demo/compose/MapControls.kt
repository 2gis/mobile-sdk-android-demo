package ru.dgis.sdk.demo.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.controls.CompassComposable
import ru.dgis.sdk.compose.map.controls.IndoorComposable
import ru.dgis.sdk.compose.map.controls.MyLocationComposable
import ru.dgis.sdk.compose.map.controls.TrafficComposable
import ru.dgis.sdk.compose.map.controls.ZoomComposable
import ru.dgis.sdk.map.Map

@Composable
fun MapControls(map: Map) {
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
            TrafficComposable(map)
        }
        Column(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZoomComposable(map)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompassComposable(map)
            MyLocationComposable(map)
        }
        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IndoorComposable(map)
        }
    }
}
