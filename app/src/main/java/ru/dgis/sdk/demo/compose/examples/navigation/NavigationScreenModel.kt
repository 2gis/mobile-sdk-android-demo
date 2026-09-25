package ru.dgis.sdk.demo.compose.examples.navigation

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.dgis.sdk.Context
import ru.dgis.sdk.DgisObjectId
import ru.dgis.sdk.LevelId
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.withElevation
import ru.dgis.sdk.map.DgisMapObject
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.navigation.RouteBuildOptions
import ru.dgis.sdk.routing.CarRouteSearchOptions
import ru.dgis.sdk.routing.PedestrianRouteSearchOptions
import ru.dgis.sdk.routing.RouteSearchOptions
import ru.dgis.sdk.routing.RouteSearchPoint
import ru.dgis.sdk.routing.TrafficRoute
import ru.dgis.sdk.routing.TrafficRouter
import ru.dgis.sdk.routing.BicycleRouteSearchOptions
import ru.dgis.sdk.compose.navigation.settings.Keys
import ru.dgis.sdk.compose.navigation.settings.NavigationSettingsRepository

enum class NavigationType {
    Car,
    Pedestrian,
    Bicycle,
}

enum class NavigationScreenState {
    SelectingStartPoint,
    SelectingRoutePoints,
    SearchingRoute,
    SetupRoute,
    Navigation,
}

/**
 * State machine for the navigation screen.
 *
 * Collects start / intermediate / finish points, builds a route (or starts free roam), and runs
 * navigation or simulation via [NavigationManager]. Use [clear] to reset the flow, remove markers,
 * and stop navigation.
 *
 * @param trafficRouter searches the route between the selected points.
 */
