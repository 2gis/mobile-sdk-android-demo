package ru.dgis.sdk.demo.compose.examples.routeEditor

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dgis.sdk.Context
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.await
import ru.dgis.sdk.compose.navigation.settings.NavigationSettingsRepository
import ru.dgis.sdk.compose.routeeditor.BriefRouteDurationProvider
import ru.dgis.sdk.compose.routeeditor.DefaultRouteEditorViewModel
import ru.dgis.sdk.compose.routeeditor.RepositoryBackedRouteSearchOptionsProvider
import ru.dgis.sdk.compose.routeeditor.RouteUiPoint
import ru.dgis.sdk.compose.routeeditor.TransportMode
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.withElevation
import ru.dgis.sdk.directory.SearchManager
import ru.dgis.sdk.geometry.ComplexGeometry
import ru.dgis.sdk.geometry.PointGeometry
import ru.dgis.sdk.geometry.point
import ru.dgis.sdk.map.DgisMapObject
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Padding
import ru.dgis.sdk.map.RouteEditorSource
import ru.dgis.sdk.map.RouteMapObject
import ru.dgis.sdk.map.RouteMapObjectCalloutLabelFlag
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.calcPosition
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.routing.RouteEditor
import ru.dgis.sdk.routing.RouteSearchPoint
import ru.dgis.sdk.routing.TrafficRoute
import ru.dgis.sdk.routing.TrafficRouter
import java.util.EnumSet
import android.content.Context as AndroidContext

