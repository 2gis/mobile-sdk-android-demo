## Начало работы

Для работы с SDK нужно создать специальный объект [Context](/ru/android/native/maps/reference/ru.dgis.sdk.context.Context), хранящий сущности, связанные с SDK. Контекст создаётся вызовом метода [DGis.initialize](/ru/android/native/maps/reference/ru.dgis.sdk.DGis#nav-lvl1--initialize).

При вызове `initialize` нужно передать несколько параметров:
 * контекст Android приложения
 * набор ключей доступа к SDK ([APIKeys](/ru/android/native/maps/reference/ru.dgis.sdk.context.ApiKeys))
 * согласие на сбор и обработку данных (опционально, по умолчанию `DataCollectStatus.AGREE`)

```kotlin
val sdkContext = DGis.initialize(
    applicationContext,
    APIKeys(
        directory = "Directory API Key",
        map = "Map API key"
    ),
    DataCollectStatus.AGREE
)
```

Дополнительно можно указать настройки журналирования ([LogOptions](/ru/android/native/maps/reference/ru.dgis.sdk.context.LogOptions)) и настройки HTTP-клиента ([HttpOptions](/ru/android/native/maps/reference/ru.dgis.sdk.context.HttpOptions)), такие как SSL сертификаты и кеширование.

```kotlin
// Настройки журналирования
val logOptions = LogOptions(LogLevel.INFO)

// Настройки HTTP-клиента
val httpOptions = HttpOptions(disableSystemSslCerts = false, useCache = false)

val sdkContext = DGis.initialize(
    applicationContext,
    APIKeys(
        directory = "Directory API Key",
        map = "Map API key"
    ),
    DataCollectStatus.AGREE,
    httpOptions,
    logOptions
)
```

## Создание карты

Чтобы создать карту, нужно добавить [MapView](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapView) в представление activity

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

Объект карты можно получить с помощью метода [getMapAsync](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapView#nav-lvl1--getMapAsync)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val sdkContext = DGis.initialize(
        applicationContext,
        APIKeys(
            directory = "Directory API Key",
            map = "Map API key"
        ),
        DataCollectStatus.AGREE
    )

    setContentView(R.layout.activity_main)

    val mapView = findViewById<MapView>(R.id.mapView)
    lifecycle.addObserver(mapView)

    mapView.getMapAsync { map ->
        // do smth
    }
}
```

## Общие принципы работы

### Работа с отложенными результатами

Некоторые методы SDK (например те, которые обращаются к удаленному серверу) возвращают отложенные результаты (интерфейс [Future](/ru/android/native/maps/reference/ru.dgis.sdk.Future)). Для работы с ними нужно создать обработчик получения данных и обработчик ошибок.

Пример получения объекта из справочника:

```kotlin
// Создание объекта для поиска по справочнику
val searchManager = SearchManager.createOnlineManager(sdkContext)

// Получение объекта из справочника по идентификатору
val future = searchManager.searchByDirectoryObjectId(objectId)

// Обработка результата
future.onResult { directoryObject ->
    // do smth
}

// Обработка ошибки
future.onError { error ->
    // do smth
}
```

По умолчанию обработчики срабатывают на UI потоке, но это можно изменить, если указать executor при вызове `onResult` и `onError`.

### Работа с потоками значений

Некоторые объекты SDK предоставляют потоки значений, которые можно обработать, используя механизм каналов: на поток можно подписаться, указав функцию-обработчик данных, и отписаться, когда обработка данных больше не требуется. Для работы с потоками значений используется интерфейс [Channel](/ru/android/native/maps/reference/ru.dgis.sdk.Channel).

Пример подписки на изменение видимой области карты (поток новых прямоугольных областей):

```kotlin
// Выбираем канал (прямоугольники видимой области карты)
val visibleRectChannel = map.camera.visibleRectChannel

