package ru.dgis.sdk.demo.compose.examples.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.collectTouchEvents
import ru.dgis.sdk.demo.compose.examples.navigation.settings.ComposeNavigationSettingsViewModel
import ru.dgis.sdk.demo.compose.examples.navigation.settings.NavigationSettingsComposable
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.controls.indoor.DefaultIndoorViewModel
import ru.dgis.sdk.compose.map.controls.indoor.IndoorComposable
import ru.dgis.sdk.compose.navigation.controls.dashboard.NavigationDashboardComposableDefaults
import ru.dgis.sdk.compose.navigation.controls.defaultcontrols.navigation.NavigationControlsComposable
import ru.dgis.sdk.compose.navigation.settings.SectionVisibilityConfig
import ru.dgis.sdk.navigation.CustomDashboardButton

/**
 * Navigation on points picked on the map.
 *
 * Long touch sets the start and the finish point, a tap adds an intermediate
 * point between them. Then the navigation is set up in [NavigationSettingsComposable]
 * and starts with the default navigation UI of the SDK.
 */
@Composable
fun NavigationScreen(
    mapViewModel: ReadyMapControllerViewModel,
    viewModel: NavigationScreenViewModel,
    settingsViewModel: ComposeNavigationSettingsViewModel,
    mapOptions: ComposeExampleMapOptions,
) {
    val mapController = mapViewModel.mapController
    val navigationControlsState by viewModel.navigationControlsState.collectAsState()
    val navigationScreenState by viewModel.navigationScreenState.collectAsState()
    val navigationSettings by settingsViewModel.state.collectAsState()

    LaunchedEffect(mapController, viewModel) {
        viewModel.map = mapController.map
        mapController.collectTouchEvents(viewModel)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapComposable(
            viewModel = mapViewModel,
            renderOptions = mapOptions.renderOptions,
            copyrightOptions = mapOptions.copyrightOptions,
        )

        if (navigationScreenState != NavigationScreenState.Navigation) {
            val map = mapController.map
            val indoorViewModel = remember(map) { DefaultIndoorViewModel(map) }
            DisposableEffect(map) {
                onDispose { indoorViewModel.onCleared() }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                IndoorComposable(viewModel = indoorViewModel)
            }
        }

        navigationControlsState?.let { state ->
            NavigationControlsComposable(
                state = state,
                // A custom dashboard button: the app sets its own icon and action.
                addRoadEventButton = CustomDashboardButton(
                    icon = NavigationDashboardComposableDefaults.icons.addRouteEvent,
                    onClick = {},
                ),
                primarySettingsSection = navigationSettings.navigationType.settingsSection(),
            )
        }

        hintFor(navigationScreenState)?.let { hint ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
            ) {
                Text(
                    text = hint,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        if (navigationScreenState == NavigationScreenState.SetupRoute) {
            NavigationSettingsComposable(
                settingsViewModel = settingsViewModel,
                onOk = { viewModel.startNavigation(settingsViewModel.state.value) },
                onCancel = viewModel::cancelRoute,
            )
        }
    }
}

private fun hintFor(state: NavigationScreenState?): String? = when (state) {
    NavigationScreenState.SelectingStartPoint -> "Long tap to set the start"
    NavigationScreenState.SelectingRoutePoints -> "Long tap to set the finish, tap to add a stop"
    NavigationScreenState.SearchingRoute -> "Searching for a route…"
    else -> null
}

/** Route settings section the dashboard settings open with. */
private fun NavigationType.settingsSection(): String = when (this) {
    NavigationType.Car -> SectionVisibilityConfig.SECTION_CAR_ROUTE
    NavigationType.Pedestrian -> SectionVisibilityConfig.SECTION_PEDESTRIAN
    NavigationType.Bicycle -> SectionVisibilityConfig.SECTION_BICYCLE
}
