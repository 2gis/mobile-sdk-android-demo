package ru.dgis.sdk.demo.compose.examples.minimap

import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val MinimapExample = ComposeExample(
    id = "minimap",
    title = "Minimap",
    summary = R.string.compose_example_minimap_summary,
    description = R.string.compose_example_minimap_description,
    topic = ComposeExampleTopic.Map,
) {
    MinimapScreen(
        mapViewModel = mapViewModel,
        viewModel = viewModel(viewModelStoreOwner) {
            MinimapScreenViewModel(sdkContext, mapOptions.minimapControllerOptions(sdkContext))
        },
        mapOptions = mapOptions,
    )
}