// Подписываемся и обрабатываем результаты в главной очереди. Значения будут присылаться при любом изменении видимой области до момента отписки.
// Важно сохранить соединение с каналом, иначе подписка будет уничтожена.
val connection = visibleRectChannel.connect { geoRect ->
    // do smth
}
```

Чтобы отменить подписку, нужно вызвать метод `close()` у соединения с каналом:

```kotlin
connection.close()
```

## Добавление объектов

Для добавления динамических объектов на карту (маркеров, линий, кругов, многоугольников) нужно создать менеджер объектов ([MapObjectManager](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager)), передав в его конструктор объект карты. При удалении менеджера объектов исчезают все связанные с ним объекты карты, поэтому менеджер объектов нужно сохранить в активити.

```kotlin
mapObjectManager = MapObjectManager(map)
```

Для каждого динамического объекта можно указать поле `userData`, которое будет хранить произвольные данные, связанные с объектом.

Настройки объектов можно менять после их создания.

Для добавления объектов на карту используется метод [addObject](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObject). Можно также добавить несколько объектов одновременно с помощью метода [addObjects](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObjects). Удалить объект с карты можно вызовом метода [removeObject](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObject), а несколько объектов - вызовом метода [removeObjects](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObjects). Удалить все связанные с менеджером объекты можно с помощью метода [removeAll](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeAll). Одновременно добавить и удалить несколько объектов можно с помощью метода [removeAndAddObjects](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeAndAddObjects).

### Маркер

Маркер - это динамический объект, представляющий собой иконку с опциональной подписью. Параметры маркера можно передать в конструктор класса [Marker](/ru/android/native/maps/reference/ru.dgis.sdk.map.Marker) в виде объекта [MarkerOptions](/ru/android/native/maps/reference/ru.dgis.sdk.map.MarkerOptions).

Иконка для маркера представляется объектом класса [Image](/ru/android/native/maps/reference/ru.dgis.sdk.map.Image), который можно получить с помощью одного из вспомогательных методов:
 * [imageFromAsset](/ru/android/native/maps/reference/ru.dgis.sdk.map.imageFromAsset)
 * [imageFromCanvas](/ru/android/native/maps/reference/ru.dgis.sdk.map.imageFromCanvas)
 * [imageFromResource](/ru/android/native/maps/reference/ru.dgis.sdk.map.imageFromResource)
 * imageFromBitmap
 * imageFromSvg

```kotlin
val icon = imageFromResource(sdkContext, R.drawable.ic_marker)

val marker = Marker(
    MarkerOptions(
        position = GeoPointWithElevation(
            latitude = 55.752425,
            longitude = 37.613983
        )
    )
)

mapObjectManager.addObject(marker)
```

Чтобы изменить точку привязки иконки (выравнивание иконки относительно координат на карте), нужно указать параметр [anchor](/ru/android/native/maps/reference/ru.dgis.sdk.map.Anchor).

## Полилиния

Полилиния - ломаная линия, состоящая из нескольких прямых линий. Параметры полилинии можно передать в конструктор класса [Polyline](/ru/android/native/maps/reference/ru.dgis.sdk.map.Polyline) в виде объекта [PolylineOptions](/ru/android/native/maps/reference/ru.dgis.sdk.map.PolylineOptions).

Кроме массива координат для точек линии, в настройках можно указать ширину линии, цвет, параметры пунктира и обводки.

```kotlin
// Координаты вершин ломаной линии
val points = listOf(
    GeoPoint(latitude = 55.7513, longitude = 37.6236),
    GeoPoint(latitude = 55.7405, longitude = 37.6235),
    GeoPoint(latitude = 55.7439, longitude = 37.6506)
)

val polyline = Polyline(
    PolylineOptions(
        points = points,
        width = 2.lpx
    )
)

