package ru.dgis.sdk.demo.compose.examples.sharedroute

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import okhttp3.OkHttpClient
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val SharedRouteExample = ComposeExample(
    id = "shared_route",
    title = "Shared Route",
    summary = R.string.compose_example_shared_route_summary,
    description = R.string.compose_example_shared_route_description,
    topic = ComposeExampleTopic.Navigation,
) {
    val appContext = LocalContext.current.applicationContext
    SharedRouteScreen(
        mapViewModel = mapViewModel,
        viewModel = viewModel(viewModelStoreOwner) {
            SharedRouteScreenViewModel(
                sdkContext = sdkContext,
                okHttpClient = OkHttpClient(),
                apiKey = readApiKeyFromAsset(appContext),
                appId = appContext.packageName,
            )
        },
        mapOptions = mapOptions,
    )
}
