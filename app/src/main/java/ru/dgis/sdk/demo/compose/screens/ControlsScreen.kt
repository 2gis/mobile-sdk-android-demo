package ru.dgis.sdk.demo.compose.screens

import android.util.Log
import android.view.Gravity
import androidx.compose.runtime.Composable
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.collectMap
import ru.dgis.sdk.demo.compose.MapControls
import ru.dgis.sdk.demo.compose.demoMapCopyrightOptions
import ru.dgis.sdk.demo.compose.demoMapRenderOptions
import ru.dgis.sdk.map.MapControllerViewModel

@Composable
fun ControlsScreen(mapViewModel: MapControllerViewModel) {
    val map = mapViewModel.collectMap()

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = demoMapRenderOptions,
        copyrightOptions = demoMapCopyrightOptions.copy(
            gravity = Gravity.BOTTOM or Gravity.START,
            uriOpener = { Log.d("ControlsScreen", it) }
        )
    )

    map?.let {
        MapControls(map = it)
    }
}
