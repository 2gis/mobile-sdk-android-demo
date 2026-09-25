package ru.dgis.sdk.demo.compose.examples.sharedroute

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import ru.dgis.sdk.Context
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.await
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.withElevation
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.geometry.ComplexGeometry
import ru.dgis.sdk.geometry.PointGeometry
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Padding
import ru.dgis.sdk.map.RouteMapObject
import ru.dgis.sdk.map.RouteMapObjectSource
import ru.dgis.sdk.map.RouteVisualizationType
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.calcPosition
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.routing.RouteIndex
import ru.dgis.sdk.routing.SharedRouteData
import ru.dgis.sdk.routing.TrafficRoute
import ru.dgis.sdk.routing.TrafficRouter

/**
 * @param okHttpClient client for the requests to the routing API that save a route.
 * @param apiKey key of the routing API.
 * @param appId application id sent to the routing API in the X-App-Id header.
 */
open class SharedRouteScreenViewModel(
    private val sdkContext: Context,
    private val okHttpClient: OkHttpClient,
    private val apiKey: String,
    private val appId: String
) : ViewModel(), TouchEventsObserver {

    private val ROUTING_URL: String =
        "https://routing.api.2gis.com/routing/7.0.0/global?key=$apiKey"

    private val trafficRouter = TrafficRouter(sdkContext)

    private var map: Map? = null
    private var objectManager: MapObjectManager? = null
    private var sharedRouteSource: RouteMapObjectSource? = null

    private var startPoint: GeoPoint? = null
    private var finishPoint: GeoPoint? = null

    private val _pointsState = mutableStateOf(PointsState())
    val pointsState: State<PointsState> = _pointsState

    private val _saveRouteState = MutableStateFlow<SaveRouteState>(SaveRouteState.Idle)
    val saveRouteState: StateFlow<SaveRouteState> = _saveRouteState.asStateFlow()

    private val _requestId = MutableStateFlow("")
    val requestId: StateFlow<String> = _requestId.asStateFlow()

    private val _sharedRouteState =
        MutableStateFlow<SharedRouteFetchState>(SharedRouteFetchState.Idle)
    val sharedRouteState: StateFlow<SharedRouteFetchState> = _sharedRouteState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onMapReady(map: Map?) {
        if (this.map == map) return
        this.map = map
        if (map != null) {
            objectManager = MapObjectManager(map)
            sharedRouteSource = RouteMapObjectSource(sdkContext, RouteVisualizationType.NORMAL)
                .also { source -> map.addSource(source) }
        }
    }

    override fun onLongTouch(point: ScreenPoint) {
        val map = this.map ?: return
        val geoPoint = map.camera.projection.screenToMap(point) ?: return

        if (startPoint == null) {
            startPoint = geoPoint
            setStartMarker(geoPoint)
            _pointsState.value = _pointsState.value.copy(start = geoPoint)
            _message.value = "Start point set"
        } else if (finishPoint == null) {
            finishPoint = geoPoint
            setFinishMarker(geoPoint)
            _pointsState.value = _pointsState.value.copy(finish = geoPoint)
            _message.value = "Finish point set"
            saveRoute(startPoint!!, finishPoint!!)
        } else {
            clearPoints()
            startPoint = geoPoint
            setStartMarker(geoPoint)
            _pointsState.value = PointsState(start = geoPoint)
            _message.value = "Start point set"
        }
    }

    fun clearPoints() {
        objectManager?.removeAll()
        startPoint = null
        finishPoint = null
        _pointsState.value = PointsState()
        _saveRouteState.value = SaveRouteState.Idle
    }

    private fun saveRoute(start: GeoPoint, finish: GeoPoint) {
        _saveRouteState.value = SaveRouteState.Loading
        viewModelScope.launch {
            try {
                val requestId = withContext(Dispatchers.IO) {
                    sendSaveRouteRequest(start, finish)
                }
                if (requestId != null) {
                    _saveRouteState.value = SaveRouteState.Idle
                    _message.value = "Route ID saved"
                    _requestId.value = requestId
                } else {
                    _saveRouteState.value = SaveRouteState.Error("X-Request-Id not found in the response")
                    _message.value = "Failed to get request_id"
                }
            } catch (e: Exception) {
                _saveRouteState.value = SaveRouteState.Error(e.message ?: "Unknown error")
                _message.value = "Request failed: ${e.message}"
            }
        }
    }

    private fun sendSaveRouteRequest(start: GeoPoint, finish: GeoPoint): String? {
        val jsonBody = JSONObject().apply {
            val pointsArray = JSONArray()
            pointsArray.put(
                JSONObject().apply {
                    put("lon", start.longitude.value)
                    put("lat", start.latitude.value)
                    put("type", "stop")
                    put("start", true)
                }
            )
            pointsArray.put(
                JSONObject().apply {
                    put("lon", finish.longitude.value)
                    put("lat", finish.latitude.value)
                    put("type", "stop")
                }
            )
            put("points", pointsArray)
            put("save_route", true)
        }.toString()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(ROUTING_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-App-Id", appId)
            .build()

        val response = okHttpClient.newCall(request).execute()
        return response.use {
            if (!it.isSuccessful) {
                val errorBody = it.body?.string() ?: "No error body"
                throw RuntimeException("HTTP ${it.code}: $errorBody")
            }
            it.header("X-Request-Id")
        }
    }

    fun updateRequestId(id: String) {
        _requestId.value = id
    }

    fun fetchSharedRoute(requestId: String) {
        if (requestId.isBlank()) {
            sharedRouteSource?.clear()
            _sharedRouteState.value = SharedRouteFetchState.Idle
            return
        }

        _sharedRouteState.value = SharedRouteFetchState.Loading
        viewModelScope.launch {
            try {
                val sharedRouteData: SharedRouteData =
                    trafficRouter.fetchSharedRoute(requestId).await()
                val routes = sharedRouteData.routes

                if (routes.isNotEmpty()) {
                    sharedRouteSource?.let { source ->
                        source.clear()
                        routes.forEachIndexed { index, route ->
                            val routeMapObject = RouteMapObject(
                                route,
                                index == 0,
                                RouteIndex(index.toLong())
                            )
                            source.addObject(routeMapObject)
                        }
                    }
                    _sharedRouteState.value = SharedRouteFetchState.Success(routes.size)
                    _message.value = "Route found (${routes.size})"
                    fitRoutesToScreen(routes)
                } else {
                    sharedRouteSource?.clear()
                    _sharedRouteState.value = SharedRouteFetchState.Empty
                    _message.value = "Route not found"
                }
            } catch (e: Exception) {
                sharedRouteSource?.clear()
                _sharedRouteState.value = SharedRouteFetchState.Error(e.message ?: "Unknown error")
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun clearSharedRoute() {
        sharedRouteSource?.clear()
        _sharedRouteState.value = SharedRouteFetchState.Idle
    }

    private fun fitRoutesToScreen(routes: List<TrafficRoute>) {
        val camera = map?.camera ?: return
        if (routes.isEmpty()) return

        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                val geometries = routes.flatMap { route ->
                    route.route.geometry.entries.map { entry ->
                        PointGeometry(entry.value)
                    }
                }
                if (geometries.isNotEmpty()) {
                    val complexGeometry = ComplexGeometry(geometries)
                    val padding = Padding(150, 150, 150, 150)
                    val cameraPosition = calcPosition(camera, complexGeometry, null, padding)
                    camera.move(cameraPosition)
                }
            }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun createMarker(point: GeoPoint, image: Image): Marker =
        Marker(
            MarkerOptions(
                position = point.withElevation(),
                icon = image,
                iconWidth = LogicalPixel(30.0f)
            )
        )

    private fun setStartMarker(point: GeoPoint) {
        objectManager?.addObject(
            createMarker(point, imageFromResource(sdkContext, R.drawable.ic_start))
        )
    }

    private fun setFinishMarker(point: GeoPoint) {
        objectManager?.addObject(
            createMarker(point, imageFromResource(sdkContext, R.drawable.ic_finish))
        )
    }

    override fun onCleared() {
        super.onCleared()
        objectManager?.close()
        sharedRouteSource?.close()
    }
}

data class PointsState(
    val start: GeoPoint? = null,
    val finish: GeoPoint? = null
)

sealed interface SaveRouteState {
    data object Idle : SaveRouteState
    data object Loading : SaveRouteState
    data class Error(val message: String) : SaveRouteState
}

sealed interface SharedRouteFetchState {
    data object Idle : SharedRouteFetchState
    data object Loading : SharedRouteFetchState
    data class Success(val routeCount: Int) : SharedRouteFetchState
    data object Empty : SharedRouteFetchState
    data class Error(val message: String) : SharedRouteFetchState
}
