package ru.dgis.sdk.demo.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.GeoRect
import ru.dgis.sdk.demo.common.asFlow
import ru.dgis.sdk.update.Territory
import ru.dgis.sdk.update.TerritoryManager

sealed class Geometry {
    data class Point(
        val geoPoint: GeoPoint
    ) : Geometry()

    data class Rect(
        val geoRect: GeoRect
    ) : Geometry()
}

class DownloadTerritoriesViewModel : ViewModel() {
    private val territoryManager = TerritoryManager.instance(DGis.context())

    private val _territories = MutableStateFlow(listOf<Territory>())
    val packages = _territories.asStateFlow()

    private fun sortTerritories(territories: List<Territory>): List<Territory> =
        territories.sortedWith(
            compareBy<Territory> { territory -> !territory.info.installed }
                .thenBy { territory -> territory.info.name }
        )

    private fun getTerritories(
        nameFilter: String?,
        geometryFilter: Geometry?
    ): List<Territory> {
        var list = when (geometryFilter) {
            is Geometry.Point -> territoryManager.findByPoint(geometryFilter.geoPoint)
            is Geometry.Rect -> territoryManager.findByRect(geometryFilter.geoRect)
            null -> territoryManager.territories
        }

        if (!nameFilter.isNullOrBlank()) {
            val query = nameFilter.trim().lowercase()
            list = list.filter { it.info.name.lowercase().contains(query) }
        }
        return sortTerritories(list)
    }

    var geometryFilter: Geometry? = null
        set(value) {
            if (value == field) return
            field = value
            _territories.value = getTerritories(nameFilter, value)
        }

    var nameFilter: String? = null
        set(value) {
            if (value == field) return
            field = value
            _territories.value = getTerritories(value, geometryFilter)
        }

    init {
        viewModelScope.launch {
            territoryManager.territoriesChannel.asFlow().collect { _ ->
                _territories.value = getTerritories(nameFilter, geometryFilter)
            }
        }
    }
}
