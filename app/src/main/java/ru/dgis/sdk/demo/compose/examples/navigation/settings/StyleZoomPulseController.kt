package ru.dgis.sdk.demo.compose.examples.navigation.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.dgis.sdk.map.StyleZoom

/**
 * Starts/stops a coroutine that periodically updates [StyleZoom] using the provided [setZoom] callback.
 *
 * Designed to keep ViewModels clean: they only call [start] / [stop].
 */
internal class StyleZoomPulseController(
    private val scope: CoroutineScope,
    private val setZoom: (StyleZoom) -> Unit,
    private val delayMs: Long = 300L,
    private val minZoom: Float = 16.5f,
    private val maxZoom: Float = 19.5f,
    private val step: Float = 0.35f
) {
    private var job: Job? = null

    fun start() {
        if (job != null) return

        job = scope.launch {
            var direction = 1
            var value = minZoom
            while (true) {
                setZoom(StyleZoom(value))
                delay(delayMs)

                value += direction * step
                if (value >= maxZoom) direction = -1
                if (value <= minZoom) direction = 1
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
