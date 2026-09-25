package ru.dgis.sdk.demo.compose.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    modifier: Modifier = Modifier,
    crossinline onSelected: (T) -> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onSelected(value) },
        colors = if (isSelected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        },
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = value.displayName,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}