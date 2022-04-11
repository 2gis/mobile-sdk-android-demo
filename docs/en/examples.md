## Getting started

To begin working with the SDK, call the `initialize()` method of [DGis](/en/android/sdk/reference/2.0/ru.dgis.sdk.DGis) and specify the application context and a set of access keys ([ApiKeys](/en/android/sdk/reference/2.0/ru.dgis.sdk.ApiKeys)).

This SDK uses two types of access keys: main SDK key (`map`) and key to access additional APIs like routing and object directory (`directory`).

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

Additionally, you can specify logging settings ([LogOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.LogOptions)) and HTTP client settings ([HttpOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.HttpOptions)) such as caching.

```kotlin
// Access keys
val apiKeys = ApiKeys(
    directory = "Directory API key",
    map = "SDK key"
)

// Logging settings
val logOptions = LogOptions(
    LogLevel.VERBOSE
)

// HTTP client settings
val httpOptions = HttpOptions(
    useCache = false
)

// Consent to personal data processing
val dataCollectConsent = PersonalDataCollectionConsent.GRANTED

sdkContext = DGis.initialize(
    appContext = this,
    apiKeys = apiKeys,
    dataCollectConsent = dataCollectConsent,
    logOptions = logOptions,
    httpOptions = httpOptions
)
```

## Creating a map

To display a map, add a [MapView](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView) to your activity:

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

You can specify starting coordinates (`cameraTargetLat` for latitude and `cameraTargetLng` for longitude) and zoom level (`cameraZoom`).

[MapView](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView) can also be created programmatically. In that case, you can specify starting coordinates and other settings as a [MapOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapOptions) object.

To get the [Map](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Map) object, you can call the `getMapAsync()` method of [MapView](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView):

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val sdkContext = DGis.initialize(applicationContext, apiKeys)
    setContentView(R.layout.activity_main)

    val mapView = findViewById<MapView>(R.id.mapView)
    lifecycle.addObserver(mapView)

    mapView.getMapAsync { map ->
        // Access map properties
        val camera = map.camera
    }
}
```

## General principles

### Deferred results

Some SDK methods (e.g., those that access a remote server) return deferred results ([Future](/en/android/sdk/reference/2.0/ru.dgis.sdk.Future)). To process a deferred result, you can register two callback functions: completion and error.

For example, to get information from object directory, you can process [Future](/en/android/sdk/reference/2.0/ru.dgis.sdk.Future) like so:

```kotlin
// Create an object for directory search
val searchManager = SearchManager.createOnlineManager(sdkContext)

// Get object by identifier
val future = searchManager.searchByDirectoryObjectId(objectId)

// Completion callback
future.onResult { directoryObject ->
    Log.d("APP", "Object title: ${directoryObject.title}")
}

// Error callback
future.onError { error ->
    Log.d("APP", "An error occurred retrieving information from the directory.")
}
```

By default, results are processed in the UI thread. To change this, you can specify [Executor](https://developer.android.com/reference/java/util/concurrent/Executor.html) for both functions.

For more information on working with object directory, see [Object directory](#nav-lvl1--Object_directory).

### Data channels

Some SDK objects provide data channels (see the [Channel](/en/android/sdk/reference/2.0/ru.dgis.sdk.Channel) interface). To subscribe to a data channel, you need to create and specify a handler function.

For example, you can subscribe to a visible rectangle channel, which is updated when the visible area of the map is changed:

```kotlin
// Choose a data channel
val visibleRectChannel = map.camera.visibleRectChannel

// Subscribe to the channel and process the results in the main thread.
// It is important to prevent the connection object from getting garbage collected to keep the subscription active.
val connection = visibleRectChannel.connect { geoRect ->
    Log.d("APP", "${geoRect.southWestPoint.latitude.value}")
}
```

When the data processing is no longer required, it is important to close the connection to avoid memory leaks. To do this, call the `close()` method:

```kotlin
connection.close()
```

### Map data sources

In some cases, to add objects to the map, you need to create a special object - a data source. Data sources act as object managers: instead of adding objects to the map directly, you add a data source to the map and add/remove objects from the data source.

There are different types of data sources: moving markers, routes that display current traffic condition, custom geometric shapes, etc. Each data source type has a corresponding class.

The general workflow of working with data sources looks like this:

```kotlin
// Create a data source
val source = MyMapObjectSource(
    sdkContext,
    ...
)

// Add the data source to the map
map.addSource(source)

