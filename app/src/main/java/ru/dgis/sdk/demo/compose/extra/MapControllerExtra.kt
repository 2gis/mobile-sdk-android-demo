package ru.dgis.sdk.demo.compose.extra

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.dgis.sdk.map.MapController
import ru.dgis.sdk.map.TouchEventsObserver

/**
 * Delivers gesture recognizer channel events to [observer] for as long as the calling
 * coroutine is alive.
 */
suspend fun MapController.collectTouchEvents(observer: TouchEventsObserver): Unit = coroutineScope {
    with(gestureRecognizer) {
        launch { tap.asFlow().collect { observer.onTap(it) } }
        launch { longTouch.asFlow().collect { observer.onLongTouch(it) } }
        launch { dragBegin.asFlow().collect { observer.onDragBegin(it) } }
        launch { dragMove.asFlow().collect { observer.onDragMove(it) } }
        launch { dragEnd.asFlow().collect { observer.onDragEnd() } }
    }
}
