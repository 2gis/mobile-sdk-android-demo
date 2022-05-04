## Начало работы

Для работы с SDK нужно вызвать метод `initialize()` объекта [DGis](/ru/android/sdk/reference/2.0/ru.dgis.sdk.DGis), указав контекст приложения и набор ключей доступа (объект [ApiKeys](/ru/android/sdk/reference/2.0/ru.dgis.sdk.ApiKeys)).

В SDK используется два ключа: `map` (основной ключ SDK) и `directory` (ключ доступа к дополнительным API: справочнику объектов и маршрутизатору).

```kotlin
class Application : Application() {
    lateinit var sdkContext: Context

    override fun onCreate() {
        super.onCreate()

        sdkContext = DGis.initialize(
            this, ApiKeys(
                directory = "Directory API key",
                map = "SDK key"
            )
        )
    }
}
```
Обратите внимание, что нельзя создавать более одного экземпляра Context.


Дополнительно можно указать настройки журналирования ([LogOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.LogOptions)) и настройки HTTP-клиента ([HttpOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.HttpOptions)), такие как кеширование.

```kotlin
// Ключи доступа
val apiKeys = ApiKeys(
    directory = "Directory API key",
    map = "SDK key"
)

// Настройки журналирования
val logOptions = LogOptions(
    LogLevel.VERBOSE
)

// Настройки HTTP-клиента
val httpOptions = HttpOptions(
    useCache = false
)

// Согласие на сбор и отправку персональных данных
val dataCollectConsent = PersonalDataCollectionConsent.GRANTED

sdkContext = DGis.initialize(
    appContext = this,
    apiKeys = apiKeys,
    dataCollectConsent = dataCollectConsent,
    logOptions = logOptions,
    httpOptions = httpOptions
)
```

## Начало работы с версии 4.x

Сначала нужно обратиться в техническую поддержку 2ГИС для получения нового ключа. Обязательно нужно указать `appId` приложения, для которого будет создан ключ. Полученный файл ключа `dgissdk.key` нужно добавить `assets`.

Для работы с SDK нужно вызвать метод `initialize()` объекта [DGis](/ru/android/sdk/reference/2.0/ru.dgis.sdk.DGis), указав контекст приложения.

```kotlin
class Application : Application() {
    lateinit var sdkContext: Context

    override fun onCreate() {
        super.onCreate()

        sdkContext = DGis.initialize(
            this
        )
    }
}
```
Обратите внимание, что нельзя создавать более одного экземпляра Context.


Дополнительно можно указать настройки журналирования ([LogOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.LogOptions)) и настройки HTTP-клиента ([HttpOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.HttpOptions)), такие как кеширование.

```kotlin
// Настройки журналирования
val logOptions = LogOptions(
    LogLevel.VERBOSE
)

// Настройки HTTP-клиента
val httpOptions = HttpOptions(
    useCache = false
)

// Согласие на сбор и отправку персональных данных
val dataCollectConsent = PersonalDataCollectionConsent.GRANTED

sdkContext = DGis.initialize(
    appContext = this,
    dataCollectConsent = dataCollectConsent,
    logOptions = logOptions,
    httpOptions = httpOptions
)
```

## Создание карты

Чтобы создать карту, добавьте [MapView](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView) в ваш activity:

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

Для карты можно указать начальные координаты (`cameraTargetLat` - широта; `cameraTargetLng` - долгота) и масштаб (`cameraZoom`).

[MapView](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView) также можно создать программно. В таком случае настройки можно указать в виде объекта [MapOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapOptions).

Объект карты ([Map](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Map)) можно получить, вызвав метод `getMapAsync()`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val sdkContext = DGis.initialize(applicationContext, apiKeys)
    setContentView(R.layout.activity_main)

    val mapView = findViewById<MapView>(R.id.mapView)
    lifecycle.addObserver(mapView)

    mapView.getMapAsync { map ->
        // Действия с картой
        val camera = map.camera
    }
}
```

## Общие принципы работы

### Отложенные результаты

Некоторые методы SDK (например те, которые обращаются к удалённому серверу) возвращают отложенные результаты ([Future](/ru/android/sdk/reference/2.0/ru.dgis.sdk.Future)). Для работы с ними нужно создать обработчик получения данных и обработчик ошибок.

Пример получения объекта из справочника:

```kotlin
// Создание объекта для поиска по справочнику
val searchManager = SearchManager.createOnlineManager(sdkContext)

