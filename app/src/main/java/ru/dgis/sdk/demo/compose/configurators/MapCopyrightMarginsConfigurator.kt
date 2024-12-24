package ru.dgis.sdk.demo.compose.configurators

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ru.dgis.sdk.compose.map.CopyrightMargins
import ru.dgis.sdk.demo.compose.CaptionSlider

@Composable
fun MapCopyrightMarginsConfigurator(
    margins: CopyrightMargins,
    onMarginsChange: ((CopyrightMargins) -> Unit)? = null
) {
    Column {
        CaptionSlider("Left", margins.left) { newLeft ->
            onMarginsChange?.invoke(margins.copy(left = newLeft))
        }
        CaptionSlider("Top", margins.top) { newTop ->
            onMarginsChange?.invoke(margins.copy(top = newTop))
        }
        CaptionSlider("Right", margins.right) { newRight ->
            onMarginsChange?.invoke(margins.copy(right = newRight))
        }
        CaptionSlider("Bottom", margins.bottom) { newBottom ->
            onMarginsChange?.invoke(margins.copy(bottom = newBottom))
        }
    }
}
