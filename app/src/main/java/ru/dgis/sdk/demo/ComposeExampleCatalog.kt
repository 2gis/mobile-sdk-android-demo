package ru.dgis.sdk.demo

import ru.dgis.sdk.Context
import ru.dgis.sdk.File
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.createDgisSources
import ru.dgis.sdk.demo.compose.CatalogEntry
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.examples.controls.ControlsExample
import ru.dgis.sdk.demo.compose.examples.copyright.CopyrightExample
import ru.dgis.sdk.demo.compose.examples.fps.FpsExample
import ru.dgis.sdk.demo.compose.examples.markers.MarkersExample
import ru.dgis.sdk.demo.compose.examples.minimap.MinimapExample
import ru.dgis.sdk.demo.compose.examples.navigation.NavigationExample
import ru.dgis.sdk.demo.compose.examples.objects.ObjectsExample
import ru.dgis.sdk.demo.compose.examples.rendermode.RenderModeExample
import ru.dgis.sdk.demo.compose.examples.routeEditor.RouteEditorExample
import ru.dgis.sdk.demo.compose.examples.searchitem.SearchItemExample
import ru.dgis.sdk.demo.compose.examples.sharedroute.SharedRouteExample
import ru.dgis.sdk.demo.compose.examples.snapshot.SnapshotExample
import ru.dgis.sdk.demo.compose.examples.theme.ThemeExample
import ru.dgis.sdk.demo.compose.home.HomeScreenViewModel
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.MapAppearance
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MapCopyrightOptions
import ru.dgis.sdk.map.MapRenderOptions
import ru.dgis.sdk.map.RoadEventSource
import ru.dgis.sdk.map.Zoom

/** The compose examples of the demo app. */
val composeExampleCatalog: List<CatalogEntry> = listOf(
    MinimapExample,
    ThemeExample,
    RenderModeExample,
    CopyrightExample,
    FpsExample,
    ObjectsExample,
    SnapshotExample,
    ControlsExample,
    MarkersExample,
    SearchItemExample,
    NavigationExample,
    RouteEditorExample,
    SharedRouteExample,
).map { CatalogEntry(it) }

/** The model of the catalog: the map of the examples and how they show it. */
fun composeHomeScreenViewModel(sdkContext: Context) = HomeScreenViewModel(
    sdkContext = sdkContext,
    mapOptions = ComposeExampleMapOptions(
        renderOptions = MapRenderOptions(),
        copyrightOptions = MapCopyrightOptions(),
        minimapControllerOptions = { context ->
            MapControllerOptions(
                position = composeCameraPosition,
                styleFile = File.fromAsset(context, "minimap-styles.2gis"),
                // The theme opposite to the main map's, so the minimap stays visible on top.
                mapAppearance = MapAppearance.defaultAppearanceInverted(),
            )
        },
    ),
    controllerOptions = {
        MapControllerOptions(
            position = composeCameraPosition,
            sources = createDgisSources(sdkContext) + RoadEventSource(sdkContext),
            mapAppearance = MapAppearance.defaultAppearance(),
        )
    },
)

private val composeCameraPosition = CameraPosition(
    point = GeoPoint(latitude = 55.760898, longitude = 37.620242),
    bearing = Bearing(20.0),
    zoom = Zoom(17f),
)
