package ru.dgis.sdk.demo.common

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.common.views.SettingsLayoutView
import ru.dgis.sdk.map.MapView

val View.globalY
    get(): Int {
        val position = IntArray(2)
        getLocationOnScreen(position)
        return position[1]
    }

fun MapView.updateMapCopyrightPosition(rootView: View, bottomSheet: View) {
    setCopyrightMargins(0, 0, 0, rootView.height + rootView.globalY - bottomSheet.globalY)
}

/**
 * Provides generic BottomSheet to layout. Should be used as parent layout for some useful view / layout, such as settings.
 * Takes lambda as a parameter, in which child layout should be added
 * @param init lambda which configures BottomSheet, adding child layout, etc
 */
fun ViewBinding.addSettingsLayout(init: ViewGroup.() -> Unit): SettingsLayoutView {
    val settingsView = SettingsLayoutView(root.context)
    val innerLayoutView = settingsView.findViewById<LinearLayout>(R.id.settingsDrawerInnerLayout)
    (root as ViewGroup).addView(settingsView)

    BottomSheetBehavior.from(innerLayoutView).apply {
        state = BottomSheetBehavior.STATE_COLLAPSED
        addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                (root as ViewGroup).mapView?.updateMapCopyrightPosition(root, innerLayoutView)
            }
        })
    }
    innerLayoutView.init()
    return settingsView
}

// Helper method for search mapView in hierarchy.
private val ViewGroup.mapView: MapView?
    get() = children.find { it is MapView } as? MapView
