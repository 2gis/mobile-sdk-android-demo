package ru.dgis.sdk.demo.compose.examples.navigation.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun IntTextField(
    label: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    onClearClicked: () -> Unit,
    unit: String? = null,
    supportingText: String? = null,
) {
    val textValue = if (value == 0) "" else value.toString()

    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = textValue,
        onValueChange = { newText ->
            if (newText.all { it.isDigit() }) {
                onValueChanged(newText.toIntOrNull() ?: 0)
            }
        },
        label = { Text(text = label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text(text = "0") },
        suffix = unit?.let { { Text(text = it) } },
        supportingText = supportingText?.let { { Text(text = it) } },
        trailingIcon = {
            if (textValue.isNotEmpty()) {
                IconButton(onClick = onClearClicked) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        ),
    )
}


@Composable
@Preview(showBackground = true)
private fun IntTextFieldShortLabelWithClearPreview() =
    IntTextField("Lorem ipsum dolor sit amet", 10, {}, {})

@Composable
@Preview(showBackground = true)
private fun IntTextFieldLongLabelWithClearPreview() =
    IntTextField(
        label = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        value = 100,
        onValueChanged = {},
        onClearClicked = {},
        unit = "m",
        supportingText = "Helper text",
    )
