package ru.dgis.sdk.demo.compose.examples.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ru.dgis.sdk.demo.compose.components.Displayable
import ru.dgis.sdk.demo.compose.components.EnumToggle
import ru.dgis.sdk.map.Fixed
import ru.dgis.sdk.map.MapAppearance
import ru.dgis.sdk.map.MapTheme

private enum class Appearance(override val displayName: String) : Displayable {
    AUTO("Auto"),
    LIGHT("Light"),
    DARK("Dark"),
}

private fun Appearance.toMapAppearance(): MapAppearance {
    return when (this) {
        Appearance.AUTO -> MapAppearance.defaultAppearance()
        Appearance.LIGHT -> MapAppearance(Fixed(MapTheme.defaultTheme))
        Appearance.DARK -> MapAppearance(Fixed(MapTheme.defaultDarkTheme))
    }
}

@Composable
fun MapAppearanceConfigurator(
    mapAppearance: MapAppearance,
    onMapAppearanceChange: (MapAppearance) -> Unit
) {
    Column {
        enumValues<Appearance>().forEach { option ->
            EnumToggle(
                value = option,
                isSelected = option.toMapAppearance() == mapAppearance,
                onSelected = {
                    onMapAppearanceChange(it.toMapAppearance())
                },
            )
        }
    }
}
