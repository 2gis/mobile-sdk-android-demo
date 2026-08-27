package ru.dgis.sdk.demo.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java9.util.concurrent.CompletableFuture
import ru.dgis.sdk.DGis
import ru.dgis.sdk.demo.common.createDgisSources
import ru.dgis.sdk.map.DefaultMapControllerViewModel
import ru.dgis.sdk.map.MapControllerOptions
import ru.dgis.sdk.map.MapControllerViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import ru.dgis.sdk.File as DGisFile

class MapStyleViewModel : ViewModel() {
    private var loadingFuture = CompletableFuture<Void>()
    private var stylePath = ""
    private var controllerStylePath = ""
    private var mapControllerViewModel: DefaultMapControllerViewModel? = null
    private val _styleFile = MutableLiveData<DGisFile>()

    var isStyleSelected: Boolean = false
        private set

    val styleFile: LiveData<DGisFile>
        get() = _styleFile

    fun loadStyle(styleStream: InputStream) {
        isStyleSelected = true

        loadingFuture = CompletableFuture
            .supplyAsync {
                val destinationFile = File.createTempFile("style-", ".2gis")
                styleStream.use { inStream ->
                    FileOutputStream(destinationFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                destinationFile.absolutePath
            }
            .thenAccept {
                this.stylePath = it
                _styleFile.postValue(DGisFile(it))
            }
    }

    fun mapViewModel(styleFile: DGisFile): MapControllerViewModel {
        if (mapControllerViewModel == null || controllerStylePath != stylePath) {
            mapControllerViewModel?.close()
            controllerStylePath = stylePath
            val sdkContext = DGis.context()
            mapControllerViewModel = DefaultMapControllerViewModel(
                sdkContext,
                MapControllerOptions(
                    sources = createDgisSources(sdkContext),
                    styleFile = styleFile
                )
            )
        }
        return checkNotNull(mapControllerViewModel)
    }

    override fun onCleared() {
        super.onCleared()
        loadingFuture.cancel(true)

        mapControllerViewModel?.close()

        if (stylePath.isNotEmpty()) {
            CompletableFuture.runAsync {
                File(stylePath).delete()
            }
        }
    }
}
