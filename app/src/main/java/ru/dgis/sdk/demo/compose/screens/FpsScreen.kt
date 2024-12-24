package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.demo.compose.configurators.MapFpsConfigurator
import ru.dgis.sdk.map.MapOptions

@Composable
fun FpsScreen(mapState: MapComposableState) {
    var fpsCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(mapState) {
        mapState.fpsCounterCallback = { fpsCounter = it.value }
    }

    MapComposable(state = mapState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MapFpsConfigurator(
                maxFpsValue = mapState.maxFps.collectAsState().value,
                onMaxFpsChange = { mapState.setMaxFps(it) },
                powerSavingMaxFpsValue = mapState.powerSavingMaxFps.collectAsState().value,
                onPowerSavingMaxFpsChange = { mapState.setPowerSavingMaxFps(it) }
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "FPS: $fpsCounter",
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FpsScreenPreview() {
    FpsScreen(mapState = MapComposableState(MapOptions()))
}
