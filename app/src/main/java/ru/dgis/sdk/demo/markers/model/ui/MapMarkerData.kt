package ru.dgis.sdk.demo.markers.model.ui

import ru.dgis.sdk.coordinates.GeoPoint

data class MapMarkerData(
    val id: String,
    val objectId: String,
    val markerTitle: String,
    val markerDescription: String,
    val markerPosition: GeoPoint
)
