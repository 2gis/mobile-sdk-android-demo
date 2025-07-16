package ru.dgis.sdk.demo

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import ru.dgis.sdk.map.StyleZoom
import ru.dgis.sdk.navigation.SpeedRange
import ru.dgis.sdk.navigation.SpeedRangeToStyleZoom
import ru.dgis.sdk.routing.RouteDistance

class StyleSpeedToZoomRelationProvider() {

    private val speedToZoomSettings: List<SpeedToZoomSettings>

    init {
        val fileName = "speed_to_zoom_restrictions.json"
        val jsonString = loadJsonFromAssets(fileName)
        if (jsonString != null) {
            val type = object : TypeToken<SpeedToZoomRestrictions>() {}.type
            val result: SpeedToZoomRestrictions? = Gson().fromJson(jsonString, type)
            if (result != null) {
                this.speedToZoomSettings = result.overrides.speedToZoomSettings
                Log.i("APP", "speed to zoom settings successfully loaded")
            } else {
                this.speedToZoomSettings = emptyList()
                Log.e("APP", "speed to zoom settings load error")
            }
        } else {
            this.speedToZoomSettings = emptyList()
            Log.e("APP", "speed to zoom settings load error")
        }
    }

    fun speedRangeToStyleZooms(): List<SpeedRangeToStyleZoom> {
        return speedToZoomSettings.map {
            SpeedRangeToStyleZoom(
                range = SpeedRange(it.range.minSpeed, it.range.maxSpeed),
                minDistanceToManeuver = RouteDistance(it.minDistanceToManeuver),
                maxDistanceToManeuver = RouteDistance(it.maxDistanceToManeuver),
                styleZoom = StyleZoom(it.styleZoom)
            )
        }
    }
}

data class SpeedToZoomRestrictions(
    val name: String,
    val comment: String,
    val overrides: Overrides
) {
    data class Overrides(
        @SerializedName("dgis/native-sdk/navigation/plugins/map/map_location_controller/ZoomFollowSettings/free_roam_speed_range_sequence")
        val speedToZoomSettings: List<SpeedToZoomSettings>
    )
}

// SpeedToZoomSettings class
data class SpeedToZoomSettings(
    @SerializedName("style_zoom")
    val styleZoom: Float,

    @SerializedName("min_distance_to_maneuver")
    val minDistanceToManeuver: Long,

    @SerializedName("max_distance_to_maneuver")
    val maxDistanceToManeuver: Long,

    val range: SpeedRange
) {
    // SpeedRange class
    data class SpeedRange(
        @SerializedName("min_speed")
        val minSpeed: Double,

        @SerializedName("max_speed")
        val maxSpeed: Double
    )
}
