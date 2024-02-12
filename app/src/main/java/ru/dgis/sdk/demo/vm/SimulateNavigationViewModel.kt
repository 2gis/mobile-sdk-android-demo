package ru.dgis.sdk.demo.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.geometry.point
import ru.dgis.sdk.map.DragBeginData
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.RouteEditorSource
import ru.dgis.sdk.map.RouteMapObjectDisplayFlag
import ru.dgis.sdk.routing.CarRouteSearchOptions
import ru.dgis.sdk.routing.Route
import ru.dgis.sdk.routing.RouteEditor
import ru.dgis.sdk.routing.RouteEditorRouteParams
import ru.dgis.sdk.routing.RouteSearchOptions
import ru.dgis.sdk.routing.RouteSearchPoint
import java.util.EnumSet

private val disabledRouteFlags = EnumSet.of(
    RouteMapObjectDisplayFlag.START_POINT,
    RouteMapObjectDisplayFlag.FINISH_POINT,
    RouteMapObjectDisplayFlag.BUBBLES
)

enum class MarkerUserData(val value: String) {
    START("start"),
    FINISH("finish")
}

data class RouteSearchPointWithMarker(
    val marker: Marker,
    val routeSearchPoint: RouteSearchPoint
)
class SimulateNavigationViewModel : ViewModel() {
    private val sdkContext = DGis.context()

    private val routeEditor by lazy { RouteEditor(sdkContext) }

    val routeEditorSource by lazy {
        RouteEditorSource(
            sdkContext,
            routeEditor,
            activeDisplayFlags = EnumSet.complementOf(
                disabledRouteFlags
            )
        )
    }

    private val _routeFlow: MutableStateFlow<Route?> = MutableStateFlow(null)
    val routeFlow: StateFlow<Route?> = _routeFlow

    private val closeables: MutableList<AutoCloseable> = mutableListOf()

    private val startPointFlow: MutableStateFlow<RouteSearchPointWithMarker?> = MutableStateFlow(null)
    private val finishPointFlow: MutableStateFlow<RouteSearchPointWithMarker?> = MutableStateFlow(null)
    private val combinedPointsFlow: Flow<RouteEditorRouteParams?> = combine(
        startPointFlow,
        finishPointFlow
    ) { start, finish ->
        if (start != null && finish != null) {
            RouteEditorRouteParams(
                start.routeSearchPoint,
                finish.routeSearchPoint,
                RouteSearchOptions(CarRouteSearchOptions())
            )
        } else {
            null
        }
    }

    private var dragRoutePoint: Marker? = null
    private val dragData: MutableSharedFlow<GeoPoint> = MutableSharedFlow()

    init {
        routeEditorSource.setShowOnlyActiveRoute(true)

        viewModelScope.launch {
            dragData.collect {
                dragRoutePoint?.position = GeoPointWithElevation(it)
            }
        }

        viewModelScope.launch {
            combinedPointsFlow.collect {
                if (it != null) {
                    routeEditor.setRouteParams(it)
                }
            }
        }

        closeables.add(
            routeEditor.routesInfoChannel.connect {
                it.routes.firstOrNull()?.let { trafficRoute ->
                    _routeFlow.value = trafficRoute.route
                }
            }
        )
    }

    fun updateStartPoint(marker: RouteSearchPointWithMarker) {
        startPointFlow.value = marker
    }
    fun updateFinishPoint(marker: RouteSearchPointWithMarker) {
        finishPointFlow.value = marker
    }

    fun onDragBegin(data: DragBeginData) {
        (data.item.item as? Marker)?.let {
            setDragRoutePoint(it)
        }
    }

    fun emitDragData(point: GeoPoint) {
        viewModelScope.launch {
            dragData.emit(point)
        }
    }

    fun onDragEnd() {
        dragRoutePoint?.let {
            when (it.userData) {
                MarkerUserData.START -> {
                    startPointFlow.value?.marker?.position = GeoPointWithElevation(it.position.point)
                    startPointFlow.value =
                        startPointFlow.value?.copy(routeSearchPoint = RouteSearchPoint(it.position.point))
                }
                MarkerUserData.FINISH -> {
                    finishPointFlow.value?.marker?.position = GeoPointWithElevation(it.position.point)
                    finishPointFlow.value =
                        finishPointFlow.value?.copy(routeSearchPoint = RouteSearchPoint(it.position.point))
                }
            }
        }
        dragRoutePoint = null
    }

    private fun setDragRoutePoint(point: Marker?) {
        dragRoutePoint = point as Marker
    }
}
