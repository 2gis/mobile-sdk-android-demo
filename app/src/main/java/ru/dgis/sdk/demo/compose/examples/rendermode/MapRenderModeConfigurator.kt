package ru.dgis.sdk.demo.compose.examples.rendermode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.demo.compose.components.Displayable
import ru.dgis.sdk.demo.compose.components.EnumToggle
import ru.dgis.sdk.map.MapRenderMode

private enum class RenderModeDisplay(override val displayName: String) : Displayable {
    SURFACE("SurfaceView (Performance)"),
    TEXTURE("TextureView (Compatibility)")
}

private fun RenderModeDisplay.toMapRenderMode(): MapRenderMode {
    return when (this) {
        RenderModeDisplay.SURFACE -> MapRenderMode.SURFACE
        RenderModeDisplay.TEXTURE -> MapRenderMode.TEXTURE
    }
}

@Composable
fun MapRenderModeConfigurator(
    renderMode: MapRenderMode,
    onRenderModeChange: (MapRenderMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Render Mode (changes cause brief blank frame)")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            enumValues<RenderModeDisplay>().forEach { option ->
                EnumToggle(
                    value = option,
                    isSelected = renderMode == option.toMapRenderMode(),
                    onSelected = {
                        onRenderModeChange(it.toMapRenderMode())
                    }
                )
            }
        }
    }
}
