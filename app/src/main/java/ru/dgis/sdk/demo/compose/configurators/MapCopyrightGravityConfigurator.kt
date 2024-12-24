package ru.dgis.sdk.demo.compose.configurators

import android.view.Gravity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.demo.compose.Displayable
import ru.dgis.sdk.demo.compose.EnumToggle

private fun CopyrightGravity.toMapCopyrightGravity(): Int {
    return when (this) {
        CopyrightGravity.TOP_LEFT -> Gravity.TOP or Gravity.START
        CopyrightGravity.TOP_RIGHT -> Gravity.TOP or Gravity.END
        CopyrightGravity.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
        CopyrightGravity.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
    }
}

private enum class CopyrightGravity(override val displayName: String) : Displayable {
    TOP_LEFT("\u2196"),
    TOP_RIGHT("\u2197"),
    BOTTOM_LEFT("\u2199"),
    BOTTOM_RIGHT("\u2198")
}

@Composable
fun MapCopyrightGravityConfigurator(
    gravity: Int,
    onGravityChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        enumValues<CopyrightGravity>().forEach { option ->
            EnumToggle(
                value = option,
                isSelected = gravity == option.toMapCopyrightGravity(),
                onSelected = {
                    onGravityChange(it.toMapCopyrightGravity())
                }
            )
        }
    }
}