class RouteEditorScreenViewModel(
    private val sdkContext: Context,
    appContext: AndroidContext,
) : ViewModel(),
    TouchEventsObserver {
    companion object {
        const val DEFAULT_PADDING_HORIZONTAL = 40
        const val DEFAULT_PADDING_TOP = 100
        const val DEFAULT_PADDING_BOTTOM = 40
    }

    private var map: Map? = null

    private val trafficRouter = TrafficRouter(sdkContext)
    private val routeEditor = RouteEditor(sdkContext, trafficRouter)

    val settingsRepository = NavigationSettingsRepository.default(appContext)

    val roureEditorComposableVM: DefaultRouteEditorViewModel
    private val routePointTitleProvider: DirectoryRoutePointTitleProvider

    init {
        val searchManager = SearchManager.createSmartManager(sdkContext)

        roureEditorComposableVM = DefaultRouteEditorViewModel(
            routeEditor = routeEditor,
            briefRouteDurationProvider = BriefRouteDurationProvider(trafficRouter),
            routeSearchOptionsProvider = RepositoryBackedRouteSearchOptionsProvider(
                settingsRepository
            ),
            scheduleProvider = DirectoryScheduleProvider(searchManager),
            onStartNavigation = { trafficRoute, routeBuildOptions ->
                Log.i(
                    "RouteEditorScreen",
                    "Chosen route: $trafficRoute, build options: RouteBuildOptions(${routeBuildOptions.finishPoint}, ${routeBuildOptions.routeSearchOptions})",
                )
            },
            onChangeRoute = ::clearPoints,
        )

        routePointTitleProvider = DirectoryRoutePointTitleProvider(searchManager)
    }

    private val uiTransportMode
        get() = roureEditorComposableVM.uiState.value.selectedTransportMode

    private var objectManager: MapObjectManager? = null
    private var routeEditorSource: RouteEditorSource? = null

    // Point state
    private var startPoint: RouteSearchPoint? = null
    private var finishPoint: RouteSearchPoint? = null
    private var intermediatePoints = mutableListOf<RouteSearchPoint>()

    private val _arePointsSet = mutableStateOf(false)
    val arePointsSet: State<Boolean> = _arePointsSet

    private fun updatePointsState() {
        _arePointsSet.value = startPoint != null && finishPoint != null
    }

    private val routesFlow: StateFlow<List<TrafficRoute>> =
        callbackFlow {
            val connection =
                routeEditor.routesInfoChannel.connect { routes ->
                    trySend(routes.routes)
                }
            awaitClose { connection.close() }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val paddingFlow =
        MutableStateFlow<Padding>(
            Padding(
                left = DEFAULT_PADDING_HORIZONTAL,
                right = DEFAULT_PADDING_HORIZONTAL,
                top = DEFAULT_PADDING_TOP,
                bottom = DEFAULT_PADDING_BOTTOM
            )
        )

    init {
        viewModelScope.launch {
            combine(paddingFlow, routesFlow) { padding, routes ->
                padding to routes
            }.collect { (padding, routes) ->
                withContext(Dispatchers.Main) {
                    map?.camera?.padding = padding
                    if (routes.isNotEmpty()) {
                        routeEditorSource?.apply {
                            setRoutesVisible(true)
                            setShowOnlyActiveRoute(uiTransportMode == TransportMode.PUBLIC_TRANSPORT)
                        }
                        focusOnRoutes(routes)
                    }
                }
            }
        }
    }

    fun onMapReady(map: Map?) {
        if (this.map == map) {
            return
        }

        this.map = map
        if (map != null) {
            objectManager = MapObjectManager(map)
            routeEditorSource =
                RouteEditorSource(
                    sdkContext,
                    routeEditor,
                    activeCalloutLabelFlags = EnumSet.of(
                        RouteMapObjectCalloutLabelFlag.DURATION,
                        RouteMapObjectCalloutLabelFlag.LENGTH
                    ),
                    inactiveCalloutLabelFlags = EnumSet.of(
                        RouteMapObjectCalloutLabelFlag.DURATION,
                        RouteMapObjectCalloutLabelFlag.LENGTH
                    )
                ).also {
                    map.addSource(it)
                }
        }
    }

    override fun onTap(point: ScreenPoint) {
        if (arePointsSet.value) {
            map?.getRenderedObjects(point)?.onResult { objectInfos ->
                objectInfos.forEach { objInfo ->
                    if (objInfo.item.item is RouteMapObject) {
                        routeEditor.setActiveRouteIndex((objInfo.item.item as RouteMapObject).routeIndex)
                        return@onResult
                    }
                }
            }
            return
        }

        addIntermediatePoint(point)
    }

    override fun onLongTouch(point: ScreenPoint) {
        val map = this.map ?: return
        viewModelScope.launch {
            val routeSearchPoint = point.toRouteSearchPoint(map)

            if (routeSearchPoint != null) {
                addPoint(routeSearchPoint)
            }
        }
    }

    fun clearPoints() {
        objectManager?.removeAll()
        startPoint = null
        finishPoint = null
        intermediatePoints.clear()
        routeEditorSource?.setRoutesVisible(false)
        updatePointsState()
    }

    override fun onCleared() {
        super.onCleared()
        roureEditorComposableVM.close()
    }

    private fun focusOnRoutes(routes: List<TrafficRoute>) {
        val camera = map?.camera ?: return
        val geometries =
            routes
                .map { route ->
                    route.route.geometry.entries
                        .map { entry ->
                            PointGeometry(entry.value)
                        }
                }.flatten()

        ComplexGeometry(geometries).apply {
            camera.move(calcPosition(camera, this))
        }
    }

    fun updateCameraPadding(padding: Padding) {
        paddingFlow.compareAndSet(paddingFlow.value, padding)
    }

    private fun createMarker(
        point: GeoPoint,
        image: Image,
        text: String? = null,
    ): Marker =
        Marker(
            MarkerOptions(
                position = point.withElevation(),
                icon = image,
                iconWidth = LogicalPixel(30.0f),
                text = text,
            ),
        )

    private fun addIntermediateMarker(
        point: GeoPoint,
        id: Int,
    ) {
        objectManager?.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_pin),
                text = id.toString(),
            ),
        )
    }

    private fun setStartMarker(point: GeoPoint) {
        objectManager?.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_start),
            ),
        )
    }

    private fun setFinishMarker(point: GeoPoint) {
        objectManager?.addObject(
            createMarker(
                point = point,
                image = imageFromResource(sdkContext, R.drawable.ic_finish),
            ),
        )
    }

    private fun addPoint(point: RouteSearchPoint) {
        if (finishPoint != null) {
            assert(startPoint != null)
        } else if (startPoint == null) {
            startPoint = point
            setStartMarker(point.coordinates)
        } else {
            finishPoint = point
            setFinishMarker(point.coordinates)

            viewModelScope.launch {
                val startUiPoint =
                    RouteUiPoint(
                        startPoint!!,
                        routePointTitleProvider.providePointTitle(startPoint!!),
                    )
                val finishUiPoint =
                    RouteUiPoint(
                        finishPoint!!,
                        routePointTitleProvider.providePointTitle(finishPoint!!),
                    )
                val intermediateUiPoints =
                    intermediatePoints.map {
                        RouteUiPoint(
                            it,
                            routePointTitleProvider.provideIntermediatePointTitle(
                                it,
                                intermediatePoints.indexOf(it),
                            ),
                        )
                    }

                roureEditorComposableVM.setPoints(
                    startUiPoint,
                    finishUiPoint,
                    intermediateUiPoints,
                )

                updatePointsState()
            }
        }
    }

    private fun addIntermediatePoint(point: ScreenPoint) {
        if (startPoint == null || finishPoint != null) {
            return
        }
        val map = this.map ?: return
        viewModelScope.launch {
            point.toRouteSearchPoint(map)?.let {
                intermediatePoints.add(it)
                addIntermediateMarker(it.coordinates, intermediatePoints.size)
            }
        }
    }
}

private suspend fun ScreenPoint.toRouteSearchPoint(map: Map): RouteSearchPoint? {
    return map.getRenderedObjects(this).await().let { objectInfos ->
        if (objectInfos.firstOrNull()?.item?.item is DgisMapObject) {
            val objInfo = objectInfos.first()
            return RouteSearchPoint(
                coordinates = objInfo.closestMapPoint.point,
                objectId = (objInfo.item.item as DgisMapObject).id,
                levelId = objInfo.item.levelId,
            )
        } else {
            val geoPoint = map.camera.projection.screenToMap(this) ?: return@let null
            return RouteSearchPoint(geoPoint)
        }
    }
}