// Получение объекта из справочника по идентификатору
val future = searchManager.searchByDirectoryObjectId(objectId)

// Обработка результата
future.onResult { directoryObject ->
    Log.d("APP", "Название объекта: ${directoryObject.title}")
}

// Обработка ошибки
future.onError { error ->
    Log.d("APP", "Ошибка получения информации об объекте.")
}
```

По умолчанию обработка результатов происходит в UI-потоке. Чтобы это изменить, для `onResult` и `onError` можно указать [Executor](https://developer.android.com/reference/java/util/concurrent/Executor.html).

Подробнее про работу со справочником можно посмотреть в разделе [Справочник объектов](#nav-lvl1--Справочник_объектов).

### Потоки значений

Некоторые объекты SDK предоставляют потоки значений, которые можно обработать, используя механизм каналов: на поток можно подписаться, указав функцию-обработчик данных, и отписаться, когда обработка данных больше не требуется. Для работы с потоками значений используется интерфейс [Channel](/ru/android/sdk/reference/2.0/ru.dgis.sdk.Channel).

Пример подписки на изменение видимой области карты (поток новых прямоугольных областей):

```kotlin
// Выбираем канал (прямоугольники видимой области карты)
val visibleRectChannel = map.camera.visibleRectChannel

// Подписываемся и обрабатываем результаты в главной очереди. Значения будут присылаться при любом изменении видимой области до момента отписки.
// Важно сохранить соединение с каналом, иначе подписка будет уничтожена.
val connection = visibleRectChannel.connect { geoRect ->
    Log.d("APP", "${geoRect.southWestPoint.latitude.value}")
}
```

После окончания работы с каналом важно отменить подписку, чтобы избежать утечки памяти. Для этого нужно вызвать метод `close()`:

```kotlin
connection.close()
```

### Источники данных для карты

В некоторых случаях для добавления объектов на карту нужно создать специальный объект - источник данных. Источники данных выступают в роли менеджеров объектов: вместо добавления объектов на карту напрямую, на карту добавляется источник данных и вся последующая работа с объектами происходит через него.

Источники данных бывают разных типов: движущиеся маркеры, маршруты с отображением текущей загруженности дорог, произвольные геометрические фигуры и т.д. Для каждого типа данных существует свой класс.

В общем случае работа с источниками данных выглядит следующим образом:

```kotlin
// Создание источника данных
val source = MyMapObjectSource(
    sdkContext,
    ...
)

// Добавление источника данных на карту
map.addSource(source)

// Добавление и удаление объектов в источнике данных
source.addObject(...)
source.removeObject(...)
```

Чтобы удалить созданный источник данных и все связанные с ним объекты, нужно вызвать метод карты `removeSource()`:

```kotlin
map.removeSource(source)
```

Список активных источников данных можно получить, используя свойство `map.sources`.

## Добавление объектов

Для добавления динамических объектов на карту (маркеров, линий, кругов, многоугольников) нужно создать менеджер объектов ([MapObjectManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager)), указав объект карты. При удалении менеджера объектов удаляются все связанные с ним объекты на карте, поэтому его нужно сохранить в activity.

```kotlin
mapObjectManager = MapObjectManager(map)
```

Для добавления объектов используются методы [addObject()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObject) и [addObjects()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObjects). Для каждого динамического объекта можно указать поле `userData`, которое будет хранить произвольные данные, связанные с объектом. Настройки объектов можно менять после их создания.

Для удаления объектов используются методы [removeObject()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObject) и [removeObjects()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObjects). Чтобы удалить все объекты, можно использовать метод [removeAll()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeAll).

### Маркер

Чтобы добавить маркер на карту, нужно создать объект [Marker](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Marker), указав нужные настройки, и передать его в вызов `addObject()` менеджера объектов.

В настройках нужно указать координаты маркера (параметр `position`).

```kotlin
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

