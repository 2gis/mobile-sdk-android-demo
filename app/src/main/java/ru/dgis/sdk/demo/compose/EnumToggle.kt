package ru.dgis.sdk.demo.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

interface Displayable {
    val displayName: String
}

val <T> T.displayName: String
    get() = if (this is Displayable) this.displayName else this.toString()

@Composable
inline fun <reified T : Enum<T>> EnumToggle(
    value: T,
    isSelected: Boolean,
    crossinline onSelected: (T) -> Unit
) {
    Button(
        onClick = { onSelected(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.LightGray
            },
            contentColor = if (isSelected) {
                Color.White
            } else {
                Color.Black
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = value.displayName)
    }
}
