package ru.dgis.sdk.demo.markers

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import ru.dgis.sdk.demo.markers.model.remote.MapSearchItem
import ru.dgis.sdk.demo.markers.model.remote.MarkersResponse
import ru.dgis.sdk.demo.markers.model.ui.MapMarkerData

class MapMarkersViewModel : ViewModel() {
    private val _markers = MutableLiveData<List<MapMarkerData>>()
    val markers: LiveData<List<MapMarkerData>>
        get() = _markers

    fun loadMarkers(context: Context) {
        val markersJson = loadJsonFromAsset(context)
        val markersResponse =
            try {
                Gson().fromJson(markersJson, MarkersResponse::class.java)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                null
            }
        val mappedMarkers = markersResponse?.let { map(it.objects) }
        mappedMarkers?.let {
            _markers.value = it
        }
    }

    fun map(items: List<MapSearchItem>): List<MapMarkerData> {
        return items
            .filter { it.getPosition() != null }
            .map {
                MapMarkerData(
                    id = it.id.orEmpty(),
                    objectId = it.object_id.orEmpty(),
                    markerTitle = it.title.orEmpty(),
                    markerDescription = "description",
                    markerPosition = it.getPosition()!!
                )
            }
    }

    private fun loadJsonFromAsset(context: Context): String? {
        return try {
            val input = context.assets.open(MARKERS)
            val size = input.available()
            val buffer = ByteArray(size)
            input.use { it.read(buffer) }
            input.close()
            String(buffer)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            null
        }
    }

    companion object {
        private const val MARKERS = "pn_map_response.json"
    }
}
