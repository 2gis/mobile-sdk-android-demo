package ru.dgis.sdk.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.dgis.sdk.demo.common.addSettingsLayout
import ru.dgis.sdk.demo.databinding.ActivityGeoJsonBinding
import ru.dgis.sdk.demo.databinding.ActivityGeoJsonSettingsBinding
import ru.dgis.sdk.map.GeometryMapObject
import ru.dgis.sdk.map.GeometryMapObjectSourceBuilder
import ru.dgis.sdk.map.parseGeoJson

enum class Identifiers(val value: String) {
    MULTI_POLYGON("multipolygon"),
    POLYGON("polygon"),
    POLYLINE("polyline");

    val filename: String
        get() = "$value.json"
}

/**
 * Sample activity to demonstrate example of work with GeoJSON.
 * There is only a subset of GeoJSON objects which SDK supports, see *.json files in src/main/assets.
 *
 * Please, pay attention to properties object inside each file. There are custom properties you need to implement
 * for parsing / displaying to work. Set of needed properties depends on GeoJSON object type.
 */
class GeoJsonActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGeoJsonBinding.inflate(layoutInflater) }
    private val settingsBinding by lazy { ActivityGeoJsonSettingsBinding.inflate(layoutInflater) }

    private val sdkContext by lazy { application.sdkContext }
    private val geometrySource by lazy { GeometryMapObjectSourceBuilder(sdkContext).createSource() }

    private val polygonGeoJson = suspendLazy {
        readAndSaveGeoJson(
            Identifiers.POLYGON.filename
        )
    }
    private val multiPolygonGeoJson = suspendLazy {
        readAndSaveGeoJson(
            Identifiers.MULTI_POLYGON.filename
        )
    }
    private val polylineGeoJson = suspendLazy {
        readAndSaveGeoJson(
            Identifiers.POLYLINE.filename
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.addSettingsLayout().apply {
            settingsDrawerInnerLayout.addView(settingsBinding.root)
        }

        binding.mapView.getMapAsync {
            it.addSource(geometrySource)
        }

        initSettings()
    }

    private fun addGeoJsonToSource(identifier: Identifiers?) {
        geometrySource.clear()

        if (identifier != null) {
            val objects = when (identifier) {
                Identifiers.POLYLINE -> polylineGeoJson
                Identifiers.MULTI_POLYGON -> multiPolygonGeoJson
                Identifiers.POLYGON -> polygonGeoJson
            }

            lifecycleScope.launch {
                objects().apply {
                    geometrySource.addObjects(this)
                }
            }
        }
    }

    private fun initSettings() {
        val switches = listOf(
            settingsBinding.multiPolygonSwitch,
            settingsBinding.polygonSwitch,
            settingsBinding.polylineSwitch
        )

        switches.forEach { switch ->
            switch.isEnabled = true
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    addGeoJsonToSource(Identifiers.entries.find { it.value == switch.tag })
                }
            }
        }

        settingsBinding.polylineSwitch.isChecked = true
    }

    /**
     * Main logic is here. SDK parses GeoJSON and converts objects to list of native [GeometryMapObject](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GeometryMapObject).
     * After conversion it is possible to add these objects to [GeometryMapObjectSource](https://docs.2gis.com/en/android/sdk/reference/7.0/ru.dgis.sdk.map.GeometryMapObjectSource)
     * See [addGeoJsonToSource] for further details on adding objects to source
     */
    private suspend fun readAndSaveGeoJson(filename: String): List<GeometryMapObject> {
        var objects: List<GeometryMapObject>
        withContext(Dispatchers.IO) {
            val geoJsonString = assets.open(filename).bufferedReader().use { it.readText() }
            objects = parseGeoJson(geoJsonString)
        }
        Log.w("FILES", "Reading file $filename")
        return objects
    }
}

// It is impossible to use lazy delegate with suspend function, because there is no suspend properties in Kotlin.
// We are introducing custom function to overcome this.
// For details see: https://kt.academy/article/cc-recipes
@Suppress("UNCHECKED_CAST")
fun <T> suspendLazy(
    initializer: suspend () -> T
): suspend () -> T {
    var initializerInner: (suspend () -> T)? = initializer
    var holder: Any? = Any()

    return {
        if (initializerInner == null) { holder as T }
        initializerInner?.let {
            holder = it()
            initializerInner = null
        }
        holder as T
    }
}
