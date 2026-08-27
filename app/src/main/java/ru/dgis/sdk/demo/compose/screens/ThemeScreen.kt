package ru.dgis.sdk.demo.compose.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.collectMap
import ru.dgis.sdk.demo.compose.configurators.MapThemeConfigurator
import ru.dgis.sdk.demo.compose.demoMapCopyrightOptions
import ru.dgis.sdk.demo.compose.demoMapRenderOptions
import ru.dgis.sdk.demo.compose.previewMapViewModel
import ru.dgis.sdk.map.Fixed
import ru.dgis.sdk.map.MapAppearance
import ru.dgis.sdk.map.MapControllerViewModel
import ru.dgis.sdk.map.MapTheme

@Composable
fun ThemeScreen(mapViewModel: MapControllerViewModel) {
    val map = mapViewModel.collectMap()
    var theme by remember(map) { mutableStateOf(map?.theme ?: MapTheme.defaultTheme) }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = demoMapRenderOptions,
        copyrightOptions = demoMapCopyrightOptions
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        MapThemeConfigurator(
            theme = theme,
            onThemeChange = {
                theme = it
                map?.appearance = MapAppearance(Fixed(it))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeScreenPreview() {
    ThemeScreen(mapViewModel = previewMapViewModel())
}