mapObjectManager.addObject(polyline)
```

Метод расширения `.lpx` преобразует целочисленное значение в объект [LogicalPixel](/ru/android/native/maps/reference/ru.dgis.sdk.map.LogicalPixel).

### Многоугольник

Многоугольник - это объект класса [Polygon](/ru/android/native/maps/reference/ru.dgis.sdk.map.Polygon). В его конструктор передаются параметры многоугольника через объект [PolygonOptions](/ru/android/native/maps/reference/ru.dgis.sdk.map.PolygonOptions).

Координаты для многоугольника указываются в виде двумерного массива. Первый вложенный массив должен содержать координаты основных вершин многоугольника. Остальные вложенные массивы не обязательны и могут быть заданы для того, чтобы создать вырез внутри многоугольника (один дополнительный массив - один вырез в виде многоугольника).

Дополнительно можно указать цвет полигона и параметры обводки.

```kotlin
val polygon = Polygon(
    PolygonOptions(
        contours = listOf(
            // Вершины многоугольника
            listOf(
                GeoPoint(latitude = 55.72014932919687, longitude = 37.562599182128906),
                GeoPoint(latitude = 55.72014932919687, longitude = 37.67555236816406),
                GeoPoint(latitude = 55.78004852149085, longitude = 37.67555236816406),
                GeoPoint(latitude = 55.78004852149085, longitude = 37.562599182128906),
                GeoPoint(latitude = 55.72014932919687, longitude = 37.562599182128906)
            ),
            // Координаты для выреза внутри многоугольника
            listOf(
                GeoPoint(latitude = 55.754167897761, longitude = 37.62422561645508),
                GeoPoint(latitude = 55.74450654680055, longitude = 37.61238098144531),
                GeoPoint(latitude = 55.74460317215391, longitude = 37.63435363769531),
                GeoPoint(latitude = 55.754167897761, longitude = 37.62422561645508)
            )
        ),
        borderWidth = 1.lpx
    )
)

mapObjectManager.addObject(polygon)
```

## Управление камерой

Для работы с камерой используется объект [Camera](/ru/android/native/maps/reference/ru.dgis.sdk.map.Camera), доступный через свойство `map.camera`.

### Перелёт

Чтобы запустить анимацию перелёта камеры, нужно вызвать одну из перегрузок метода `move()`, в которую нужно передать:
 * параметры перелёта: позицию камеры, время перелёта и тип анимации камеры.
 * контроллер перелёта ([CameraMoveController](/ru/android/native/maps/reference/ru.dgis.sdk.map.CameraMoveController))

```kotlin
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val cameraPosition = CameraPosition(
        point = GeoPoint(latitude = 55.752425, longitude = 37.613983),
        zoom = Zoom(16.0),
        tilt = Tilt(25.0),
        bearing = Arcdegree(85.0)
    )

    map.camera.move(cameraPosition, 2.seconds, CameraAnimationType.LINEAR).onResult {
        // перелёт закончен
    }
}
```

### Получение состояния камеры

Текущее состояние камеры (находится ли камера в полёте) можно получить, используя свойство `state`. См. [CameraState](/ru/android/native/maps/reference/ru.dgis.sdk.map.CameraState) для списка возможных состояний камеры.

```kotlin
val currentState = camera.state
```

Подписаться на изменения состояния камеры можно, используя `stateChannel`.

```kotlin
// Подписка
val connection = camera.stateChannel.connect { state ->
    // do smth
}

// Отписка
connection.close()
```

### Получение позиции камеры

Текущую позицию камеры можно получить, используя свойство `position` (см. структуру [CameraPosition](/ru/android/native/maps/reference/ru.dgis.sdk.map.CameraPosition)).

```kotlin
val currentPosition = map.camera.position
Log.d("APP", "Координаты: ${currentPosition.point}")
Log.d("APP", "Приближение: ${currentPosition.zoom}")
Log.d("APP", "Наклон: ${currentPosition.tilt}")
Log.d("APP", "Поворот: ${currentPosition.bearing}")
```

Подписаться на изменения позиции камеры (и угла наклона/поворота) можно, используя `positionChannel`.

```kotlin
// Подписка
val connection = camera.positionChannel.connect { position ->
    // do smth
}

// Отписка
connection.close()
```

### Моё местоположение

На карту можно добавить специальный маркер, который будет отражать текущее местоположение устройства. Для этого нужно создать источник данных - объект [MyLocationMapObjectSource](/ru/android/native/maps/reference/ru.dgis.sdk.map.MyLocationMapObjectSource). Созданный источник нужно передать в метод карты [addSource()](/ru/android/native/maps/reference/ru.dgis.sdk.map.Map#nav-lvl1--addSource).

```kotlin
val source = MyLocationMapObjectSource(
    sdkContext,
    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING
)

