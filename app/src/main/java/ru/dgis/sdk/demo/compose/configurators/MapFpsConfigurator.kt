package ru.dgis.sdk.demo.compose.configurators

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.dgis.sdk.demo.compose.CaptionSlider
import ru.dgis.sdk.map.Fps

@Composable
private fun MapFpsSlider(label: String, value: Fps?, onValueChange: (Fps?) -> Unit) {
    var sliderValue by remember { mutableIntStateOf(value?.value ?: 0) }

    Row {
        Checkbox(
            checked = value != null,
            onCheckedChange = { onValueChange(if (it) Fps(sliderValue) else null) }
        )

        CaptionSlider(
            caption = label,
            value = sliderValue,
            onValueChange = {
                sliderValue = it
                if (value != null) {
                    onValueChange(Fps(it))
                }
            }
        )
    }
}

@Composable
fun MapFpsConfigurator(
    maxFpsValue: Fps?,
    onMaxFpsChange: (Fps?) -> Unit,
    powerSavingMaxFpsValue: Fps?,
    onPowerSavingMaxFpsChange: (Fps?) -> Unit
) {
    Column {
        MapFpsSlider(
            label = "MapFps",
            value = maxFpsValue,
            onValueChange = onMaxFpsChange
        )

        MapFpsSlider(
            label = "PowerSavingMapFps",
            value = powerSavingMaxFpsValue,
            onValueChange = onPowerSavingMaxFpsChange
        )
    }
}
