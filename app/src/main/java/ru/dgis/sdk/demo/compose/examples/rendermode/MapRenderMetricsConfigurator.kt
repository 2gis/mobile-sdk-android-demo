package ru.dgis.sdk.demo.compose.examples.rendermode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.dgis.sdk.map.DeviceDensity
import ru.dgis.sdk.map.DevicePpi
import java.util.Locale

@Composable
private fun NullableFloatSlider(
    caption: String,
    value: Float?,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float?) -> Unit,
    format: (Float) -> String = { String.format(Locale.US, "%.1f", it) },
) {
    // Keeps the last value picked by the user, whatever the nullable value is.
    var sliderValue by remember { mutableFloatStateOf(value ?: minValue) }

    // A new non-null value from outside moves the slider.
    LaunchedEffect(value) {
        if (value != null) {
            sliderValue = value
        }
    }

    Column {
        Row {
            Checkbox(
                checked = value != null,
                onCheckedChange = { checked ->
                    onValueChange(if (checked) sliderValue else null)
                },
            )
            Text(text = "$caption: ${value?.let(format) ?: "null"}")
        }
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                if (value != null) {
                    onValueChange(newValue)
                }
            },
            valueRange = minValue..maxValue,
            enabled = value != null,
        )
    }
}

@Composable
fun MapRenderMetricsConfigurator(
    devicePpi: DevicePpi?,
    onDevicePpiChange: (DevicePpi?) -> Unit,
    deviceDensity: DeviceDensity?,
    onDeviceDensityChange: (DeviceDensity?) -> Unit,
) {
    Column {
        NullableFloatSlider(
            caption = "DPI",
            value = devicePpi?.value,
            minValue = 50f,
            maxValue = 1000f,
            onValueChange = { onDevicePpiChange(it?.let(::DevicePpi)) },
            format = { String.format(Locale.US, "%.0f", it) },
        )
        NullableFloatSlider(
            caption = "Density",
            value = deviceDensity?.value,
            minValue = 0.2f,
            maxValue = 10.0f,
            onValueChange = { onDeviceDensityChange(it?.let(::DeviceDensity)) },
        )
    }
}
