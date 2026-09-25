package ru.dgis.sdk.demo.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModelStoreOwner
import ru.dgis.sdk.Context
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MapCopyrightOptions
import ru.dgis.sdk.map.MapRenderOptions

/**
 * An example: what it is and how to show it. Lives next to the example screen,
 * so every app with the catalog takes the same description and texts.
 *
 * @property id the route of the example in the catalog.
 * @property title the name of the example in the catalog.
 * @property summary one line under the title in the catalog.
 * @property description what the example shows and how to use it; opened from the catalog.
 * @property topic what the example is about; the tab of the catalog it is in.
 * @property content shows the example; what it needs comes from the host
 * through [ComposeExampleEnvironment].
 */
data class ComposeExample(
    val id: String,
    val title: String,
    @StringRes val summary: Int,
    @StringRes val description: Int,
    val topic: ComposeExampleTopic,
    val content: @Composable ComposeExampleEnvironment.() -> Unit,
)

/** What an example is about; the catalog has a tab per topic. */
enum class ComposeExampleTopic {
    Map,
    Navigation,
    Directory,
}

/**
 * How the examples show the map. The host creates it and gives it to the examples
 * through [ComposeExampleEnvironment].
 *
 * @property renderOptions how the map view renders.
 * @property copyrightOptions the copyright overlay of the map view.
 * @property minimapControllerOptions options of a minimap shown over the map.
 */
data class ComposeExampleMapOptions(
    val renderOptions: MapRenderOptions,
    val copyrightOptions: MapCopyrightOptions,
    val minimapControllerOptions: (Context) -> MapControllerOptions,
)

/**
 * What the host gives to an example it shows.
 *
 * @property mapViewModel a ready map: the host takes care of awaiting its creation.
 * @property viewModelStoreOwner the owner of the example's view models.
 * @property sdkContext the SDK context, so the example does not reach for it globally.
 * @property mapOptions how the example shows the map.
 */
class ComposeExampleEnvironment(
    val mapViewModel: ReadyMapControllerViewModel,
    val viewModelStoreOwner: ViewModelStoreOwner,
    val sdkContext: Context,
    val mapOptions: ComposeExampleMapOptions,
)

/**
 * A label the app puts on its examples in the catalog, like where an example lives.
 * The catalog shows the labels only when its examples have more than one of them.
 *
 * @property name the name on the filter chip.
 * @property icon the mark of the label in the list and on the chip.
 * @property description what the examples with the label are.
 */
data class ComposeExampleLabel(
    val name: String,
    val icon: ImageVector,
    @StringRes val description: Int,
)

/** A line of the catalog: an example with a label of the app, or without one. */
data class CatalogEntry(
    val example: ComposeExample,
    val label: ComposeExampleLabel? = null,
)

/** Puts [label] on the examples. */
fun List<ComposeExample>.withLabel(label: ComposeExampleLabel): List<CatalogEntry> =
    map { CatalogEntry(it, label) }
