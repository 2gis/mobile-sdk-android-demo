package ru.dgis.sdk.demo.markers.model.remote

import ru.dgis.sdk.coordinates.GeoPoint

data class MarkersResponse(
    val objects: List<MapSearchItem>
)

data class MapSearchItem(
    val id: String? = null,
    val object_id: String? = null,
    val title: String? = null,
    val lat: String? = null,
    val lon: String? = null,
    val image: String? = null,
    val cost_min: Int? = null,
    val cnt: Int? = null,
) {
    fun getPosition(): GeoPoint? {
        lat ?: return null
        lon ?: return null
        return GeoPoint(
            lat.toDouble(),
            lon.toDouble()
        )
    }
}
