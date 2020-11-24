package ru.dgis.sdk.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.sdk.Connection
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.directory.DirectoryObjectId
import ru.dgis.sdk.geometry.GeometryCreator
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.routing.*

class TrafficRouterActivity : AppCompatActivity(), TouchEventsObserver {
    private lateinit var routeEditor: RouteEditor
    private lateinit var routeEditorSource: RouteEditorSource
    private lateinit var routeIndexSpinner: Spinner
    private lateinit var mapView: MapView
    private var map: Map? = null
    private var geometryMapObjectSource: GeometryMapObjectSource? = null

    private var connections = mutableListOf<Connection>()

    // Начальная точка маршрута. По умолчанию: Театральная площадь, Большой театр
    private var startPoint: GeoPoint = GeoPoint(Arcdegree(55.759909), Arcdegree(37.618806))

    // Конечная точка маршрута. По умолчанию: Кремль
    private var finishPoint: GeoPoint = GeoPoint(Arcdegree(55.752425), Arcdegree(37.613983))

    // Объекты карты, соответствующие начальной и конечной точкам маршрута. Показываются на карте,
    // когда маршрут ещё не построен. Когда маршрут уже построен, точки начала и конца маршрута
    // отрисовываются вместе с ним, и эти переменные проставляются в null.
    private var startPointMapObject: GeometryMapObject? = null
    private var finishPointMapObject: GeometryMapObject? = null

    private var inProgressToast: Toast? = null
    private var routeDisplayed: Boolean = false

    private var dragRoutePoint: RoutePointMapObject? = null
    private var dragGeometryPoint: GeometryMapObject? = null
    private var dragPoint: GeoPoint? = null

