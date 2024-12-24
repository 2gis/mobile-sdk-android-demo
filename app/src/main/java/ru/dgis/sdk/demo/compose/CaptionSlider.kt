package ru.dgis.sdk.demo.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CaptionSlider(caption: String, value: Int, onValueChange: (Int) -> Unit) {
    Column {
        Text(text = "$caption: $value")
        androidx.compose.material3.Slider(
            value = value.toFloat(),
            onValueChange = { newValue ->
                onValueChange(newValue.toInt())
            },
            valueRange = 0f..100f,
            steps = 100
        )
    }
}
