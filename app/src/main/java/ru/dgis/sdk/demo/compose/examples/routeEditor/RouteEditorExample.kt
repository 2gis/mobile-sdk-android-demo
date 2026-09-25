package ru.dgis.sdk.demo.compose.examples.routeEditor

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val RouteEditorExample = ComposeExample(
    id = "route_editor",
    title = "Route Editor",
    summary = R.string.compose_example_route_editor_summary,
    description = R.string.compose_example_route_editor_description,
    topic = ComposeExampleTopic.Navigation,
) {
    val appContext = LocalContext.current.applicationContext
    RouteEditorScreen(
        mapViewModel = mapViewModel,
        viewModel = viewModel(viewModelStoreOwner) {
            RouteEditorScreenViewModel(sdkContext, appContext)
        },
        mapOptions = mapOptions,
    )
}