    inner class SpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            parent ?: return
            val item = parent.getItemAtPosition(pos) as String?
            item ?: return
            val index = item.toIntOrNull()
            index ?: return
            routeEditor.setActiveRouteIndex(RouteIndex(value = index.toLong()))
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            routeEditor.setActiveRouteIndex(RouteIndex(value = 0))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_traffic_router)

        routeEditor = RouteEditor(sdkContext)

        routeEditorSource = createRouteEditorSource(sdkContext, routeEditor)!!
        routeEditorSource.setRoutesVisible(false)

        connections.add(routeEditor.routesInfo().connect(this::handleRoutes))

        val findRouteButton = findViewById<FloatingActionButton>(R.id.startRouteSearch).apply {
            isEnabled = false
        }

        val clearRouteMapObjectsButton = findViewById<FloatingActionButton>(R.id.clear).apply {
            isEnabled = false
        }

        mapView = findViewById<MapView>(R.id.mapView)
        lifecycle.addObserver(mapView)

        mapView.getMapAsync { m ->
            map = m

            findRouteButton.isEnabled = true
            clearRouteMapObjectsButton.isEnabled = true

            geometryMapObjectSource = GeometryMapObjectSourceBuilder(sdkContext).createSource()
            if (geometryMapObjectSource != null) {
                m.addSource(geometryMapObjectSource!!)
            }

            m.addSource(routeEditorSource)

            createRouteMapPoint(startPoint, true)
            createRouteMapPoint(finishPoint, false)
        }

        findRouteButton.setOnClickListener {
            findRoute()
        }

        clearRouteMapObjectsButton.setOnClickListener {
            clearRouteMapObjects()
        }

        mapView.setTouchEventsObserver(this)

        routeIndexSpinner = findViewById<Spinner>(R.id.routeIndexSpinner)
        routeIndexSpinner.onItemSelectedListener = SpinnerListener()
        routeIndexSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, Array(0) {""})
    }

    override fun onDestroy() {
        connections.forEach(Connection::disconnect)
        super.onDestroy()
    }

    override fun onTap(point: ViewportPoint) {
        // TODO: вся эта логика должна быть внутри RouteEditor'a, иначе зачем он вообще?
        mapView.getRenderedObjects(point, ScreenDistance(5f)).onResult { renderedObjectInfos ->
            if (renderedObjectInfos.isEmpty()) {
                return@onResult
            }

            var routeMapObject: RouteMapObject? = null
            for (renderObjectInfo in renderedObjectInfos) {
                routeMapObject = renderObjectInfo.item.item as? RouteMapObject
                if (routeMapObject != null) {
                    val isActive = routeMapObject.isActive().value
                    if (isActive) {
                        continue
                    }

                    break
                }
            }

            if (routeMapObject == null) {
                return@onResult
            }

            val routeIndex = routeMapObject.routeIndex()
            routeEditor.setActiveRouteIndex(routeIndex)
            routeIndexSpinner.setSelection(routeIndex.value.toInt())
        }
    }

    override fun onLongTouch(point: ViewportPoint) {
        map?.camera?.projection()?.screenToMap(point)?.let {
            if (routeDisplayed) {
                return
            }

            if (startPointMapObject == null) {
                startPoint = it
                createRouteMapPoint(startPoint, true)
                notify("задана начальная точка маршрута")

            } else {
                finishPoint = it
                createRouteMapPoint(finishPoint, false)
                notify("Задана конечная точка маршрута")

                if (routeDisplayed) {
                    findRoute()
                }
                Unit
            }
        }
    }

    override fun onDragBegin(data: DragBeginData) {
        dragRoutePoint = data.item.item as? RoutePointMapObject
        dragGeometryPoint = data.item.item as? GeometryMapObject
        dragPoint = map!!.camera.projection().screenToMap(data.point)
    }

    override fun onDragMove(point: ViewportPoint) {
        val newPoint = map!!.camera.projection().screenToMap(point)!!
        val shift = GeoPoint(Arcdegree(newPoint.latitude.value - dragPoint!!.latitude.value), Arcdegree(newPoint.longitude.value - dragPoint!!.longitude.value))
        dragRoutePoint?.setShift(shift)
        dragGeometryPoint?.setShift(shift)
        dragPoint = newPoint

        if (dragRoutePoint != null) {
            if (dragRoutePoint!!.kind() == RoutePointKind.START) {
                startPoint = newPoint
            } else if (dragRoutePoint!!.kind() == RoutePointKind.FINISH) {
                finishPoint = newPoint
            }
        }
        else if (dragGeometryPoint != null) {
            if (dragGeometryPoint == startPointMapObject) {
                startPoint = newPoint
            } else if (dragGeometryPoint == finishPointMapObject) {
                finishPoint = newPoint
            }
        }
    }

    override fun onDragEnd() {
        dragRoutePoint = null
        dragGeometryPoint = null
        dragPoint = null

        if (routeDisplayed) {
            findRoute()
        }
    }

    private fun findRoute() {
        if (!routeDisplayed) {
            if (startPointMapObject == null) {
                notify("Задайте начальную точку маршрута")
                return
            }

            if (finishPointMapObject == null) {
                notify("Задайте конечную точку маршрута")
                return
            }
        }

        inProgressToast = notify("Ищем маршрут...")

        val startSearchPoint = RouteSearchPoint(
            coordinates = startPoint,
            course = null,
            objectId = DirectoryObjectId(0)
        )

        val finishSearchPoint = RouteSearchPoint(
            coordinates = finishPoint,
            course = null,
            objectId = DirectoryObjectId(1)
        )

        val routeOptions = RouteOptions(
            avoidTollRoads = false,
            avoidUnpavedRoads = false,
            avoidFerry = false
        )

        if (this::routeEditorSource.isInitialized) {
            routeEditorSource.setRoutesVisible(false)
        }

        routeEditor.setRouteParams(RouteParams(
            startPoint = startSearchPoint,
            finishPoint = finishSearchPoint,
            routeOptions = routeOptions
        ))
    }

    private fun handleRoutes(routesInfo: RouteEditorRoutesInfo) {
        val toast = inProgressToast ?: return
        toast.cancel()

        val routes = routesInfo.routes
        val message = if (routes.isNotEmpty()) "Маршрут найден" else "Не удалось найти маршрут"
        notify(message)

        // Удаляем точки начала и конца маршрута, оформленные как геометрические объекты. Вместо
        // них будут точки, отрисованные вместе с маршрутом
        clearRouteMapPoints()

        routeEditorSource.setRoutesVisible(true)
        routeDisplayed = true

        var routeIndices = Array(0) {""}
        if (routes.size > 1) {
            routeIndices = Array(routes.size) { i -> i.toString() }
        }
        routeIndexSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, routeIndices)
    }

    private fun createRouteMapPoint(point: GeoPoint, isStart: Boolean) {
        val source = geometryMapObjectSource ?: return

        if (isStart) {
            if (startPointMapObject != null) {
                source.removeObject(startPointMapObject)
            }

            startPointMapObject = createMapPoint(point, true)
            source.addObject(startPointMapObject)
        }
        else {
            if (finishPointMapObject != null) {
                source.removeObject(finishPointMapObject)
            }

            finishPointMapObject = createMapPoint(point, false)
            source.addObject(finishPointMapObject)
        }
    }

    private fun createMapPoint(point: GeoPoint, isStart: Boolean): GeometryMapObject = GeometryMapObjectBuilder()
        .setGeometry(GeometryCreator.createPointGeometry(point))
        .setDraggable(true)
        .setObjectAttribute("db_sublayer", if (isStart) "s_dvg_transport_point_a" else "s_dvg_transport_point_b")
        .createObject()!!

    private fun clearRouteMapObjects() {
        clearRouteMapPoints()
        clearRouteMapRoutes()
    }

    private fun clearRouteMapPoints() {
        val source = geometryMapObjectSource ?: return

        if (startPointMapObject != null) {
            source.removeObject(startPointMapObject)
            startPointMapObject = null
        }

        if (finishPointMapObject != null) {
            source.removeObject(finishPointMapObject)
            finishPointMapObject = null
        }
    }

    private fun clearRouteMapRoutes() {
        if (this::routeEditorSource.isInitialized) {
            routeEditorSource.setRoutesVisible(false)
            routeDisplayed = false
        }

        routeIndexSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, Array(0) {""})
    }

    private fun notify(msg: String): Toast = Toast
        .makeText(this, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.TOP, 0, 42)
            show()
        }
}