// Add and remove objects from the data source
source.addObject(...)
source.removeObject(...)
```

To remove a data source and all objects associated with it from the map, call the `removeSource()` method:

```kotlin
map.removeSource(source)
```

You can get the list of all active data sources using the `map.sources` property.

## Adding objects

To add dynamic objects to the map (such as markers, lines, circles, and polygons), you must first create a [MapObjectManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager) object, specifying the map instance. Deleting an object manager removes all associated objects from the map, so do not forget to save it in activity.

```kotlin
mapObjectManager = MapObjectManager(map)
```

After you have created an object manager, you can add objects to the map using the [addObject()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObject) and [addObjects()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--addObjects) methods. For each dynamic object, you can specify a `userData` field to store arbitrary data. Object settings can be changed after their creation.

To remove objects from the map, use [removeObject()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObject) and [removeObjects()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeObjects). To remove all objects, call the [removeAll()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--removeAll) method.

### Marker

To add a marker to the map, create a [Marker](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Marker) object, specifying the required options, and pass it to the `addObject()` method of the object manager.

The only required parameter is the coordinates of the marker (`position`).

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

To change the marker icon, specify an [Image](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Image) object as the `icon` parameter. You can create [Image](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Image) using the following functions:

- [imageFromAsset()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromAsset(context%2CassetName%2Csize))
- [imageFromBitmap()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromBitmap(context%2Cbitmap))
- [imageFromCanvas()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromCanvas(context%2Csize%2Cblock))
- [imageFromResource()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromResource(context%2CresourceId%2Csize))
- [imageFromSvg()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.imageFromSvg(context%2Cdata))

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

To change the hotspot of the icon, use the [anchor](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Anchor) parameter.

You can also set the text for the marker and other options (see [MarkerOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MarkerOptions)).

### Line

To draw a line on the map, create a [Polyline](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Polyline) object, specifying the required options, and pass it to the `addObject()` method of the object manager.

In addition to the coordinates of the line points, you can set the line width, color, stroke type, and other options (see [PolylineOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.PolylineOptions)).

```kotlin
// Coordinates of the vertices of the polyline
val points = listOf(
    GeoPoint(latitude = 55.7513, longitude = 37.6236),
    GeoPoint(latitude = 55.7405, longitude = 37.6235),
    GeoPoint(latitude = 55.7439, longitude = 37.6506)
)

// Creating a Polyline object
val polyline = Polyline(
    PolylineOptions(
        points = points,
        width = 2.lpx
    )
)

