package ru.dgis.sdk.demo.common

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.common.views.SettingsLayoutView
import ru.dgis.sdk.map.CopyrightMargins
import ru.dgis.sdk.map.MapControl
import ru.dgis.sdk.map.MapController
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.Channel as SdkChannel

val View.globalY
    get(): Int {
        val position = IntArray(2)
        getLocationOnScreen(position)
        return position[1]
    }

fun MapView.updateMapCopyrightPosition(rootView: View, bottomSheet: View) {
    copyrightOptions = copyrightOptions.copy(
        margins = CopyrightMargins(
            bottom = rootView.height + rootView.globalY - bottomSheet.globalY
        )
    )
}

/**
 * Provides generic BottomSheet to layout. Should be used as parent layout for some useful view / layout, such as settings.
 * Takes lambda as a parameter, in which child layout should be added
 * @param mapView map whose copyright position should follow the BottomSheet
 * @param init lambda which configures BottomSheet, adding child layout, etc
 */
fun ViewBinding.addSettingsLayout(mapView: MapView, init: ViewGroup.() -> Unit): SettingsLayoutView {
    val settingsView = SettingsLayoutView(root.context)
    val innerLayoutView = settingsView.findViewById<LinearLayout>(R.id.settingsDrawerInnerLayout)
    (root as ViewGroup).addView(settingsView)

    BottomSheetBehavior.from(innerLayoutView).apply {
        state = BottomSheetBehavior.STATE_COLLAPSED
        addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                mapView.updateMapCopyrightPosition(root, innerLayoutView)
            }
        })
    }
    innerLayoutView.init()
    return settingsView
}

fun <T : Any?> SdkChannel<T>.asFlow(): Flow<T> = callbackFlow {
    val connection = connect { value -> trySend(value) }
    awaitClose { connection.close() }
}.buffer(Channel.CONFLATED)

/** Delivers controller gesture events while the calling coroutine is active. */
suspend fun MapController.collectTouchEvents(observer: TouchEventsObserver): Unit = coroutineScope {
    with(gestureRecognizer) {
        launch { tap.asFlow().collect(observer::onTap) }
        launch { longTouch.asFlow().collect(observer::onLongTouch) }
        launch { dragBegin.asFlow().collect(observer::onDragBegin) }
        launch { dragMove.asFlow().collect(observer::onDragMove) }
        launch { dragEnd.asFlow().collect { observer.onDragEnd() } }
    }
}

/** Binds both a single map control and controls nested in a container. */
fun View.bindMapControls(controller: MapController, mapView: MapView) {
    if (this is MapControl) bindToMap(controller, mapView)
    if (this is ViewGroup) children.forEach { it.bindMapControls(controller, mapView) }
}
