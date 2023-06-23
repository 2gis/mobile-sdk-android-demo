package ru.dgis.sdk.demo

import android.view.View
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
