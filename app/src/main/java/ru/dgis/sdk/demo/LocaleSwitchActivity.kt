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
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.DgisSource
import ru.dgis.sdk.map.MapControllerOptions
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
    private val mapOwner by demoMapOwner { createMapControllerOptions() }
    private lateinit var mapView: MapView
    private lateinit var mapContainer: LinearLayout
    private var ignoreInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locale_switch)

        if (savedInstanceState == null) {
            LocaleManager.instance(sdkContext).overrideLocales(emptyList())
        }
        mapContainer = findViewById(R.id.map_container)
        mapView = mapContainer.attachMapView(mapOwner.mapViewModel)
        initSpinner(findViewById(R.id.spinnerLocale), R.array.locales_list, this::onLocaleItemSelected)
    }

    /**
     * Handles selection changes in the locale dropdown. Updates the locale settings in the
     * DGis SDK and recreates the map to apply the changes.
     *
     * @param item The selected locale as a string.
     */
    private fun onLocaleItemSelected(item: String) {
        if (ignoreInitialSelection) {
            ignoreInitialSelection = false
            return
        }
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
        mapContainer.removeAllViews()
        mapView = mapContainer.attachMapView(mapOwner.replace(createMapControllerOptions()))
    }

    private fun createMapControllerOptions() = MapControllerOptions(
        position = CameraPosition(
            GeoPoint(40.37741938, 49.87862621),
            Zoom(9f)
        ),
        sources = listOf(
            DgisSource.createDgisSource(sdkContext),
            MyLocationMapObjectSource(
                sdkContext,
                MyLocationControllerSettings(BearingSource.MAGNETIC)
            )
        )
    )

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
