package ru.dgis.sdk.demo.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import ru.dgis.sdk.demo.R

class SettingsLayoutView(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CoordinatorLayout(context, attrs, defStyle) {

    val settingsDrawerInnerLayout: LinearLayout
    init {
        inflate(context, R.layout.bottom_settings_layout, this)
        settingsDrawerInnerLayout = findViewById(R.id.settingsDrawerInnerLayout)
    }
}
