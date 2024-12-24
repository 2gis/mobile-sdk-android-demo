package ru.dgis.sdk.demo.compose.configurators

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ru.dgis.sdk.demo.compose.Displayable
import ru.dgis.sdk.demo.compose.EnumToggle
import ru.dgis.sdk.map.MapTheme

private enum class Theme(override val displayName: String) : Displayable {
    LIGHT("Light"),
    DARK("Dark")
}

private fun Theme.toMapTheme(): MapTheme {
    return when (this) {
        Theme.LIGHT -> MapTheme.defaultTheme
        Theme.DARK -> MapTheme.defaultDarkTheme
    }
}

@Composable
fun MapThemeConfigurator(
    theme: MapTheme,
    onThemeChange: (MapTheme) -> Unit
) {
    Column {
        enumValues<Theme>().forEach { option ->
            EnumToggle(
                value = option,
                isSelected = option.toMapTheme() == theme,
                onSelected = {
                    onThemeChange(it.toMapTheme())
                }
            )
        }
    }
}
