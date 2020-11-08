package ru.dgis.sdk.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.core.Connection
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.directory.DirectoryObjectId
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.routing.*

class TrafficRouterActivity : AppCompatActivity(), TouchEventsObserver {
    private lateinit var routeEditor: RouteEditor
    private lateinit var routeEditorSource: RouteEditorSource
    private lateinit var routeIndexSpinner: Spinner
    private var map: Map? = null
    private var viewport: Viewport? = null
    private var geometryMapObjectSource: GeometryMapObjectSource? = null

    private var connections = mutableListOf<Connection>()

    // Начальная точка маршрута. По умолчанию: Театральная площадь, Большой театр
    private var startPoint: GeoPoint = GeoPoint(Arcdegree(55.759909), Arcdegree(37.618806))

    // Конечная точка маршрута. По умолчанию: Кремль
    private var finishPoint: GeoPoint = GeoPoint(Arcdegree(55.752425), Arcdegree(37.613983))

    // Объекты карты, соответствующие начальной и конечной точкам маршрута
    private var startPointMapObject: GeometryMapObject? = null
    private var finishPointMapObject: GeometryMapObject? = null

    private var inProgressToast: Toast? = null
    private var routeDisplayed: Boolean = false

    inner class SpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
            parent ?: return
            val item = parent.getItemAtPosition(pos) as String?
            item ?: return
            val index = item.toIntOrNull()
            index ?: return
            routeEditor.setActiveRouteIndex(index)
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {
            routeEditor.setActiveRouteIndex(0)
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

        val mapView = findViewById<MapView>(R.id.mapView)
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

        mapView.getViewPortAsync {
            viewport = it
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
        // FIXME: will be available next release
        /*
        viewport?.getRenderedObjects(point, ScreenDistance(5f))?.onResult { renderedObjectInfos ->
            if (renderedObjectInfos.isEmpty()) {
                return@onResult
            }

            var routeMapObject: RouteMapObject? = null
            for (renderObjectInfo in renderedObjectInfos) {
                routeMapObject = tryCastToRouteMapObject(renderObjectInfo.item.item)
                if (routeMapObject != null) {
                    val isActive = routeMapObject.isActive().value!!
                    if (isActive) {
                        continue
                    }

                    break
                }
            }

            if (routeMapObject == null) {
                return@onResult
            }

            val routesInfo = routeEditor.routesInfo().value
            val index = routesInfo.routes.indexOfFirst {
                it!!.equals(routeMapObject.route()!!)
            }

            if (index < 0) {
                return@onResult
            }

            routeEditor.setActiveRouteIndex(index)
            routeIndexSpinner.setSelection(index)
        }
         */
    }

    override fun onLongTouch(point: ViewportPoint) {
        map?.camera?.projection()?.screenToMap(point)?.let {
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

    private fun findRoute() {
        if (startPointMapObject == null) {
            notify("Задайте начальную точку маршрута")
            return
        }

        if (finishPointMapObject == null) {
            notify("Задайте конечную точку маршрута")
            return
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

            startPointMapObject = createMapPoint(point)
            source.addObject(startPointMapObject)
        }
        else {
            if (finishPointMapObject != null) {
                source.removeObject(finishPointMapObject)
            }

            finishPointMapObject = createMapPoint(point)
            source.addObject(finishPointMapObject)
        }
    }

    private fun createMapPoint(point: GeoPoint): GeometryMapObject = MarkerBuilder()
        .setIconFromResource(R.drawable.ic_red_nav_pin)
        .setPosition(point)
        .setAnchor(0.5f, 0.95f)
        .build()

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