// Adding the polyline to the map
mapObjectManager.addObject(polyline)
```

Extension property `.lpx` in the example above converts an integer to a [LogicalPixel](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.LogicalPixel) object.

### Polygon

To draw a polygon on the map, create a [Polygon](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Polygon) object, specifying the required options, and pass it to the `addObject()` method of the object manager.

Coordinates for the polygon are specified as a two-dimensional list. The first sublist must contain the coordinates of the vertices of the polygon itself. The other sublists are optional and can be specified to create a cutout (a hole) inside the polygon (one sublist - one polygonal cutout).

Additionally, you can specify the polygon color and stroke options (see [PolygonOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.PolygonOptions)).

```kotlin
val polygon = Polygon(
    PolygonOptions(
        contours = listOf(
            // Vertices of the polygon
            listOf(
                GeoPoint(latitude = 55.72014932919687, longitude = 37.562599182128906),
                GeoPoint(latitude = 55.72014932919687, longitude = 37.67555236816406),
                GeoPoint(latitude = 55.78004852149085, longitude = 37.67555236816406),
                GeoPoint(latitude = 55.78004852149085, longitude = 37.562599182128906),
                GeoPoint(latitude = 55.72014932919687, longitude = 37.562599182128906)
            ),
            // Cutout inside the polygon
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

### Clustering

To add markers to the map in clustering mode, you must create a [MapObjectManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager) object using [MapObjectManager.withClustering()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager#nav-lvl1--withClustering), specifying the map instance, distance between clusters in logical pixels, maximum value of zoom-level, when MapObjectManager in clustering mode, and user implementation of the protocol SimpleClusterRenderer.
[SimpleClusterRenderer](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.SimpleClusterRenderer) is used to customize clusters in [MapObjectManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapObjectManager).

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

## Controlling the camera

You can control the camera by accessing the `map.camera` property. See the [Camera](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Camera) object for a full list of available methods and properties.

### Changing camera position

You can change the position of the camera by calling the [move()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Camera#nav-lvl1--move) method, which initiates a flight animation. This method has three parameters:

- `position` - new camera position (coordinates and zoom level). Additionally, you can specify the camera tilt and rotation (see [CameraPosition](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraPosition)).
- `time` - flight duration in seconds as a [Duration](/en/android/sdk/reference/2.0/ru.dgis.sdk.Duration) object.
- `animationType` - type of animation to use ([CameraAnimationType](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraAnimationType)).

The call will return a [Future](/en/android/sdk/reference/2.0/ru.dgis.sdk.Future) object, which can be used to handle the animation finish event.

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
        Log.d("APP", "Camera flight finished.")
    }
}
```

You can use the `.seconds` extension to specify the duration of the flight:

```kotlin
map.camera.move(cameraPosition, 2.seconds, CameraAnimationType.LINEAR)
```

For more precise control over the flight, you can create a flight controller that will determine the camera position at any given moment. To do this, implement the [CameraMoveController](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraMoveController) interface and pass the created object to the `move()` method instead of the three parameters described previously.

### Getting camera state

The current state of the camera (i.e., whether the camera is currently in flight) can be obtained using the `state` property. See [CameraState](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraState) for a list of possible camera states.

```kotlin
val currentState = map.camera.state
```

You can subscribe to changes of camera state using the `stateChannel` property.

```kotlin
// Subscribe to camera state changes
val connection = map.camera.stateChannel.connect { state ->
    Log.d("APP", "Camera state has changed to ${state}")
}

// Unsubscribe when it's no longer needed
connection.close()
```

### Getting camera position

The current position of the camera can be obtained using the `position` property (see [CameraPosition](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.CameraPosition)).

```kotlin
val currentPosition = map.camera.position

Log.d("APP", "Coordinates: ${currentPosition.point}")
Log.d("APP", "Zoom level: ${currentPosition.zoom}")
Log.d("APP", "Tilt: ${currentPosition.tilt}")
Log.d("APP", "Rotation: ${currentPosition.bearing}")
```

You can subscribe to changes of camera position using the `positionChannel` property.

```kotlin
// Subscribe to camera position changes
val connection = map.camera.positionChannel.connect { position ->
    Log.d("APP", "Camera position has changed (coordinates, zoom level, tilt, or rotation).")
}

// Unsubscribe when it's no longer needed
connection.close()
```

## My location

You can add a special marker to the map that will be automatically updated to reflect the current location of the device. To do this, create a [MyLocationMapObjectSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MyLocationMapObjectSource) data source and add it to the map.

```kotlin
// Create the data source
val source = MyLocationMapObjectSource(
    sdkContext,
    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING
)

// Add the data source to the map
map.addSource(source)
```

## Getting objects using screen coordinates

You can get information about map objects using pixel coordinates. For this, call the [getRenderedObjects()](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.Map#nav-lvl1--getRenderedObjects) method of the map and specify the pixel coordinates and the radius in screen millimeters. The method will return a deferred result ([Future](/en/android/sdk/reference/2.0/ru.dgis.sdk.Future)) containing information about all found objects within the specified radius on the visible area of the map (a list of [RenderedObjectInfo](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RenderedObjectInfo)).

An example of a function that takes tap coordinates and passes them to `getRenderedObjects()`:

```kotlin
override fun onTap(point: ScreenPoint) {
    map.getRenderedObjects(point, ScreenDistance(5f)).onResult { renderedObjectInfos ->
        // First list object is the closest to the coordinates
        for (renderedObjectInfo in renderedObjectInfos) {
            Log.d("APP", "Arbitrary object data: ${renderedObjectInfo.item.item.userData}")
        }
    }
}
```

## Object directory

To search for objects in the directory, first create a [SearchManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager) object by calling one of the following methods:

- [SearchManager.createOnlineManager()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOnlineManager) - creates an object to work with an online directory.
- [SearchManager.createOfflineManager()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createOfflineManager) - creates an object to work with an offline directory (preloaded data only).
- [SearchManager.createSmartManager()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--createSmartManager) - creates an object that works primarily with online data and switches to offline mode when the network goes down.

```kotlin
val searchManager = SearchManager.createSmartManager(sdkContext)
```

Then, to get object information by its ID, call the [searchById()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--searchById) method. The method will return a deferred result with [DirectoryObject](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.DirectoryObject).

```kotlin
searchManager.searchById(id).onResult { directoryObject ->
    Log.d("APP", "Object title: ${directoryObject.title}")
}
```

If the object ID is not known, you can create a [SearchQuery](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchQuery) object using [SearchQueryBuilder](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchQueryBuilder) and pass it to the [search()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--search) method. The method will return a deferred result with a [SearchResult](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchResult) object, which will contain a paginated list of [DirectoryObject](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.DirectoryObject).

```kotlin
val query = SearchQueryBuilder.fromQueryText("pizza").setPageSize(10).build()

searchManager.search(query).onResult { searchResult ->
    // Get the first object of the first page
    val directoryObject = searchResult.firstPage?.items?.getOrNull(0) ?: return
    Log.d("APP", "Object title: ${directoryObject.title}")
}
```

To get the next page of search results, use the [fetchNextPage()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.Page#nav-lvl1--fetchNextPage) method of the page, which will return a deferred result with a [Page](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.Page) object.

```kotlin
firstPage.fetchNextPage().onResult { nextPage
    val directoryObject = nextPage?.items?.getOrNull(0) ?: return
}
```

You can also use object directory to get suggestions when text searching (see [Suggest API](/en/api/search/suggest/overview) for demonstration). To do this, create a [SuggestQuery](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestQuery) object using [SuggestQueryBuilder](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestQueryBuilder) and pass it to the [suggest()](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SearchManager#nav-lvl1--suggest) method. The method will return a deferred result with a [SuggestResult](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.SuggestResult) object, which will contain a list of [Suggest](/en/android/sdk/reference/2.0/ru.dgis.sdk.directory.Suggest) objects.

```kotlin
val query = SuggestQueryBuilder.fromQueryText("pizz").setLimit(10).build()

searchManager.suggest(query).onResult { suggestResult ->
    // Get the first suggestion from the list
    val firstSuggest = suggestResult.suggests?.getOrNull(0) ?: return
    Log.d("APP", "Suggestion title: ${firstSuggest.title}")
}
```

## Building a route

In order to create a route on the map, you need to create two objects: [TrafficRouter](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter) to find an optimal route and [RouteMapObjectSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource) to display it on the map.

To find a route between two points, call the [findRoute()](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter#nav-lvl1--findRoute) method and specify the coordinates of the start and end points as [RouteSearchPoint](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteSearchPoint) objects. You can additionally specify a list of intermediate points of the route and [RouteOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteOptions).

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

The `findRoute()` call will return a deferred result with a list of [TrafficRoute](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute) objects. To display the found route on the map, you need to use these objects to create [RouteMapObject](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObject) objects and add them to a [RouteMapObjectSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource) data source.

```kotlin
// Create a data source
val routeMapObjectSource = RouteMapObjectSource(sdkContext, RouteVisualizationType.NORMAL)
map.addSource(routeMapObjectSource)

// Find a route
val routesFuture = trafficRouter.findRoute(startSearchPoint, finishSearchPoint)
val trafficRouter = TrafficRouter(sdkContext)

// After receiving the route, add it to the map
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

Instead of using [TrafficRouter](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRouter) and [RouteMapObjectSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteMapObjectSource) objects and manually processing a list of [TrafficRoute](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute) objects, you can use [RouteEditor](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteEditor) and [RouteEditorSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.RouteEditorSource). In that case, you can simply pass the coordinates for the route as a [RouteParams](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteParams) object to the [setRouteParams()](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.RouteEditor#nav-lvl1--setRouteParams) method and the route will be displayed automatically.

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

## Turn-by-turn navigation

You can add a turn-by-turn navigation to your app using the ready-to-use interface components and the [NavigationManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager) class.

To do that, first add a [NavigationView](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationView) and a [DefaultNavigationControls](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.DefaultNavigationControls) to your [MapView](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.MapView).

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

Then, add a [My location marker](#nav-lvl1--My_location) to the map and create a [NavigationManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager) object.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    sdkContext = DGis.initialize(applicationContext, apiKeys)

    // Register the geolocation source
    locationProvider = ManagerLocationSource(applicationContext)
    registerPlatformLocationSource(sdkContext, locationProvider)

    setContentView(R.layout.activity_navigation)

    findViewById<MapView>(R.id.mapView).apply { mapView ->
        lifecycle.addObserver(mapView)

        mapView.getMapAsync { map ->
            // Add a marker for the current location
            map.addSource(
                MyLocationMapObjectSource(
                    sdkContext,
                    MyLocationDirectionBehaviour.FOLLOW_SATELLITE_HEADING,
                    createSmoothMyLocationController()
                )
            )
        }
    }
    
    // Create a NavigationManager object
    navigationManager = NavigationManager(sdkContext)

    findViewById<NavigationView>(R.id.navigationView).apply {
        // Attach the created NavigationManager object to the NavigationView
        navigationManager = this@NavigationActivity.navigationManager
    }
    
    // Start navigation in a free-drive mode
    navigationManager.start()
}
```

You can start navigation in one of three modes: free-drive, turn-by-turn, and the simulated navigation mode.

Additional navigation settings can be changed using the properties of the [NavigationManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager#nav-lvl1--val%20uiModel) object.

### Free-drive mode

In free-drive mode, no route will be displayed on the map, but the user will still be informed about speed limits, traffic cameras, incidents, and road closures.

To start navigation in this mode, call the `start()` method without arguments.

```kotlin
navigationManager.start()
```

### Turn-by-turn mode

In turn-by-turn mode, a route will be displayed on the map and the user will receive navigation instructions as they move along the route.

To start navigation in this mode, call the `start()` method and specify a [RouteBuildOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.RouteBuildOptions) object - arrival coordinates and route settings.

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

Additionally, when calling the `start()` method, you can specify a [TrafficRoute](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute) object - a complete route object for navigation (see [Building a route](#nav-lvl1--Building_a_route)). In this case, [NavigationManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager) will use the specified route instead of building a new one.

```kotlin
navigationManager.start(routeBuildOptions, trafficRoute)
```

### Simulated navigation

In this mode, [NavigationManager](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.NavigationManager) will not track the current location of the device. Instead, it will simulate a movement along the specified route.

This mode is useful for debugging.

To use this mode, call the `startSimulation()` method and specify a [RouteBuildOptions](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.RouteBuildOptions) object (route settings) and a [TrafficRoute](/en/android/sdk/reference/2.0/ru.dgis.sdk.routing.TrafficRoute) object (the route itself).

You can change the speed of the simulated movement using the [SimulationSettings.speed](/en/android/sdk/reference/2.0/ru.dgis.sdk.navigation.SimulationSettings) property (specified in meters per second).

```kotlin
navigationManager.simulationSettings.speed = 30 / 3.6
navigationManager.startSimulation(routeBuildOptions, trafficRoute)
```

To stop the simulation, call the `stop()` method.

```kotlin
navigationManager.stop()
```

### Traffic display

To display traffic on the map, add a [TrafficSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.map.TrafficSource) data source.

```kotlin
val trafficSource = TrafficSource(sdkContext)
map.addSource(trafficSource)
```

## Custom geolocation source

You can use your own geolocation source within the SDK. To do this, first implement the [LocationSource](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationSource) interface.

```kotlin
public class CustomLocationSource: LocationSource {
    override fun activate(listener: LocationChangeListener?) {
        // Geolocation source has been activated
    }

    override fun deactivate() {
        // Geolocation source has been deactivated
    }

    override fun setDesiredAccuracy(accuracy: DesiredAccuracy?) {
        // Change of required accuracy level has been requested
    }
}
```

Then, register the created source in the SDK using the [registerPlatformLocationSource()](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.registerPlatformLocationSource) function.

```kotlin
val customSource = CustomLocationSource()
registerPlatformLocationSource(sdkContext, customSource)
```

The entry point of the interface is the `activate()` function. When the SDK requires a geolocation, it will call this function passing a [LocationChangeListener](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener) object. After that, to report the current geolocation, you need to pass an array of [Location](https://developer.android.com/reference/kotlin/android/location/Location) objects (ordered from oldest to newest) to the [onLocationChanged()](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener#nav-lvl1--onLocationChanged) method.

```kotlin
val location = Location(...)
val newLocations = arrayOf(location)
listener.onLocationChanged(newLocations)
```

To notify of a change in source availability, use the [onAvailabilityChanged()](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.LocationChangeListener#nav-lvl1--onAvailabilityChanged) method.

Optionally, you can add additional logic to handle different levels of geolocation accuracy. The required accuracy is passed to the `setDesiredAccuracy()` function as a [DesiredAccuracy](/en/android/sdk/reference/2.0/ru.dgis.sdk.positioning.DesiredAccuracy) object.

When the geolocation source is no longer needed, the SDK will call the `deactivate()` function.
