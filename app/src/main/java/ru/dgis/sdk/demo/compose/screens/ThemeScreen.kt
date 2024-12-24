package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.demo.compose.configurators.MapThemeConfigurator
import ru.dgis.sdk.map.MapOptions

@Composable
fun ThemeScreen(mapState: MapComposableState) {
    MapComposable(state = mapState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        MapThemeConfigurator(
            theme = mapState.theme.collectAsState().value,
            onThemeChange = { mapState.setTheme(it) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ThemeScreenPreview() {
    ThemeScreen(mapState = MapComposableState(MapOptions()))
}
