package ru.dgis.sdk.demo.compose

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.TextHorizontalAlignment

class MarkerViewModel(private val marker: Marker) : ViewModel() {
    private var _text = MutableStateFlow(TextFieldValue(marker.text))
    val text = _text.asStateFlow()

    fun setText(text: TextFieldValue) {
        marker.text = text.text
        _text.value = text
    }

    private var _textStyle = MutableStateFlow(marker.textStyle)
    val textStyle = _textStyle.asStateFlow()

    fun setHorizontalAlignment(alignment: TextHorizontalAlignment) {
        _textStyle.value = _textStyle.value.copy(textHorizontalAlignment = alignment)
        marker.textStyle = _textStyle.value
    }

    fun setFontSize(size: LogicalPixel) {
        _textStyle.value = _textStyle.value.copy(fontSize = size)
        marker.textStyle = _textStyle.value
    }

    fun setStrokeWidth(width: LogicalPixel) {
        _textStyle.value = _textStyle.value.copy(strokeWidth = width)
        marker.textStyle = _textStyle.value
    }

    fun setOffset(offset: LogicalPixel) {
        _textStyle.value = _textStyle.value.copy(textOffset = offset)
        marker.textStyle = _textStyle.value
    }
}
