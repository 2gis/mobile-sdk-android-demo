package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import ru.dgis.sdk.Context
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.DgisSource
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.MyLocationControllerSettings
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.platform.Locale
import ru.dgis.sdk.platform.LocaleManager

/**
 * Activity that demonstrates how to dynamically switch map locales in a DGis-based application.
 * The locale changes are applied through a dropdown menu, and the map is recreated to reflect the
 * changes.
 */
class LocaleSwitchActivity : AppCompatActivity() {
    private val sdkContext: Context by lazy { application.sdkContext }
    private lateinit var mapView: MapView
    private lateinit var mapContainer: LinearLayout
    private var map: Map? = null
    private lateinit var mapSource: MyLocationMapObjectSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locale_switch)

        mapContainer = findViewById(R.id.map_container)
        initSpinner(findViewById(R.id.spinnerLocale), R.array.locales_list, this::onLocaleItemSelected)
        recreateMap()
    }

    /**
     * Handles selection changes in the locale dropdown. Updates the locale settings in the
     * DGis SDK and recreates the map to apply the changes.
     *
     * @param item The selected locale as a string.
     */
    private fun onLocaleItemSelected(item: String) {
        if (item == "System") {
            LocaleManager.instance(sdkContext).overrideLocales(emptyList())
        } else {
            Locale.makeLocale(item)?.let {
                LocaleManager.instance(sdkContext).overrideLocales(listOf(it))
            }
        }
    }

    /**
     * Recreates the map to reflect any changes in settings (e.g., locale). The current map view
     * is removed, and a new one is initialized with the updated options.
     */
    private fun recreateMap() {
        map?.let { lifecycle.removeObserver(mapView) }
        mapContainer.removeAllViews()

        val mapOptions = MapOptions().apply {
            position = CameraPosition(
                GeoPoint(40.37741938, 49.87862621),
                Zoom(9.0f)
            )
            sources = listOf(DgisSource.createDgisSource(sdkContext))
        }

        mapView = MapView(this, mapOptions).also {
            it.getMapAsync { map ->
                this.map = map
                mapSource = MyLocationMapObjectSource(
                    sdkContext,
                    MyLocationControllerSettings(BearingSource.MAGNETIC)
                )
                map.addSource(mapSource)
            }
            mapContainer.addView(it)
            lifecycle.addObserver(it)
        }
    }

    /**
     * Initializes a dropdown spinner with a list of items and a callback for selection changes.
     *
     * @param onItemSelected Callback invoked when an item is selected.
     */
    private fun initSpinner(spinner: Spinner, itemsResource: Int, onItemSelected: (String) -> Unit) {
        ArrayAdapter.createFromResource(this, itemsResource, R.layout.spinner_item).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                onItemSelected(parent?.getItemAtPosition(pos)?.toString() ?: "")
            }
        }
    }
}
