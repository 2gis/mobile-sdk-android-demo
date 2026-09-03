package ru.dgis.sdk.demo.common

import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.first
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.sdkContext
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.DefaultMapControllerViewModel
import ru.dgis.sdk.map.DgisSource
import ru.dgis.sdk.map.DgisSourceWorkingMode
import ru.dgis.sdk.map.MapController
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MapControllerState
import ru.dgis.sdk.map.MapControllerViewModel
import ru.dgis.sdk.map.MapCopyrightOptions
import ru.dgis.sdk.map.MapRenderOptions
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Source

/** Owns a map controller for the lifetime of an AndroidX ViewModelStore. */
class DemoMapOwner(
    private val sdkContext: Context,
    options: MapControllerOptions
) : ViewModel() {
    var mapViewModel = DefaultMapControllerViewModel(sdkContext, options)
        private set

    fun replace(options: MapControllerOptions): MapControllerViewModel {
        mapViewModel.close()
        return DefaultMapControllerViewModel(sdkContext, options).also {
            mapViewModel = it
        }
    }

    override fun onCleared() = mapViewModel.close()

    companion object {
        fun factory(
            sdkContext: Context,
            options: MapControllerOptions
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(DemoMapOwner::class.java))
                return DemoMapOwner(sdkContext, options) as T
            }
        }
    }
}

/**
 * Waits for map creation without propagating [MapControllerState.Error] to an Activity coroutine.
 * Returns `null` after displaying the creation error, so the caller can skip example setup.
 */
suspend fun AppCompatActivity.awaitMapControllerOrShowError(
    viewModel: MapControllerViewModel
): MapController? = when (val state = viewModel.state.first { it !is MapControllerState.Creating }) {
    is MapControllerState.Created -> state.controller
    is MapControllerState.Error -> {
        Log.e(javaClass.simpleName, "Map controller creation failed", state.cause)
        if (!isFinishing && !isDestroyed) {
            AlertDialog.Builder(this)
                .setTitle(R.string.map_creation_error_title)
                .setMessage(state.cause.localizedMessage ?: state.cause.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        null
    }
    MapControllerState.Closed -> null
    is MapControllerState.Creating -> null
}

fun ComponentActivity.demoMapOwner(
    position: CameraPosition? = null
): Lazy<DemoMapOwner> = demoMapOwner { sdkContext ->
    demoMapControllerOptions(sdkContext, position)
}

fun ComponentActivity.demoMapOwner(
    optionsFactory: (Context) -> MapControllerOptions
): Lazy<DemoMapOwner> = viewModels {
    val sdkContext = application.sdkContext
    DemoMapOwner.factory(sdkContext, optionsFactory(sdkContext))
}

fun createDgisSources(
    sdkContext: Context,
    workingMode: DgisSourceWorkingMode = DgisSourceWorkingMode.HYBRID_ONLINE_FIRST
): List<Source> = listOf(
    DgisSource.createDgisSource(sdkContext, workingMode),
    DgisSource.createImmersiveDgisSource(sdkContext)
)

fun demoMapControllerOptions(
    sdkContext: Context,
    position: CameraPosition? = null
): MapControllerOptions = MapControllerOptions(
    position = position,
    sources = createDgisSources(sdkContext)
)

fun ViewGroup.attachMapView(
    viewModel: MapControllerViewModel,
    renderOptions: MapRenderOptions = MapRenderOptions(),
    copyrightOptions: MapCopyrightOptions = MapCopyrightOptions()
): MapView = MapView(context, renderOptions, copyrightOptions).also { mapView ->
    mapView.setMapControllerViewModel(viewModel)
    addView(
        mapView,
        0,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
}