map.addSource(source)
```

Чтобы удалить маркер, нужно вызвать метод [removeSource()](/ru/android/native/maps/reference/ru.dgis.sdk.map.Map#nav-lvl1--removeSource). Список активных источников данных можно получить, используя свойство `map.sources`.

```kotlin
map.removeSource(source)
```

### Получение объектов по экранным координатам

Информацию об объектах на карте можно получить, используя пиксельные координаты. Для этого нужно вызвать метод карты [getRenderedObjects()](/ru/android/native/maps/reference/ru.dgis.sdk.map.Map#nav-lvl1--getRenderedObjects), указав координаты в пикселях и радиус в экранных миллиметрах. Метод вернет отложенный результат, содержащий информацию обо всех найденных объектах в указанном радиусе на видимой области карты (массив [RenderedObjectInfo](/ru/android/native/maps/reference/ru.dgis.sdk.map.RenderedObjectInfo)).

Метод `getRenderedObjects` также можно вызвать у [MapView](/ru/android/native/maps/reference/ru.dgis.sdk.map.MapView#nav-lvl1--getRenderedObjects), чтобы не получать отдельно карту только для этого вызова.

Пример функции, которая принимает координаты нажатия на экран и передает их в метод `getRenderedObjects()`:

```kotlin
override fun onTap(point: ScreenPoint) {
    mapView.getRenderedObjects(point, ScreenDistance(5f)).onResult { renderedObjectInfos ->
        // Первый объект в массиве - самый близкий к координатам
        for (renderedObjectInfo in renderedObjectInfos) {
            // do smth
        }
    }
}
```

### Добавление объектов из GeoJson

Чтобы добавить объекты из GeoJson на карту, используйте вспомогательные функции:
 * `parseGeoJson` - возвращает набор геометрических объектов из строки GeoJson
 * `parseGeoJsonFile` - возвращает набор геометрических объектов из файла GeoJson

```kotlin
val source = GeometryMapObjectSourceBuilder(sdkContext).createSource()
map.addSource(source)

parseGeojson("yourGeoJsonString")
    .forEach(source::addObject)
// or
parseGeojsonFile("your/path/to/GeoJson.json")
    .forEach(source::addObject)
```

## Работа со справочником

Для поиска объекта в справочнике нужно создать объект [SearchManager](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager). Можно создать следующие типы справочников:
 * онлайновый ([SearchManager.createOnlineManager](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOnlineManager))
 * оффлайновый, т.е. работающий с предзагруженными данными ([SearchManager.createOfflineManager](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOfflineManager))
 * комбинированный, т.е. работающий онлайн при наличии сети или с предзагруженными данными в противном случае ([SearchManager.createSmartManager](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createSmartManager))

У `SearchManager` можно вызывать методы:
 * [search](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--search) для поиска объектов, соответствующих запросу
 * [searchById](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--searchById) для поиска объекта по известному идентификатору
 * [suggest](/ru/android/native/maps/reference/ru.dgis.sdk.directory.SearchManager#nav-lvl1--suggest) для получения подсказок, соответствующих запросу

Пример поиска объекта:

```kotlin
val searchManager = SearchManager.createSmartManager(sdkContext)
val query = SearchQueryBuilder.fromQueryText("пицца").setPageSize(1).build()

