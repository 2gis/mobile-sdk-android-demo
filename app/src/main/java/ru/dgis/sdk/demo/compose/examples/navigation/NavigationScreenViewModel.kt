package ru.dgis.sdk.demo.compose.examples.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dgis.sdk.Context
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.compose.navigation.controls.defaultcontrols.navigation.DefaultNavigationControlsState
import ru.dgis.sdk.compose.navigation.controls.defaultcontrols.navigation.NavigationControlsState
import ru.dgis.sdk.compose.navigation.settings.NavigationSettingsRepository
import ru.dgis.sdk.demo.compose.examples.navigation.settings.ComposeNavigationSettingsState
import ru.dgis.sdk.demo.compose.examples.navigation.settings.FollowControllerSwitcher
import ru.dgis.sdk.demo.compose.examples.navigation.settings.FollowControllerType
import ru.dgis.sdk.demo.compose.examples.navigation.settings.applySettings
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.routing.TrafficRouter
import android.content.Context as AndroidContext

/**
 * Navigation on points picked on the map, with the default navigation UI of the SDK.
 *
 * Long touch sets the start and then the finish point, a tap between them adds
 * an intermediate point. Route and sound options are changed in the settings of
 * the navigation dashboard and stored in [NavigationSettingsRepository].
 *
 * @param trafficRouter searches the route between the selected points.
 * @param navigationManager runs navigation along the found route.
 */
open class NavigationScreenViewModel(
    private val sdkContext: Context,
    appContext: AndroidContext,
    private val minimapOptions: MapControllerOptions,
    private val trafficRouter: TrafficRouter = TrafficRouter(sdkContext),
    protected val navigationManager: NavigationManager = NavigationManager(sdkContext, trafficRouter)
) : ViewModel(), TouchEventsObserver {
    private val settingsRepository = NavigationSettingsRepository.default(appContext)
    private val locationSource = MyLocationMapObjectSource(sdkContext)

    private var screenModel: NavigationScreenModel? = null
    private var screenStateJob: Job? = null

    private val _navigationControlsState = MutableStateFlow<NavigationControlsState?>(null)

    /** Navigation UI of the SDK; null until the map is set. */
    val navigationControlsState: StateFlow<NavigationControlsState?> = _navigationControlsState.asStateFlow()

    private val _navigationScreenState = MutableStateFlow<NavigationScreenState?>(null)

    /** Step of the route setup; null until the map is set. */
    val navigationScreenState: StateFlow<NavigationScreenState?> = _navigationScreenState.asStateFlow()

    private val followControllerSwitcher = FollowControllerSwitcher(viewModelScope)

    /** Map the navigation works on; setting another map moves navigation to it. */
    var map: Map? = null
        set(value) {
            if (field == value) return
            field?.let(::unbindMap)
            field = value
            value?.let(::bindMap)
        }

    private fun bindMap(map: Map) {
        // While navigation is active the navigator replaces this marker with its own
        // and puts it back after the stop.
        map.addSource(locationSource)

        val model = NavigationScreenModel(
            sdkContext = sdkContext,
            map = map,
            navigationManager = navigationManager,
            trafficRouter = trafficRouter,
            settingsRepository = settingsRepository
        )
        screenModel = model
        screenStateJob = viewModelScope.launch {
            model.state.collect { _navigationScreenState.value = it }
        }

        _navigationControlsState.value = DefaultNavigationControlsState(
            map = map,
            navigationManager = navigationManager,
            minimapOptions = minimapOptions,
            settingsRepository = settingsRepository
        ).apply {
            dashboardViewModel.setFinishHandler { model.clear() }
            finishRouteViewModel.setFinishHandler { model.clear() }
        }
    }

    private fun unbindMap(map: Map) {
        followControllerSwitcher.reset(map)
        screenStateJob?.cancel()
        screenStateJob = null
        screenModel?.clear()
        screenModel = null
        _navigationControlsState.value?.onCleared()
        _navigationControlsState.value = null
        map.removeSource(locationSource)
    }

    /**
     * Applies [settings] chosen in the settings dialog and starts navigation on the selected
     * points: free roam, or navigation / simulation along a found route.
     */
    open fun startNavigation(settings: ComposeNavigationSettingsState) {
        navigationManager.applySettings(settings)
        map?.let {
            followControllerSwitcher.apply(
                map = it,
                type = FollowControllerType.entries.getOrNull(settings.followControllerTypeOrdinal)
                    ?: FollowControllerType.Default
            )
        }
        screenModel?.navigate(
            navigationType = settings.navigationType,
            isFreeRoamEnabled = settings.isFreeRoamEnabled,
            useSimulation = settings.useSimulation
        )
    }

    /** Drops the selected points without starting navigation. */
    fun cancelRoute() {
        followControllerSwitcher.reset(map)
        screenModel?.clear()
    }

    override fun onTap(point: ScreenPoint) {
        _navigationControlsState.value?.controlsViewModel?.onMapControlsAreaTouched()
        screenModel?.addIntermediatePoint(point)
    }

    override fun onLongTouch(point: ScreenPoint) {
        if (_navigationScreenState.value == NavigationScreenState.Navigation) return
        screenModel?.addPoint(point)
    }

    override fun onCleared() {
        map = null
        // The model owns the SDK objects it was created with.
        locationSource.close()
        navigationManager.close()
        trafficRouter.close()
        super.onCleared()
    }
}
