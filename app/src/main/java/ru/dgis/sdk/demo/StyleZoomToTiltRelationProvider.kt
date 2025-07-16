package ru.dgis.sdk.demo

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import ru.dgis.sdk.map.StyleZoom
import ru.dgis.sdk.map.StyleZoomToTiltRelation
import ru.dgis.sdk.map.Tilt

/**
 * #углы_наклона
 */
class StyleZoomToTiltRelationProvider() : StyleZoomToTiltRelation {

    private val tiltSettings: TiltSettings

    init {
        val jsonString = loadJsonFromAssets("tilt_restrictions.json")
        if (jsonString != null) {
            val type = object : TypeToken<TiltRestrictions>() {}.type
            val result: TiltRestrictions? = Gson().fromJson(jsonString, type)

            if (result != null &&
                result.overrides.viewportTiltRestrictions.tiltStyleZooms.size == result.overrides.viewportTiltRestrictions.tiltValues.size
            ) {
                this.tiltSettings = result.overrides.viewportTiltRestrictions
                Log.i("APP", "tilt settings successfully loaded")
            } else {
                this.tiltSettings =
                    TiltSettings(tiltStyleZooms = listOf(0f), tiltValues = listOf(0f))
                Log.e("APP", "tilt settings load error")
            }
        } else {
            this.tiltSettings = TiltSettings(tiltStyleZooms = listOf(0f), tiltValues = listOf(0f))
            Log.e("APP", "tilt settings load error")
        }
    }

    override fun styleZoomToTilt(styleZoom: StyleZoom): Tilt {
        val value = getTiltValue(styleZoom.value)
        return Tilt(value)
    }

    // Приватный метод для получения значения наклона на основе значения zoom
    private fun getTiltValue(zoomValue: Float): Float {
        if (tiltSettings.tiltStyleZooms.isEmpty()) return 0f

        var nearestZoomIndex = 0
        for ((index, zoom) in tiltSettings.tiltStyleZooms.withIndex()) {
            if (zoom > zoomValue) {
                break
            } else {
                nearestZoomIndex = index
            }
        }

        return tiltSettings.tiltValues.getOrNull(nearestZoomIndex) ?: 0f
    }
}

data class TiltRestrictions(
    val name: String,
    val comment: String,
    val overrides: Overrides
) {
    data class Overrides(
        @SerializedName("dgis/core/map/viewport/tilt_restrictions")
        val viewportTiltRestrictions: TiltSettings
    )
}

data class TiltSettings(
    @SerializedName("tilt_style_zooms")
    val tiltStyleZooms: List<Float>,
    @SerializedName("tilt_values")
    val tiltValues: List<Float>
)
