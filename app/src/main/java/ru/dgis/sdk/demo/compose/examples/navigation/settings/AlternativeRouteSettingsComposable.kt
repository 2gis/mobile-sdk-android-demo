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
 * Alternative route settings screen.
 *
 * This composable renders a small form that controls thresholds used by the navigation engine
 * to decide whether an alternative route is worth showing.
 *
 * It is a pure UI component:
 * - receives current values via [ComposeNavigationSettingsState]
 * - emits updates via callbacks (handled by [ComposeNavigationSettingsViewModel])
 */
@Composable
fun AlternativeRouteSettingsComposable(
    settings: ComposeNavigationSettingsState,
    onSetMinTimeGainSec: (Int) -> Unit,
    onClearMinTimeGainSec: () -> Unit,
    onSetMinLengthGainM: (Int) -> Unit,
    onClearMinLengthGainM: () -> Unit,
    onSetTimeoutSec: (Int) -> Unit,
    onClearTimeoutSec: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Alternative route",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Thresholds that control when an alternative route is considered worth showing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        IntTextField(
            label = "Minimum time gain",
            value = settings.alternativeMinTimeGainSec,
            onValueChanged = { v -> onSetMinTimeGainSec(v.coerceAtLeast(0)) },
            onClearClicked = onClearMinTimeGainSec,
            unit = "s",
            supportingText = "0 — disable threshold",
        )
        IntTextField(
            label = "Minimum length gain",
            value = settings.alternativeMinLengthGainM,
            onValueChanged = { v -> onSetMinLengthGainM(v.coerceAtLeast(0)) },
            onClearClicked = onClearMinLengthGainM,
            unit = "m",
            supportingText = "0 — disable threshold",
        )
        IntTextField(
            label = "Search timeout",
            value = settings.alternativeSearchTimeoutSec,
            onValueChanged = { v -> onSetTimeoutSec(v.coerceAtLeast(5)) },
            onClearClicked = onClearTimeoutSec,
            unit = "s",
            supportingText = "Minimum 5 seconds",
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun AlternativeRouteSettingsPreview() =
    AlternativeRouteSettingsComposable(
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
            freeRoamCacheRadiusM = 0,
        ),
        onSetMinTimeGainSec = {},
        onClearMinTimeGainSec = {},
        onSetMinLengthGainM = {},
        onClearMinLengthGainM = {},
        onSetTimeoutSec = {},
        onClearTimeoutSec = {},
    )
