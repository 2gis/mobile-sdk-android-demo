package ru.dgis.sdk.demo.compose.examples.rendermode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel

@Composable
fun RenderModeScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    var renderOptions by remember { mutableStateOf(mapOptions.renderOptions) }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = renderOptions,
        copyrightOptions = mapOptions.copyrightOptions
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MapRenderModeConfigurator(
            renderMode = renderOptions.renderMode,
            onRenderModeChange = { renderOptions = renderOptions.copy(renderMode = it) }
        )

        MapRenderMetricsConfigurator(
            devicePpi = renderOptions.devicePpi,
            onDevicePpiChange = { renderOptions = renderOptions.copy(devicePpi = it) },
            deviceDensity = renderOptions.deviceDensity,
            onDeviceDensityChange = { renderOptions = renderOptions.copy(deviceDensity = it) }
        )
    }
}
