package ru.dgis.sdk.demo.compose.examples.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic
import ru.dgis.sdk.demo.compose.examples.navigation.settings.ComposeNavigationSettingsViewModel

val NavigationExample = ComposeExample(
    id = "navigation_demo",
    title = "Navigation",
    summary = R.string.compose_example_navigation_demo_summary,
    description = R.string.compose_example_navigation_demo_description,
    topic = ComposeExampleTopic.Navigation
) {
    val appContext = LocalContext.current.applicationContext
    NavigationScreen(
        mapViewModel = mapViewModel,
        viewModel = viewModel(viewModelStoreOwner) {
            NavigationScreenViewModel(
                sdkContext = sdkContext,
                appContext = appContext,
                minimapOptions = mapOptions.minimapControllerOptions(sdkContext)
            )
        },
        settingsViewModel = viewModel(viewModelStoreOwner) {
            ComposeNavigationSettingsViewModel()
        },
        mapOptions = mapOptions
    )
}
