package ru.dgis.sdk.demo.compose.examples.navigation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Free roam (prefetch/cache) settings screen.
 *
 * This composable renders cache-related parameters that affect tile prefetching and perceived
 * smoothness when moving on/off route.
 *
 * It is a pure UI component:
 * - receives current values via [settings]
 * - emits updates via the provided callbacks
 */
@Composable
fun FreeRoamSettingsComposable(
    settings: ComposeNavigationSettingsState,
    onSetCacheDistanceOnRouteM: (Int) -> Unit,
    onClearCacheDistanceOnRouteM: () -> Unit,
    onSetCacheRadiusOnRouteM: (Int) -> Unit,
    onClearCacheRadiusOnRouteM: () -> Unit,
    onSetCacheRadiusM: (Int) -> Unit,
    onClearCacheRadiusM: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Freeroam",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Cache settings that affect offline tile prefetching",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IntTextField(
            label = "Cache distance on route",
            value = settings.freeRoamCacheDistanceOnRouteM,
            onValueChanged = { onSetCacheDistanceOnRouteM(it.coerceAtLeast(0)) },
            onClearClicked = onClearCacheDistanceOnRouteM,
            unit = "m"
        )
        IntTextField(
            label = "Cache radius on route",
            value = settings.freeRoamCacheRadiusOnRouteM,
            onValueChanged = { onSetCacheRadiusOnRouteM(it.coerceAtLeast(0)) },
            onClearClicked = onClearCacheRadiusOnRouteM,
            unit = "m"
        )
        IntTextField(
            label = "Cache radius",
            value = settings.freeRoamCacheRadiusM,
            onValueChanged = { onSetCacheRadiusM(it.coerceAtLeast(0)) },
            onClearClicked = onClearCacheRadiusM,
            unit = "m",
            supportingText = "Used when you move without a route"
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun FreeRoamSettingsPreview() =
    FreeRoamSettingsComposable(
        settings = ComposeNavigationSettingsState(
            isFreeRoamEnabled = true,
            useSimulation = true,
            allowableSpeedExcessKph = 15f,
            simulationSpeedKph = 60f,
            navigationTypeOrdinal = 0,
            followControllerTypeOrdinal = 0,
            alternativeMinTimeGainSec = 0,
            alternativeMinLengthGainM = 0,
            alternativeSearchTimeoutSec = 0,
            freeRoamCacheDistanceOnRouteM = 0,
            freeRoamCacheRadiusOnRouteM = 0,
            freeRoamCacheRadiusM = 0
        ),
        onSetCacheDistanceOnRouteM = {},
        onClearCacheDistanceOnRouteM = {},
        onSetCacheRadiusOnRouteM = {},
        onClearCacheRadiusOnRouteM = {},
        onSetCacheRadiusM = {},
        onClearCacheRadiusM = {}
    )
