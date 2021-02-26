# Примеры

## Создание виджета карты

Чтобы отобразить карту:
1. Добавьте следующий [`MapView`](/en/android/native/maps/reference/MapView#nav-lvl1--MapView) в layout-файл для нужного Activity:

   ```xml
   <ru.dgis.sdk.map.MapView
       android:id="@+id/mapView"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       app:dgis_cameraTargetLat="55.740444"
       app:dgis_cameraTargetLng="37.619524"
       app:dgis_cameraZoom="16.0"
       />
   ```

2. Инициализируйте виджет, вызвав метод [`getMapAsync`](/en/android/native/maps/reference/MapView#nav-lvl2--getMapAsync):

   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       super.onCreate(savedInstanceState)

       val sdkContext = DGis.initialize(applicationContext)
       setContentView(R.layout.activity_main)

       val mapView = findViewById<MapView>(R.id.mapView)
       lifecycle.addObserver(mapView)

       mapView.getMapAsync { map ->
           // do smth
       }
   }
   ```

## Использование тайлов карты из онлайн-источника

Текущая версия SDK по умолчанию использует предустановленные данные для отображения тайлов карты (map tiles). Также доступна экспериментальная функция, позволяющая загружать тайлы из онлайн-источника:

```kotlin
val mapOptions = MapOptions().apply {
    source = DgisSourceCreator.createOnlineDgisSource(sdkContext)
}
val mapView = MapView(this, mapOptions).also { 
    lifecycle.addObserver(it)
}
mapContainer.addView(mapView)
```

## Перемещение по карте

Вы можете изменять позицию карты и перемещаться по карте  с помощью [`Camera`](/en/android/native/maps/reference/Camera):

```kotlin
val sdkContext = DGis.initialize(applicationContext)
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val cameraPosition = CameraPosition(
        point = GeoPoint(Arcdegree(55.752425), Arcdegree(37.613983)),
        zoom = Zoom(16.0),
        tilt = Tilt(25.0),
        bearing = Arcdegree(85.0))

    map
        ?.camera
        ?.move(cameraPosition, Duration.ofSeconds(2), CameraAnimationType.LINEAR)
        ?.onResult {
            // перелет закончен
        }
}
```

## Добавление динамических объектов

Чтобы добавить свой объект на карту, передайте объект класса [GeometryMapObject](/en/android/native/maps/reference/GeometryMapObject) в метод `addObject()` объекта класса [GeometryMapObjectSource](/en/android/native/maps/reference/GeometryMapObjectSource)
```kotlin
val source = GeometryMapObjectSourceBuilder(sdkContext)
    .createSource()!!
map.addSource(source)

val polylineObject = GeometryMapObjectBuilder()
    .setGeometry(createPolygonSampleGeometry())
    .setObjectAttribute("color", fillColor)
    .createObject()

source.addObject(polylineObject)
```

## Добавление объектов из GeoJson

Чтобы добавить объекты из GeoJson на карту, используйте [GeometryMapObjectCreator](/en/android/native/maps/reference/GeometryMapObjectCreator):

```kotlin
val source = GeometryMapObjectSourceBuilder(sdkContext)
    .createSource()!!
map.addSource(source)

GeometryMapObjectCreator.parseGeojson("yourGeoJsonString")
    .filterNotNull()
    .forEach(source::addObject)
// or
GeometryMapObjectCreator.parseGeojsonFile("your/path/to/GeoJson.json")
    .filterNotNull()
    .forEach(source::addObject)
```

## Работа со справочником

Чтобы выполнить поисковый запрос к справочнику, используйте [SearchManager](ru/android/native/maps/reference/SearchManager):

```kotlin
val sdkContext = DGis.initialize(applicationContext)
val searchManager = SearchManager.createSmartManager(sdkContext)!!
val query = SearchQueryBuilder.fromQueryText("пицца")!!.setPageSize(1).build()

