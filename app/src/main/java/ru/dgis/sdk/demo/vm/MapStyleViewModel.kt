package ru.dgis.sdk.demo.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.util.IOUtils
import java9.util.concurrent.CompletableFuture
import ru.dgis.sdk.context.File as DGisFile
import ru.dgis.sdk.map.Map
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MapStyleViewModel: ViewModel() {
    private val closeables = mutableListOf<AutoCloseable>()
    private var loadingFuture = CompletableFuture<Void>()
    private var stylePath = ""
    private val _styleFile = MutableLiveData<DGisFile>()
    private var map: Map? = null

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
                        IOUtils.copyStream(inStream, outStream)
                    }
                }
                destinationFile.absolutePath
            }
            .thenAccept {
                this.stylePath = it
                _styleFile.postValue(DGisFile(it))
            }
    }

    fun onMapReady(map: Map) {
        this.map = map
        closeables.add(map)
    }

    override fun onCleared() {
        super.onCleared()
        loadingFuture.cancel(true)

        closeables.forEach(AutoCloseable::close)
        closeables.clear()

        if (stylePath.isNotEmpty()) {
            CompletableFuture.runAsync {
                File(stylePath).delete()
            }
        }
    }
}
