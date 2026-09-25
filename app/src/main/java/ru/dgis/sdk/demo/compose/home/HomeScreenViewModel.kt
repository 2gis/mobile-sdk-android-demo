package ru.dgis.sdk.demo.compose.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.map.DefaultMapControllerViewModel
import ru.dgis.sdk.map.MapControllerFactory
import ru.dgis.sdk.map.MapControllerOptions

/**
 * Defines the behavior applied when the user opens an example.
 */
enum class MapStateResolveRule {
    /** Closes the map of the previous example and creates a new one */
    Unique,

    /** Reuses a single map controller instance across multiple screens */
    Shared
}

/**
 * Owns the map of the catalog and applies [MapStateResolveRule] when an example is opened.
 * The app gives it the map settings, so the catalog itself knows nothing about them.
 *
 * @property sdkContext the SDK context of the maps and the examples.
 * @property mapOptions how the example screens show the map.
 * @param controllerOptions options of a new map; called for every map the catalog creates.
 * @param controllerFactory creates the map; called for every map the catalog creates.
 */
open class HomeScreenViewModel(
    val sdkContext: Context,
    val mapOptions: ComposeExampleMapOptions,
    private val controllerOptions: () -> MapControllerOptions,
    private val controllerFactory: () -> MapControllerFactory = { MapControllerFactory.Default }
) : ViewModel() {

    private var _mapViewModel: DefaultMapControllerViewModel? = null

    val mapViewModel: DefaultMapControllerViewModel
        get() = _mapViewModel ?: DefaultMapControllerViewModel(
            sdkContext,
            controllerOptions(),
            controllerFactory()
        ).also { _mapViewModel = it }

    private val _resolveRule = MutableStateFlow(MapStateResolveRule.Unique)
    val resolveRule = _resolveRule.asStateFlow()

    /**
     * Updates the rule applied the next time an example is opened.
     */
    fun setResolveRule(mapStateResolveRule: MapStateResolveRule) {
        _resolveRule.value = mapStateResolveRule
    }

    /**
     * Applies [resolveRule] to the map of the previous example: in
     * [MapStateResolveRule.Unique] mode it is closed, so the opened example gets a new map.
     */
    fun onExampleOpened() {
        if (_resolveRule.value == MapStateResolveRule.Unique) {
            closeMapViewModel()
        }
    }

    override fun onCleared() {
        closeMapViewModel()
    }

    private fun closeMapViewModel() {
        _mapViewModel?.close()
        _mapViewModel = null
    }
}
