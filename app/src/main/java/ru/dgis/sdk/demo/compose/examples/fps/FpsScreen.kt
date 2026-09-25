package ru.dgis.sdk.demo.compose.examples.fps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.asFlow

@Composable
fun FpsScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    val mapController = mapViewModel.mapController
    var fpsCounter by remember { mutableIntStateOf(0) }
    var maxFps by remember(mapController) { mutableStateOf(mapController.renderer.maxFps) }
    var powerSavingMaxFps by remember(mapController) {
        mutableStateOf(mapController.renderer.powerSavingMaxFps)
    }

    LaunchedEffect(mapController) {
        mapController.renderer.fpsChannel.asFlow().collect { fpsCounter = it.value }
    }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = mapOptions.copyrightOptions
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MapFpsConfigurator(
                maxFpsValue = maxFps,
                onMaxFpsChange = {
                    maxFps = it
                    mapController.renderer.setMaxFps(it, powerSavingMaxFps)
                },
                powerSavingMaxFpsValue = powerSavingMaxFps,
                onPowerSavingMaxFpsChange = {
                    powerSavingMaxFps = it
                    mapController.renderer.setMaxFps(maxFps, it)
                }
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