searchManager.search(query).onResult { searchResult ->
    val directoryItem = searchResult.firstPage?.items?.getOrNull(0) ?: return
    // do smth with directoryItem
}
```

## Работа с маршрутами

SDK позволяет искать маршруты между произвольными точками на карте, отображать их на карте и осуществлять ведение по маршруту в режиме навигации.

Поиск маршрутов осуществляется с помощью класса [TrafficRouter](/ru/android/native/maps/reference/ru.dgis.sdk.routing.TrafficRouter), в конструктор которого нужно передать контекст SDK.
Для поиска маршрута нужно вызвать метод `findRoute` со следующими параметрами:
 * начальная точка маршрута
 * конечная точка маршрута
 * опционально - параметры маршрута
 * опционально - список промежуточных точек на маршруте

Для отображения маршрута на карте нужно создать источник объектов маршрута на карте - [RouteMapObjectSource](/ru/android/native/maps/reference/ru.dgis.sdk.map.RouteMapObjectSource).
В этот источник для каждого найденного маршрута будут добавляться объекты [RouteMapObject](/ru/android/native/maps/reference/ru.dgis.sdk.map.RouteMapObject).

```kotlin
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val routeMapObjectSource = RouteMapObjectSource(sdkContext, RouteVisualizationType.NORMAL)
    map.addSource(routeMapObjectSource)

    val startSearchPoint = RouteSearchPoint(
        coordinates = GeoPoint(latitude = 55.759909, longitude = 37.618806)
    )

    val finishSearchPoint = RouteSearchPoint(
        coordinates = GeoPoint(latitude = 55.752425, longitude = 37.613983)
    )

    val trafficRouter = TrafficRouter(sdkContext)
    val routesFuture = trafficRouter.findRoute(startSearchPoint, finishSearchPoint)
    routesFuture.onResult { routes: List<TrafficRoute> ->
        var isActive = true
        var routeIndex = 0
        for (route in routes) {
            routeMapObjectSource.addObject(
                RouteMapObject(route, isActive, routeIndex)
            )
            isActive = false
            routeIndex++
        }
    }
}
```

Альтернативой использованию `TrafficRouter` и `RouteMapObjectSource` является использование [RouteEditor](/ru/android/native/maps/reference/ru.dgis.sdk.routing.RouteEditor) и [RouteEditorSource](/ru/android/native/maps/reference/ru.dgis.sdk.map.RouteEditorSource).
Редактор маршрута даёт меньше контроля над отображением маршрута на карте, но является более простым в использовании: вместо ручного поиска маршрутов и размещения соответствующих объектов на карте можно передать параметры поиска в редактор маршрута, а источник редактора маршрута сделает всё остальное автоматически.

```kotlin
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val routeEditor = RouteEditor(sdkContext)
    val routeEditorSource = RouteEditorSource(sdkContext, routeEditor)
    map.addSource(routeEditorSource)

    routeEditor.setRouteParams(
        RouteParams(
            startPoint = RouteSearchPoint(
                coordinates = GeoPoint(latitude = 55.759909, longitude = 37.618806)
            ),
            finishPoint = RouteSearchPoint(
                coordinates = GeoPoint(latitude = 55.752425, longitude = 37.613983)
            )
        )
    )
}
```

## Ведение по маршруту

Ведение по маршруту осуществляется с помощью класса [NavigationManager](/ru/android/native/maps/reference/ru.dgis.sdk.navigation.NavigationManager). Для запуска ведения по маршруту нужно вызвать его метод `start`, в который нужно передать
 * конечную точку маршрута
 * параметры маршрута
 * опционально - готовый маршрут. Если готовый маршрут не передан, `NavigationManager` построит маршрут самостоятельно.

При ведении по маршруту есть возможность подписаться на события: информация об оставшемся расстоянии до конечной точки маршрута, об оставшемся времени, информация о камерах, полосах движения и т.д. Подписка реализуется посредством соединения с каналами UI модели ([Model](/ru/android/native/maps/reference/ru.dgis.sdk.navigation.Model)). Важно: для избежания утечек подписки нужно сохранять и закрывать при выходе из режима ведения по маршруту: `connections.forEach(AutoCloseable::close)`.

```kotlin
val connections = mutableListOf<AutoCloseable>()

connections.add(navigator.uiModel.roadNameChannel.connect(this::onRoadNameChanged))

val trafficRouter = TrafficRouter(sdkContext)
val routeFuture = trafficRouter.findRoute(fromPoint, toPoint, routeOptions)

routeFuture.onResult { routes ->
    val route = routes.getOrNull(0) ?: return
    navigator.start(toPoint, options, route)
}
```

## Создание и использование собственного источника позиции

Для создания собственного источника геопозиции и передачи позиции в SDK необходимо реализовать интерфейс [LocationSource](/ru/android/native/maps/reference/LocationChangeListener).

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

Созданный источник нужно зарегистрировать в SDK посредством вызова функции `registerPlatformLocationSource(DGis.context(), customSource)`

Когда SDK потребуется позиция, будет вызван метод `activate`, с объектом [`LocationChangeListener`](/ru/android/native/maps/reference/LocationChangeListener), в который необходимо передавать изменения позиции посредством вызова метода `onLocationChanged`. Также интерфейс предоставляет возможность передавать доступность данного источника позиции через метод `onAvailabilityChanged`.

Вызов метода `LocationSource.deactivate` означает, что SDK позиция более не нужна.

`LocationSource.setDesiredAccuracy` - устанавливает необходимую точность геопозиции для текущей сессии.

