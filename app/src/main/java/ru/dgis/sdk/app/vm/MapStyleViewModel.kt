package ru.dgis.sdk.app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java9.util.concurrent.CompletableFuture
import ru.dgis.sdk.DGis
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.Style
import ru.dgis.sdk.map.StyleBuilder
import java.io.File
import java.lang.RuntimeException


class MapStyleViewModel: ViewModel() {
    private val closeables = mutableListOf<AutoCloseable>()
    private var loadingFuture = CompletableFuture<Void>()
    private var stylePath = ""
    private val styleData = MutableLiveData<Style>()
    private var map: Map? = null

    var isStyleSelected: Boolean = false
        private set

    val style: LiveData<Style>
        get() = styleData

    fun loadStyle(styleFuture: CompletableFuture<String>) {
        isStyleSelected = true
        loadingFuture = styleFuture
            .thenComposeAsync { stylePath ->
                this.stylePath = stylePath

                val future = CompletableFuture<Style>()
                val sdkContext = checkNotNull(DGis.context())

                StyleBuilder(sdkContext)
                    .loadStyleFromFile(stylePath)
                    .onComplete({ style ->
                        if (style == null) {
                            val msg = "Creation style from $stylePath is failed"
                            future.completeExceptionally(RuntimeException(msg))
                        } else {
                            future.complete(style)
                        }
                    }, future::completeExceptionally)

                future
            }
            .thenAccept(styleData::setValue)
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
