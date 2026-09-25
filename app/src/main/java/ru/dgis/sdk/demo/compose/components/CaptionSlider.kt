package ru.dgis.sdk.demo.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CaptionSlider(caption: String, value: Int, onValueChange: (Int) -> Unit) {
    CaptionSlider(
        caption = caption,
        value = value,
        minValue = 0,
        maxValue = 100,
        steps = 100,
        onValueChange = onValueChange
    )
}

@Composable
fun CaptionSlider(
    caption: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    steps: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Text(text = "$caption: $value")
        androidx.compose.material3.Slider(
            value = value.toFloat(),
            onValueChange = { newValue ->
                onValueChange(newValue.toInt())
            },
            valueRange = minValue.toFloat()..maxValue.toFloat(),
            steps = steps - 1
        )
    }
}
