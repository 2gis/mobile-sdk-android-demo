package ru.dgis.sdk.demo.compose.configurators

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.demo.compose.CaptionSlider
import ru.dgis.sdk.demo.compose.EnumToggle
import ru.dgis.sdk.demo.compose.MarkerViewModel
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.TextHorizontalAlignment

@Composable
fun MarkerConfigurator(markerViewModel: MarkerViewModel, modifier: Modifier = Modifier) {
    val text by markerViewModel.text.collectAsState()
    val textStyle by markerViewModel.textStyle.collectAsState()

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            CaptionSlider(
                caption = "Font Size",
                value = textStyle.fontSize.value.toInt(),
                onValueChange = {
                    markerViewModel.setFontSize(LogicalPixel(it.toFloat()))
                }
            )

            CaptionSlider(
                caption = "Text Offset",
                value = textStyle.textOffset.value.toInt(),
                onValueChange = {
                    markerViewModel.setOffset(LogicalPixel(it.toFloat()))
                }
            )

            CaptionSlider(
                caption = "Stroke Width",
                value = textStyle.strokeWidth.value.toInt(),
                onValueChange = {
                    markerViewModel.setStrokeWidth(LogicalPixel(it.toFloat()))
                }
            )

            OutlinedTextField(
                value = text,
                onValueChange = { markerViewModel.setText(it) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                singleLine = false
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                enumValues<TextHorizontalAlignment>().forEach { option ->
                    EnumToggle(
                        value = option,
                        isSelected = option == textStyle.textHorizontalAlignment,
                        onSelected = {
                            markerViewModel.setHorizontalAlignment(it)
                        }
                    )
                }
            }
        }
    }
}
