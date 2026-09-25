package ru.dgis.sdk.demo.compose.examples.controls

import android.util.Log
import android.view.Gravity
import androidx.compose.runtime.Composable
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.compose.map.MapComposable

@Composable
fun ControlsScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = mapOptions.copyrightOptions.copy(
            // Display copyright in the bottom left corner to avoid conflict with the MyLocation control.
            gravity = Gravity.BOTTOM or Gravity.START,
            // Just check custom uri opener.
            uriOpener = { Log.d("ControlsScreen", it) },
        )
    )

    MapControls(map = mapViewModel.map)
}
