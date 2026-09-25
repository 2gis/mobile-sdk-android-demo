package ru.dgis.sdk.demo.compose.examples.theme

import androidx.compose.foundation.layout.Box
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
fun ThemeScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    val mapController = mapViewModel.mapController
    val map = mapController.map
    var appearance by remember { mutableStateOf(map.appearance) }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = mapOptions.copyrightOptions
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        MapAppearanceConfigurator(
            mapAppearance = appearance,
            onMapAppearanceChange = {
                map.appearance = it
                appearance = it
            }
        )
    }
}