class NavigationScreenModel(
    private val sdkContext: Context,
    private val map: Map,
    private val navigationManager: NavigationManager,
    private val trafficRouter: TrafficRouter,
    private val settingsRepository: NavigationSettingsRepository,
) {
    private val objectManager by lazy { MapObjectManager(map) }

    private var startPoint: GeoPoint = GeoPoint(0.0, 0.0)
    private var finishPoint: GeoPoint = GeoPoint(0.0, 0.0)

    private var startLevelId: LevelId? = null
    private var startObjectId: DgisObjectId = DgisObjectId()
    private var finishLevelId: LevelId? = null
    private var finishObjectId: DgisObjectId = DgisObjectId()

    private var intermediatePoints = mutableListOf<RouteSearchPoint>()
    private var searchObjectsCloseable: AutoCloseable? = null
    private var findRouteAutoCloseable: AutoCloseable? = null
    private var foundRouteBuildOptions: RouteBuildOptions? = null
    private var foundRoute: TrafficRoute? = null

    private val _state = MutableStateFlow(NavigationScreenState.SelectingStartPoint)

    /**
     * State of the current [NavigationScreenModel].
     */
    var state = _state.asStateFlow()

    private fun createMarker(
        point: GeoPoint,
        image: Image,
        text: String? = null,
        levelId: LevelId? = null,
    ): Marker {
        return Marker(
            MarkerOptions(
                position = point.withElevation(),
                icon = image,
                iconWidth = LogicalPixel(30.0f),
                text = text,
                levelId = levelId,
            )
        )
    }

    private fun addIntermediateMarker(point: GeoPoint, id: Int) {
        objectManager.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_pin),
                text = id.toString(),
            )
        )
    }

    private fun setStartMarker(point: GeoPoint) {
        objectManager.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_start),
                levelId = startLevelId,
            )
        )
    }

    private fun setFinishMarker(point: GeoPoint) {
        objectManager.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_finish),
                levelId = finishLevelId,
            )
        )
    }

    private fun removeMarkers() {
        objectManager.removeAll()
    }

    fun addPoint(geoPoint: GeoPoint) {
        when (_state.value) {
            NavigationScreenState.SelectingStartPoint -> {
                startPoint = geoPoint
                searchObject(geoPoint) { levelId, objectId ->
                    startLevelId = levelId
                    startObjectId = objectId
                    setStartMarker(geoPoint)
                }
                _state.value = NavigationScreenState.SelectingRoutePoints
            }

            NavigationScreenState.SelectingRoutePoints -> {
                finishPoint = geoPoint
                searchObject(geoPoint) { levelId, objectId ->
                    finishLevelId = levelId
                    finishObjectId = objectId
                    setFinishMarker(geoPoint)
                }
                _state.value = NavigationScreenState.SetupRoute
            }

            else -> {
                // The route searching has been started.
                assert(findRouteAutoCloseable != null)
                clear()
            }
        }
    }

    private fun searchObject(geoPoint: GeoPoint, callback: (LevelId?, DgisObjectId) -> Unit) {
        searchObjectsCloseable?.close()
        searchObjectsCloseable = null

        val mapPoint = map.camera.projection.mapToScreen(geoPoint)
        if (mapPoint == null) {
            callback(null, DgisObjectId())
            return
        }

        searchObjectsCloseable = map.getRenderedObjects(mapPoint).apply {
            onResult { objects ->
                searchObjectsCloseable?.close()
                searchObjectsCloseable = null

                var levelId: LevelId? = null
                var objectId = DgisObjectId()
                objects.firstOrNull()?.let { obj ->
                    if (obj.item.item is DgisMapObject) {
                        levelId = obj.item.levelId
                        objectId = (obj.item.item as DgisMapObject).id
                    }
                }
                callback(levelId, objectId)
            }
            onError {
                searchObjectsCloseable?.close()
                searchObjectsCloseable = null

                Log.d(
                    "NavigationScreenModel",
                    "couldn't find object for point: ${it.message}"
                )
                callback(null, DgisObjectId())
            }
        }
    }

    /**
     * Starts navigation on the selected points: free roam without a route, otherwise searches
     * a route for [navigationType] and runs navigation or its simulation along it.
     */
    fun navigate(
        navigationType: NavigationType,
        isFreeRoamEnabled: Boolean,
        useSimulation: Boolean,
    ) {
        if (_state.value != NavigationScreenState.SetupRoute) {
            return
        }

        navigationManager.mapManager.addMap(map)

        if (isFreeRoamEnabled) {
            navigationManager.start()
            removeMarkers()
            _state.value = NavigationScreenState.Navigation
            return
        }

        _state.value = NavigationScreenState.SearchingRoute

        when (navigationType) {
            NavigationType.Car -> {
                searchRoute(
                    useSimulation = useSimulation,
                    startPoint = RouteSearchPoint(startPoint),
                    intermediatePoints = intermediatePoints,
                    finishPoint = RouteSearchPoint(finishPoint),
                    routeSearchOptions = RouteSearchOptions(
                        CarRouteSearchOptions(
                            avoidTollRoads = settingsRepository[Keys.CAR_AVOID_TOLL_ROADS],
                            avoidUnpavedRoads = settingsRepository[Keys.CAR_AVOID_UNPAVED_ROADS],
                            avoidFerries = settingsRepository[Keys.CAR_AVOID_FERRIES],
                            avoidLockedRoads = settingsRepository[Keys.CAR_AVOID_LOCKED_ROADS],
                            routeSearchType = settingsRepository[Keys.CAR_ROUTE_SEARCH_TYPE],
                        )
                    ),
                )
            }

            NavigationType.Pedestrian -> {
                val startSearchPoint = RouteSearchPoint(
                    coordinates = startPoint,
                    objectId = startObjectId,
                    levelId = startLevelId,
                )
                val routeFinishPoint = RouteSearchPoint(
                    coordinates = finishPoint,
                    levelId = finishLevelId,
                    objectId = finishObjectId,
                )

                searchRoute(
                    useSimulation = useSimulation,
                    startPoint = startSearchPoint,
                    intermediatePoints = intermediatePoints,
                    finishPoint = routeFinishPoint,
                    routeSearchOptions = RouteSearchOptions(
                        PedestrianRouteSearchOptions(
                            avoidStairways = settingsRepository[Keys.PEDESTRIAN_AVOID_STAIRWAYS],
                            avoidUnderpassesAndOverpasses = settingsRepository[Keys.PEDESTRIAN_AVOID_UNDERPASSES],
                            useIndoor = settingsRepository[Keys.PEDESTRIAN_USE_INDOOR],
                            avoidUnpavedRoads = settingsRepository[Keys.PEDESTRIAN_AVOID_UNPAVED_ROADS],
                        )
                    ),
                )
            }

            NavigationType.Bicycle -> {
                searchRoute(
                    useSimulation = useSimulation,
                    startPoint = RouteSearchPoint(startPoint),
                    intermediatePoints = intermediatePoints,
                    finishPoint = RouteSearchPoint(finishPoint),
                    routeSearchOptions = RouteSearchOptions(
                        BicycleRouteSearchOptions(
                            avoidCarRoads = settingsRepository[Keys.BICYCLE_AVOID_CAR_ROADS],
                            avoidStairways = settingsRepository[Keys.BICYCLE_AVOID_STAIRWAYS],
                            avoidUnderpassesAndOverpasses = settingsRepository[Keys.BICYCLE_AVOID_UNDERPASSES],
                            avoidUnpavedRoads = settingsRepository[Keys.BICYCLE_AVOID_UNPAVED_ROADS],
                        )
                    ),
                )
            }
        }
    }

    private fun searchRoute(
        useSimulation: Boolean,
        startPoint: RouteSearchPoint,
        intermediatePoints: List<RouteSearchPoint>,
        finishPoint: RouteSearchPoint,
        routeSearchOptions: RouteSearchOptions,
    ) {
        assert(findRouteAutoCloseable == null)

        foundRouteBuildOptions = null
        foundRoute = null

        findRouteAutoCloseable = trafficRouter.findRoute(
            startPoint = startPoint,
            finishPoint = finishPoint,
            intermediatePoints = intermediatePoints,
            routeSearchOptions = routeSearchOptions
        ).apply {
            onResult {
                val route = it.firstOrNull() ?: return@onResult
                foundRouteBuildOptions = RouteBuildOptions(
                    finishPoint = finishPoint,
                    routeSearchOptions = routeSearchOptions,
                )
                foundRoute = route

                if (useSimulation) {
                    navigationManager.startSimulation(foundRouteBuildOptions!!, foundRoute!!)
                } else {
                    navigationManager.start(foundRouteBuildOptions!!, foundRoute!!)
                }

                removeMarkers()
                _state.value = NavigationScreenState.Navigation
            }
            onError {
                Log.e("NavigationScreenModel", "couldn't find route: ${it.message}")
                clear()
            }
        }
    }

    fun addPoint(point: ScreenPoint) {
        val geoPoint = map.camera.projection.screenToMap(point) ?: return
        addPoint(geoPoint)
    }

    private fun addIntermediatePoint(geoPoint: GeoPoint) {
        if (_state.value == NavigationScreenState.SelectingRoutePoints) {
            intermediatePoints.add(RouteSearchPoint(geoPoint))
            addIntermediateMarker(geoPoint, intermediatePoints.size)
        }
    }

    fun addIntermediatePoint(point: ScreenPoint) {
        val geoPoint = map.camera.projection.screenToMap(point) ?: return
        addIntermediatePoint(geoPoint)
    }

    fun clear() {
        _state.value = NavigationScreenState.SelectingStartPoint

        intermediatePoints.clear()

        startLevelId = null
        startObjectId = DgisObjectId()
        finishLevelId = null
        finishObjectId = DgisObjectId()

        searchObjectsCloseable?.close()
        searchObjectsCloseable = null

        findRouteAutoCloseable?.close()
        findRouteAutoCloseable = null
        foundRouteBuildOptions = null
        foundRoute = null

        navigationManager.stop()
        navigationManager.mapManager.removeMap(map)

        objectManager.removeAll()
    }
}