searchManager.search(query).onResult { searchResult ->
    searchResult?.firstPage()?.items()?.getOrNull(0)?.let { directoryItem ->
        // использовать объект поиска
    }
}
```

## Построение маршрута и его отображение на карте

```kotlin
val sdkContext = DGis.initialize(applicationContext)
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val routeMapObjectSource = createRouteMapObjectSource(sdkContext)
    map.viewport.addSource(routeMapObjectSource)

    val startSearchPoint = RouteSearchPoint(
        coordinates = GeoPoint(Arcdegree(55.759909), Arcdegree(37.618806)),
        course = null,
        objectId = DirectoryObjectId(0)
    )

    val finishSearchPoint = RouteSearchPoint(
        coordinates = GeoPoint(Arcdegree(55.752425), Arcdegree(37.613983)),
        course = null,
        objectId = DirectoryObjectId(1)
    )

    val routeOptions = RouteOptions(
        avoidTollRoads = false,
        avoidUnpavedRoads = false,
        avoidFerry = false
    )

    val trafficRouter = TrafficRouter(sdkContext)
    val routesFuture = trafficRouter.findRoute(startSearchPoint, finishSearchPoint, routeOptions)
    routesFuture.onResult { routes: List<TrafficRoute?> ->
        runOnUiThread {
            var isActive = true
            for (route in routes) {
                route ?: continue
                routeMapObjectSource?.addObject(
                    createRouteMapObject(
                        route,
                        isActive
                    )
                )
                isActive = false
            }
        }
    }
}
```

## Создание и использование собственного источника геопозиции

Вы можете создать собственный источник геопозиции и передавать геопозицию в SDK. Для этого:
1. Реализуйте интерфейс [`LocationSource`](/en/android/native/maps/reference/LocationSource), чтобы создать источник.

   ```kotlin
   public class CustomLocationSource: LocationSource {
       override fun activate(listener: LocationChangeListener?) {
       }

       override fun deactivate() {
       }

       override fun setDesiredAccuracy(accuracy: DesiredAccuracy?) {
       }
   }
   ```
2. Зарегистрируйте этот источник в SDK, вызвав метод `registerPlatformLocationSource(DGis.context(), customSource)`

Когда SDK потребуется позиция, будет вызван метод `activate`, с объектом [`LocationChangeListener`](/en/android/native/maps/reference/LocationChangeListener), в который необходимо передавать изменения позиции посредством вызова метода `onLocationChanged`. Также интерфейс предоставляет возможность передавать доступность данного источника позиции через метод `onAvailabilityChanged`.

Вызов метода `LocationSource.deactivate` означает, что позиция более не нужна SDK.

Метод `LocationSource.setDesiredAccuracy` устанавливает необходимую точность геопозиции для текущей сессии.


## Отображение маркера текущего местоположения

Вы можете отобразить на карте маркер текущего местоположения. Для этого нужно добавить объект-источник текущего местоположения для карты:

```kotlin
mapView.getMapAsync { map ->
    createMyLocationMapObjectSource(sdkContext)?.let { locationSource ->
		map.addSource(locationSource)
	}
}
```

## Добавление маркера на карту

```kotlin
GeometryMapObjectSourceBuilder(sdkContext).createSource()?.let { source ->
    map.addSource(source)
    
    val marker = MarkerBuilder()
        .setIconFromResource(resourceId)
        .setPosition(latitude, longitude)
        .build()
    
    source.addObject(marker)
}
```

## Подписка на события во время ведения по маршруту 

Вы можете подписаться на события, которые возникают при ведении по маршруту. В примере ниже происходит подписка на событие обновления имени текущей улицы:

```kotlin
val connections = mutableListOf<Connection>()

navigator.uiModel()?.let { model ->
    connections.add(model.roadNameChannel().connect(this::roadNameChanged))
}

val trafficRouter = TrafficRouter(sdkContext)
val routeFuture = trafficRouter.findRoute(fromPoint, pt, options)

routeFuture.onResult {  routes ->
    routes.getOrNull(0)?.let { route ->
        navigator.startSimulation(pt, options, route)
    }
}
```

Аналогичным образом можно получить информацию о дистанции, оставшемся времени, камере, полосности и т.д.

Важно: для избежания утечек следует сохранить все подписки и при выходе их отключить: `connections.forEach(Connection::disconnect)`.

## Обработка событий нажатия

При нажатии на карту можно определить, какой объект был выбран — динамический объект или объект 2ГИС. Ниже приведен пример получения информации из справочника:

```kotlin
override fun onTap(point: ScreenPoint) {
    viewport.getRenderedObjects(point, ScreenDistance(5f))
        .onResult { renderedObjects ->
            renderedObjects.mapNotNull { objectInfo ->
                tryCastToDgisMapObject(objectInfo.item.item)
            }.forEach { dgisObject ->
                dgisObject.directoryObject().onResult { directoryObject ->
                    Toast
                        .makeText(this, "${directoryObject?.title()}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
}
```
