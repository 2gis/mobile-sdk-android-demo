package ru.dgis.sdk.demo.vm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.dgis.sdk.Context
import ru.dgis.sdk.await
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.MyLocationControllerSettings
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.RouteEditorSource
import ru.dgis.sdk.map.RouteMapObject
import ru.dgis.sdk.map.ScreenDistance
import ru.dgis.sdk.map.ScreenPoint
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.navigation.RouteBuildOptions
import ru.dgis.sdk.routing.BicycleRouteSearchOptions
import ru.dgis.sdk.routing.CarRouteSearchOptions
import ru.dgis.sdk.routing.PedestrianRouteSearchOptions
import ru.dgis.sdk.routing.PublicTransportRouteSearchOptions
import ru.dgis.sdk.routing.RouteEditor
import ru.dgis.sdk.routing.RouteEditorRouteParams
import ru.dgis.sdk.routing.RouteSearchOptions
import ru.dgis.sdk.routing.RouteSearchPoint
import ru.dgis.sdk.routing.ScooterRouteSearchOptions
import ru.dgis.sdk.routing.TaxiRouteSearchOptions
import ru.dgis.sdk.routing.TrafficRoute
import ru.dgis.sdk.navigation.State as NavigationState

class NavigationViewModel(
    private val sdkContext: Context,
    private val map: Map,
    private val coroutineScope: CoroutineScope
) : AutoCloseable {
    enum class State {
        ROUTE_EDITING,
        NAVIGATION
    }

    enum class MenuAction {
        SELECT_START_POINT,
        SELECT_FINISH_POINT,
        CLEAR_POINTS
    }

    enum class RouteType {
        CAR,
        PEDESTRIAN,
        BICYCLE,
        PUBLIC_TRANSPORT,
        SCOOTER,
        TAXI
    }

    private val _state = MutableStateFlow(State.ROUTE_EDITING)
    val state: StateFlow<State> = _state

    private val _canStartNavigation = MutableStateFlow(true)
    val canStartNavigation: StateFlow<Boolean> = _canStartNavigation

    val navigationType: NavigationState
        get() =
            if (points.any { it != null }) NavigationState.NAVIGATION else NavigationState.DISABLED

    var routeType: RouteType = RouteType.CAR
        set(value) {
            field = value
            updateRouteEditor()
        }

    var useSimulation: Boolean = false
        set(value) {
            field = value
            updateCanStartNavigation()
        }

    var messageCallback: ((String) -> Unit)? = null

    private val closeables = mutableListOf<AutoCloseable>()

    private val points = MutableList<RouteSearchPoint?>(2) { null }

    private var routes = listOf<TrafficRoute>()
    private val activeRoute
        get(): TrafficRoute? {
            val index = routeEditor.activeRouteIndex ?: return null
            return routes.getOrNull(index.value.toInt())
        }

    private val objectManager = MapObjectManager(map).also {
        closeables.add(it)
    }
    private val markers: List<Marker> =
        listOf(
            R.drawable.ic_small_pin,
            R.drawable.ic_marker
        ).map {
            Marker(
                MarkerOptions(
                    GeoPointWithElevation(0.0, 0.0),
                    icon = imageFromResource(sdkContext, it),
                    visible = false
                )
            )
        }.also {
            objectManager.addObjects(it)
            closeables.addAll(it)
        }

    private val routeEditor = RouteEditor(sdkContext).also {
        closeables.add(it)
        closeables.add(
            it.routesInfoChannel.connect { info ->
                routes = info.routes
                onRoutesChanged()
            }
        )
    }

    private val routeEditorSource = RouteEditorSource(sdkContext, routeEditor).also {
        it.setRoutesVisible(false)
        map.addSource(it)
        closeables.add(it)
    }

    val navigationManager = NavigationManager(sdkContext)

    init {
        closeables.add(map)
        initLocationSource()
    }

    private fun initLocationSource() {
        MyLocationMapObjectSource(
            sdkContext,
            MyLocationControllerSettings(BearingSource.SATELLITE)
        ).also {
            map.addSource(it)
            closeables.add(it)
        }
    }

    override fun close() {
        closeables.forEach(AutoCloseable::close)
        closeables.clear()
    }

    fun onMenuAction(routeSearchPoint: RouteSearchPoint, action: MenuAction) {
        fun setPoint(index: Int) {
            points[index] = routeSearchPoint
        }

        fun clearPoints() {
            points[0] = null
            points[1] = null
            routes = listOf()
            updateRouteEditorSource()
        }

        when (action) {
            MenuAction.SELECT_START_POINT -> setPoint(0)
            MenuAction.SELECT_FINISH_POINT -> setPoint(1)
            MenuAction.CLEAR_POINTS -> clearPoints()
        }

        updateMarkers()
        updateRouteEditor()
        updateCanStartNavigation()
    }

    fun onTap(point: ScreenPoint) {
        coroutineScope.launch {
            val objects = map.getRenderedObjects(point, ScreenDistance(5f)).await()
            for (obj in objects) {
                val routeMapObject = obj.item.item as? RouteMapObject ?: continue
                if (routeMapObject.isActive) {
                    continue
                }
                routeEditor.setActiveRouteIndex(routeMapObject.routeIndex)
                break
            }
        }
    }

    private fun updateMarkers() {
        fun hasRouteWithEndPoint(point: RouteSearchPoint): Boolean {
            if (routes.isEmpty()) return false
            val routeParams = routeEditor.routesInfo.routeParams
            return point == routeParams.finishPoint ||
                point == routeParams.startPoint
        }

        points.forEachIndexed { index, point ->
            val marker = markers[index]
            // Uncomment this to enable hiding markers when route is built.
//            marker.isVisible =
//                state.value == State.ROUTE_EDITING && point != null && !hasRouteWithEndPoint(
//                point
//            )
            marker.position = if (point != null) {
                GeoPointWithElevation(
                    point.coordinates.latitude,
                    point.coordinates.longitude
                )
            } else {
                GeoPointWithElevation(0.0, 0.0)
            }
        }
    }

    private fun updateRouteEditor() {
        val startPoint = points[0]
        val finishPoint = points[1]

        if (startPoint == null || finishPoint == null) {
            return
        }
        routeEditor.setRouteParams(
            RouteEditorRouteParams(
                startPoint = startPoint,
                finishPoint = finishPoint,
                routeSearchOptions = routeType.toRouteSearchOptions()
            )
        )
    }

    private fun updateRouteEditorSource() {
        routeEditorSource.setRoutesVisible(
            state.value == State.ROUTE_EDITING && routes.isNotEmpty()
        )
    }

    private fun onRoutesChanged() {
        updateMarkers()
        updateRouteEditorSource()
        updateCanStartNavigation()
        if (routes.isEmpty() && points.all { it != null }) {
            messageCallback?.invoke("Failed to find route")
        }
    }

    fun startNavigation() {
        val route = activeRoute
        val finishPoint = points[1]
        if (finishPoint != null && (route != null || !useSimulation)) {
            val options = RouteBuildOptions(
                finishPoint = finishPoint,
                routeSearchOptions = routeType.toRouteSearchOptions()
            )
            if (useSimulation) {
                navigationManager.startSimulation(options, route!!)
            } else {
                navigationManager.start(options, route)
            }
        } else {
            navigationManager.start()
        }
        setState(State.NAVIGATION)
    }

    fun stopNavigation() {
        navigationManager.stop()
        setState(State.ROUTE_EDITING)
    }

    private fun setState(state: State) {
        _state.value = state
        updateRouteEditorSource()
        updateMarkers()
    }

    private fun updateCanStartNavigation() {
        val bothPoints = points.all { it != null }
        _canStartNavigation.value =
            if (bothPoints) routes.isNotEmpty() else (!useSimulation && points[0] == null)
    }
}

private fun NavigationViewModel.RouteType.toRouteSearchOptions(): RouteSearchOptions =
    when (this) {
        NavigationViewModel.RouteType.CAR -> RouteSearchOptions(CarRouteSearchOptions())
        NavigationViewModel.RouteType.PEDESTRIAN -> RouteSearchOptions(
            PedestrianRouteSearchOptions()
        )

        NavigationViewModel.RouteType.BICYCLE -> RouteSearchOptions(BicycleRouteSearchOptions())
        NavigationViewModel.RouteType.PUBLIC_TRANSPORT -> RouteSearchOptions(
            PublicTransportRouteSearchOptions()
        )

        NavigationViewModel.RouteType.SCOOTER -> RouteSearchOptions(ScooterRouteSearchOptions())
        NavigationViewModel.RouteType.TAXI -> RouteSearchOptions(
            TaxiRouteSearchOptions(
                CarRouteSearchOptions()
            )
        )
    }