Чтобы изменить иконку маркера, нужно указать объект [Image](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Image) в качестве параметра `icon`. Создать [Image](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Image) можно с помощью следующих функций:

- [imageFromAsset()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromAsset(context%2CassetName%2Csize))
- [imageFromBitmap()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromBitmap(context%2Cbitmap))
- [imageFromCanvas()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromCanvas(context%2Csize%2Cblock))
- [imageFromResource()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromResource(context%2CresourceId%2Csize))
- [imageFromSvg()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromSvg(context%2Cdata))

```kotlin
val icon = imageFromResource(sdkContext, R.drawable.ic_marker)

val marker = Marker(
    MarkerOptions(
        position = GeoPointWithElevation(
            latitude = 55.752425,
            longitude = 37.613983
        ),
        icon = icon
    )
)
```

Чтобы изменить точку привязки иконки (выравнивание иконки относительно координат на карте), нужно указать параметр [anchor](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Anchor).

Дополнительно можно указать текст для маркера и другие настройки (см. [MarkerOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MarkerOptions)).

### Линия

Чтобы нарисовать на карте линию, нужно создать объект [Polyline](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Polyline), указав нужные настройки, и передать его в вызов `addObject()` менеджера объектов.

Кроме списка координат для точек линии, в настройках можно указать ширину линии, цвет, пунктир, обводку и другие параметры (см. [PolylineOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.PolylineOptions)).

```kotlin
// Координаты вершин ломаной линии
val points = listOf(
    GeoPoint(latitude = 55.7513, longitude = 37.6236),
    GeoPoint(latitude = 55.7405, longitude = 37.6235),
    GeoPoint(latitude = 55.7439, longitude = 37.6506)
)

// Создание линии
val polyline = Polyline(
    PolylineOptions(
        points = points,
        width = 2.lpx
    )
)

// Добавление линии на карту
mapObjectManager.addObject(polyline)
```

Свойство-расширение `.lpx` преобразует целое число в объект [LogicalPixel](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.LogicalPixel).

### Многоугольник

Чтобы нарисовать на карте многоугольник, нужно создать объект [Polygon](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Polygon), указав нужные настройки, и передать его в вызов `addObject()` менеджера объектов.

Координаты для многоугольника указываются в виде двумерного списка. Первый вложенный список должен содержать координаты основных вершин многоугольника. Остальные вложенные списки не обязательны и могут быть заданы для того, чтобы создать вырез внутри многоугольника (один дополнительный список - один вырез в виде многоугольника).

Дополнительно можно указать цвет полигона и параметры обводки (см. [PolygonOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.PolygonOptions)).

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

### Кластеризация

Для добавления маркеров на карту в режиме кластеризации нужно создать менеджер объектов ([MapObjectManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager)) через [MapObjectManager.withClustering()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--withClustering), указав инстанс карты, расстояние между кластерами в логических пикселях, максимальный zoom-уровень формирования кластеров и пользовательскую имплементацию протокола SimpleClusterRenderer.
[SimpleClusterRenderer](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.SimpleClusterRenderer) используется для кастомизации кластеров в [MapObjectManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager).

```kotlin
val clusterRenderer = object : SimpleClusterRenderer {
    override fun renderCluster(cluster: SimpleClusterObject): SimpleClusterOptions {
        val textStyle = TextStyle(
            fontSize = LogicalPixel(15.0f),
            textPlacement = TextPlacement.RIGHT_TOP
        )
        val objectCount = cluster.objectCount
        val iconMapDirection = if (objectCount < 5) MapDirection(45.0) else null
        return SimpleClusterOptions(
            icon,
            iconWidth = LogicalPixel(30.0f),
            text = objectCount.toString(),
            textStyle = textStyle,
            iconMapDirection = iconMapDirection,
            userData = objectCount.toString()
        )
    }
}

mapObjectManager = MapObjectManager.withClustering(map, LogicalPixel(80.0f), Zoom(18.0f), clusterRenderer)
```

## Управление камерой

Для работы с камерой используется объект [Camera](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Camera), доступный через свойство `map.camera`.

