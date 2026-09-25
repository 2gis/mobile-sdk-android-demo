package ru.dgis.sdk.demo.compose.examples.minimap

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.minimap.DefaultMinimapControllerViewModel
import ru.dgis.sdk.compose.map.minimap.MinimapComposable
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.Opacity

/** Owns the minimap for the lifetime of the screen; survives configuration changes. */
class MinimapScreenViewModel(
    sdkContext: Context,
    minimapControllerOptions: MapControllerOptions,
) : ViewModel() {
    val minimap = DefaultMinimapControllerViewModel(
        sdkContext,
        minimapControllerOptions,
    )

    override fun onCleared() = minimap.close()
}

@Composable
fun MinimapScreen(
    mapViewModel: ReadyMapControllerViewModel,
    viewModel: MinimapScreenViewModel,
    mapOptions: ComposeExampleMapOptions,
) {
    val minimapViewModel = viewModel.minimap

    Box(modifier = Modifier.fillMaxSize()) {
        MapComposable(
            viewModel = mapViewModel,
            renderOptions = mapOptions.renderOptions,
            copyrightOptions = mapOptions.copyrightOptions,
        )

        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            MinimapComposable(
                viewModel = minimapViewModel,
                size = 200.dp,
                opacity = Opacity(1f),
            )
        }
    }
}
