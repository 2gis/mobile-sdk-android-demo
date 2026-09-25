package ru.dgis.sdk.demo.compose

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapController
import ru.dgis.sdk.map.MapControllerState
import ru.dgis.sdk.map.MapControllerViewModel

/**
 * [MapControllerViewModel] over an already created controller: the state is always
 * [MapControllerState.Created], so [mapController] and [map] are available directly.
 *
 * Does not own the controller — it is closed by whoever created it.
 */
class ReadyMapControllerViewModel(
    val mapController: MapController,
) : MapControllerViewModel {

    override val state: StateFlow<MapControllerState> =
        MutableStateFlow(MapControllerState.Created(mapController))

    val map: Map
        get() = mapController.map
}