### Перелёт

Чтобы запустить анимацию перелёта камеры, нужно вызвать метод [move()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Camera#nav-lvl1--move) и указать параметры перелёта:

- `position` - конечная позиция камеры (координаты и уровень приближения). Дополнительно можно указать наклон и поворот камеры (см. [CameraPosition](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraPosition)).
- `time` - продолжительность перелёта в секундах ([Duration](/ru/android/sdk/reference/2.0/ru.dgis.sdk.Duration)).
- `animationType` - тип анимации ([CameraAnimationType](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraAnimationType)).

Функция `move()` возвращает объект [Future](/ru/android/sdk/reference/2.0/ru.dgis.sdk.Future), который можно использовать, чтобы обработать событие завершения перелёта.

```kotlin
val mapView = findViewById<MapView>(R.id.mapView)

mapView.getMapAsync { map ->
    val cameraPosition = CameraPosition(
        point = GeoPoint(latitude = 55.752425, longitude = 37.613983),
        zoom = Zoom(16.0),
        tilt = Tilt(25.0),
        bearing = Arcdegree(85.0)
    )

    map.camera.move(cameraPosition, Duration.ofSeconds(2), CameraAnimationType.LINEAR).onResult {
        Log.d("APP", "Перелёт камеры завершён.")
    }
}
```

Для указания продолжительности перелёта можно использовать расширение `.seconds`:

```kotlin
map.camera.move(cameraPosition, 2.seconds, CameraAnimationType.LINEAR)
```

Для более точного контроля над анимацией перелёта можно использовать контроллер перелёта, который будет определять позицию камеры в каждый конкретный момент времени. Для этого нужно реализовать интерфейс [CameraMoveController](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraMoveController) и передать созданный объект в метод `move()` вместо параметров перелёта.

### Получение состояния камеры

Текущее состояние камеры (находится ли камера в полёте) можно получить, используя свойство `state`. См. [CameraState](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraState) для списка возможных состояний камеры.

```kotlin
val currentState = map.camera.state
```

Подписаться на изменения состояния камеры можно с помощью свойства `stateChannel`.

```kotlin
// Подписка
val connection = map.camera.stateChannel.connect { state ->
    Log.d("APP", "Состояние камеры изменилось на ${state}")
}

// Отписка
connection.close()
```

### Получение позиции камеры

Текущую позицию камеры можно получить, используя свойство `position` (см. объект [CameraPosition](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraPosition)).

```kotlin
val currentPosition = map.camera.position

Log.d("APP", "Координаты: ${currentPosition.point}")
Log.d("APP", "Приближение: ${currentPosition.zoom}")
Log.d("APP", "Наклон: ${currentPosition.tilt}")
Log.d("APP", "Поворот: ${currentPosition.bearing}")
```

Подписаться на изменения позиции камеры (и угла наклона/поворота) можно с помощью свойства `positionChannel`.

```kotlin
// Подписка
val connection = map.camera.positionChannel.connect { position ->
    Log.d("APP", "Изменилась позиция камеры или угол наклона/поворота.")
}

// Отписка
connection.close()
```

## Моё местоположение

На карту можно добавить специальный маркер, который будет отражать текущее местоположение устройства. Для этого нужно добавить на карту источник данных [MyLocationMapObjectSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MyLocationMapObjectSource).

```kotlin
// Создание источника данных
val source = MyLocationMapObjectSource(
    sdkContext,
    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING
)

// Добавление источника данных на карту
map.addSource(source)
```

## Получение объектов по экранным координатам

Информацию об объектах на карте можно получить, используя пиксельные координаты. Для этого нужно вызвать метод карты [getRenderedObjects()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.Map#nav-lvl1--getRenderedObjects), указав координаты в пикселях и радиус в экранных миллиметрах. Метод вернет отложенный результат, содержащий информацию обо всех найденных объектах в указанном радиусе на видимой области карты (список [RenderedObjectInfo](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RenderedObjectInfo)).

Пример функции, которая принимает координаты нажатия на экран и передаёт их в метод `getRenderedObjects()`:

```kotlin
override fun onTap(point: ScreenPoint) {
    map.getRenderedObjects(point, ScreenDistance(5f)).onResult { renderedObjectInfos ->
        // Первый объект в списке - самый близкий к координатам
        for (renderedObjectInfo in renderedObjectInfos) {
            Log.d("APP", "Произвольные данные объекта: ${renderedObjectInfo.item.item.userData}")
        }
    }
}
```

## Справочник объектов

Для поиска объектов в справочнике нужно создать объект [SearchManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager), вызвав один из следующих методов:

- [SearchManager.createOnlineManager()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOnlineManager) - создаёт онлайн-справочник.
- [SearchManager.createOfflineManager()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOfflineManager) - создаёт офлайн-справочник, работающий только с предзагруженными данными.
- [SearchManager.createSmartManager()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createSmartManager) - создаёт комбинированный справочник, работающий с онлайн-данными при наличии сети и с предзагруженными данными при отсутствии сети.

```kotlin
val searchManager = SearchManager.createSmartManager(sdkContext)
```

Если идентификатор (ID) объекта известен, то для получения информации о нём нужно вызвать метод [searchById()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--searchById). Метод вернёт отложенный результат [DirectoryObject](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.DirectoryObject).

```kotlin
searchManager.searchById(id).onResult { directoryObject ->
    Log.d("APP", "Название объекта: ${directoryObject.title}")
}
```

Если ID объекта не известен, то можно создать поисковый запрос (объект [SearchQuery](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchQuery)) с помощью [SearchQueryBuilder](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchQueryBuilder) и передать его в метод [search()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--search). Вызов вернёт отложенный результат [SearchResult](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchResult), содержащий список найденных объектов ([DirectoryObject](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.DirectoryObject)), разделенный на страницы.

```kotlin
val query = SearchQueryBuilder.fromQueryText("пицца").setPageSize(10).build()

searchManager.search(query).onResult { searchResult ->
    // Получаем первый объект с первой страницы
    val directoryObject = searchResult.firstPage?.items?.getOrNull(0) ?: return
    Log.d("APP", "Название объекта: ${directoryObject.title}")
}
```

Чтобы получить следующую страницу результатов поиска, нужно вызвать метод страницы [fetchNextPage()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.Page#nav-lvl1--fetchNextPage), который вернёт отложенный результат [Page](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.Page).

```kotlin
firstPage.fetchNextPage().onResult { nextPage
    val directoryObject = nextPage?.items?.getOrNull(0) ?: return
}
```

Также с помощью справочника можно получать подсказки при текстовом поиске объектов (см. [Suggest API](/ru/api/search/suggest/overview) для демонстрации). Для этого нужно создать объект [SuggestQuery](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestQuery) с помощью [SuggestQueryBuilder](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestQueryBuilder) и передать его в метод [suggest()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--suggest). Вызов вернёт отложенный результат [SuggestResult](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestResult), содержащий список подсказок ([Suggest](/ru/android/sdk/reference/2.0/ru.dgis.sdk.directory.Suggest)).

```kotlin
val query = SuggestQueryBuilder.fromQueryText("пицц").setLimit(10).build()

searchManager.suggest(query).onResult { suggestResult ->
    // Получаем первую подсказку из списка
    val firstSuggest = suggestResult.suggests?.getOrNull(0) ?: return
    Log.d("APP", "Заголовок подсказки: ${firstSuggest.title}")
}
```

## Построение маршрута

Для того, чтобы проложить маршрут на карте, нужно создать два объекта: [TrafficRouter](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter) для поиска оптимального маршрута и источник данных [RouteMapObjectSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource) для отображения маршрута на карте.

Чтобы найти маршрут между двумя точками, нужно вызвать метод [findRoute()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter#nav-lvl1--findRoute), передав координаты точек в виде объектов [RouteSearchPoint](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteSearchPoint). Дополнительно можно указать параметры маршрута ([RouteOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteOptions)), а также список промежуточных точек маршрута (список [RouteSearchPoint](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteSearchPoint)).

```kotlin
val startSearchPoint = RouteSearchPoint(
    coordinates = GeoPoint(latitude = 55.759909, longitude = 37.618806)
)
val finishSearchPoint = RouteSearchPoint(
    coordinates = GeoPoint(latitude = 55.752425, longitude = 37.613983)
)

val trafficRouter = TrafficRouter(sdkContext)
val routesFuture = trafficRouter.findRoute(startSearchPoint, finishSearchPoint)
```

Вызов вернёт отложенный результат со списком объектов [TrafficRoute](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute). Чтобы отобразить найденный маршрут на карте, нужно на основе этих объектов создать объекты [RouteMapObject](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObject) и добавить их в источник данных [RouteMapObjectSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource).

```kotlin
// Создаём источник данных
val routeMapObjectSource = RouteMapObjectSource(sdkContext, RouteVisualizationType.NORMAL)
map.addSource(routeMapObjectSource)

// Ищем маршрут
val routesFuture = trafficRouter.findRoute(startSearchPoint, finishSearchPoint)
val trafficRouter = TrafficRouter(sdkContext)

// После получения маршрута добавляем его на карту
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
```

Вместо пары [TrafficRouter](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter) и [RouteMapObjectSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource) для построения маршрута можно использовать [RouteEditor](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteEditor) и [RouteEditorSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteEditorSource). В таком случае не нужно обрабатывать список [TrafficRoute](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute), достаточно передать координаты маршрута в виде объекта [RouteParams](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteParams) в метод [setRouteParams()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteEditor#nav-lvl1--setRouteParams) и маршрут отобразится автоматически.

```kotlin
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
```

## Навигатор

Чтобы создать навигатор, можно использовать готовые элементы интерфейса и класс [NavigationManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager).

Для этого нужно добавить в [MapView](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView) элементы [NavigationView](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationView) и [DefaultNavigationControls](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.DefaultNavigationControls).

```xml
<ru.dgis.sdk.map.MapView
    android:id="@+id/mapView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    <ru.dgis.sdk.navigation.NavigationView
        android:id="@+id/navigationView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        >
        <ru.dgis.sdk.navigation.DefaultNavigationControls
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </ru.dgis.sdk.navigation.NavigationView>
</ru.dgis.sdk.map.MapView>
```

После этого нужно добавить на карту маркер с текущим местоположением и создать объект [NavigationManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager).

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sdkContext = DGis.initialize(applicationContext, apiKeys)

    // Регистрируем источник геопозиции
    locationProvider = ManagerLocationSource(applicationContext)
    registerPlatformLocationSource(sdkContext, locationProvider)

    setContentView(R.layout.activity_navigation)

    findViewById<MapView>(R.id.mapView).apply { mapView ->
        lifecycle.addObserver(mapView)

        mapView.getMapAsync { map ->
            // Добавляем маркер с текущим местоположением
            map.addSource(
                MyLocationMapObjectSource(
                    sdkContext,
                    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING,
                    createSmoothMyLocationController()
                )
            )
        }
    }
    
    // Создаём объект NavigationManager
    navigationManager = NavigationManager(sdkContext)

    findViewById<NavigationView>(R.id.navigationView).apply {
        // Привязываем созданный объект NavigationManager к элементу интерфейса NavigationView
        navigationManager = this@NavigationActivity.navigationManager
    }
    
    // Запускаем навигатор в режиме свободной навигации
    navigationManager.start()
}
```

Навигатор может работать в трёх режимах: свободная навигация, ведение по маршруту и симуляция ведения.

Настройки навигатора можно изменить через [свойства NavigationManager](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager#nav-lvl1--val%20uiModel).

### Свободная навигация

В этом режиме маршрут следования отсутствует, но навигатор будет информировать о превышениях скорости, дорожных камерах, авариях и ремонтных работах.

Чтобы запустить навигатор в этом режиме, нужно вызвать метод `start()` без параметров.

```kotlin
navigationManager.start()
```

### Ведение по маршруту

В этом режиме на карте будет построен маршрут от текущего местоположения до указанной точки назначения, и пользователь будет получать инструкции по мере движения.

Чтобы запустить навигатор в этом режиме, нужно вызвать метод `start()` и указать объект [RouteBuildOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.RouteBuildOptions) - координаты точки назначения и настройки маршрута.

```kotlin
val routeBuildOptions = RouteBuildOptions(
    finishPoint = RouteSearchPoint(
        coordinates = GeoPoint(latitude = 55.752425, longitude = 37.613983)
    ),
    routeSearchOptions = CarRouteSearchOptions(
        avoidTollRoads = true,
        avoidUnpavedRoads = false,
        avoidFerry = false,
        routeSearchType = RouteSearchType.JAM
    )
)

navigationManager.start(routeBuildOptions)
```

Дополнительно при вызове метода `start()` можно указать объект [TrafficRoute](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute) - готовый маршрут для навигации (см. раздел [Построение маршрута](#nav-lvl1--Построение_маршрута)). В таком случае навигатор не будет пытаться построить маршрут от текущего местоположения, а начнёт ведение по указанному маршруту.

```kotlin
navigationManager.start(routeBuildOptions, trafficRoute)
```

### Симуляция ведения по маршруту

В этом режиме навигатор не будет отслеживать реальное местоположение устройства, а запустит симулированное движение по указанному маршруту. Режим удобно использовать для отладки.

Чтобы запустить навигатор в режиме симуляции, нужно вызвать метод `startSimulation()`, указав готовый маршрут ([TrafficRoute](/ru/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute)) и его настройки ([RouteBuildOptions](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.RouteBuildOptions)).

Скорость движения можно изменить с помощью свойства [SimulationSettings.speed](/ru/android/sdk/reference/2.0/ru.dgis.sdk.navigation.SimulationSettings) (метры в секунду).

```kotlin
navigationManager.simulationSettings.speed = 30 / 3.6
navigationManager.startSimulation(routeBuildOptions, trafficRoute)
```

Остановить симуляцию можно с помощью метода `stop()`.

```kotlin
navigationManager.stop()
```

### Отображение пробок на карте

Чтобы включить показ дорожного трафика, нужно добавить на карту источник данных [TrafficSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.map.TrafficSource).

```kotlin
val trafficSource = TrafficSource(sdkContext)
map.addSource(trafficSource)
```

## Собственный источник геопозиции

В рамках SDK можно использовать произвольный источник геопозиции. Для этого нужно реализовать интерфейс [LocationSource](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationSource).

```kotlin
public class CustomLocationSource: LocationSource {
    override fun activate(listener: LocationChangeListener?) {
        // Включение источника геопозиции
    }

    override fun deactivate() {
        // Выключение источника геопозиции
    }

    override fun setDesiredAccuracy(accuracy: DesiredAccuracy?) {
        // Изменение требуемого уровня точности
    }
}
```

Чтобы зарегистрировать созданный источник в SDK, нужно вызвать функцию [registerPlatformLocationSource()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.registerPlatformLocationSource).

```kotlin
val customSource = CustomLocationSource()
registerPlatformLocationSource(sdkContext, customSource)
```

Основная точка входа в интерфейс - функция `activate()`. Когда SDK потребуется геопозиция, в эту функцию будет передан объект [LocationChangeListener](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener). После этого, чтобы сообщить текущую геопозицию, нужно передать в него массив объектов [Location](https://developer.android.com/reference/kotlin/android/location/Location) (от более старой позиции к более новой), используя метод [onLocationChanged()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener#nav-lvl1--onLocationChanged).

```kotlin
val location = Location(...)
val newLocations = arrayOf(location)
listener.onLocationChanged(newLocations)
```

Чтобы сообщить изменение доступности источника, можно вызвать метод [onAvailabilityChanged()](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener#nav-lvl1--onAvailabilityChanged).

Дополнительно можно менять логику определения геопозиции в зависимости от требуемой точности. Требуемая точность передаётся в функцию `setDesiredAccuracy()` в виде объекта [DesiredAccuracy](/ru/android/sdk/reference/2.0/ru.dgis.sdk.positioning.DesiredAccuracy).

Когда источник геопозиции больше не требуется, будет вызвана функция `deactivate()`.
