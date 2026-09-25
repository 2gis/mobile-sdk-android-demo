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
 * Finish detector settings screen.
 *
 * This composable renders thresholds used by [ru.dgis.sdk.navigation.NavigationManager.finishDetector]
 * to decide when the route is considered finished.
 *
 * It is a pure UI component:
 * - receives current values via [ComposeNavigationSettingsState]
 * - emits updates via callbacks (handled by [ComposeNavigationSettingsViewModel])
 *
 * The values are applied to `navigationManager.finishDetector` right before navigation starts,
 * see [applySettings].
 */
@Composable
fun FinishDetectorSettingsComposable(
    settings: ComposeNavigationSettingsState,
    onSetSoftLimitM: (Int) -> Unit,
    onClearSoftLimitM: () -> Unit,
    onSetHardLimitM: (Int) -> Unit,
    onClearHardLimitM: () -> Unit,
    onSetStraightLineLimitM: (Int) -> Unit,
    onClearStraightLineLimitM: () -> Unit,
    onSetVehicleSoftLimitM: (Int) -> Unit,
    onClearVehicleSoftLimitM: () -> Unit,
    onSetVehicleHardLimitM: (Int) -> Unit,
    onClearVehicleHardLimitM: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Finish detector",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Distance thresholds that control when the navigation engine treats the route as finished.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        IntTextField(
            label = "Soft limit",
            value = settings.finishDetectorSoftLimitM,
            onValueChanged = { v -> onSetSoftLimitM(v.coerceAtLeast(0)) },
            onClearClicked = onClearSoftLimitM,
            unit = "m",
            supportingText = "0 — use SDK default",
        )
        IntTextField(
            label = "Hard limit",
            value = settings.finishDetectorHardLimitM,
            onValueChanged = { v -> onSetHardLimitM(v.coerceAtLeast(0)) },
            onClearClicked = onClearHardLimitM,
            unit = "m",
            supportingText = "0 — use SDK default",
        )
        IntTextField(
            label = "Straight line distance to finish",
            value = settings.finishDetectorStraightLineLimitM,
            onValueChanged = { v -> onSetStraightLineLimitM(v.coerceAtLeast(0)) },
            onClearClicked = onClearStraightLineLimitM,
            unit = "m",
            supportingText = "0 — use SDK default",
        )
        IntTextField(
            label = "Vehicle soft limit",
            value = settings.finishDetectorVehicleSoftLimitM,
            onValueChanged = { v -> onSetVehicleSoftLimitM(v.coerceAtLeast(0)) },
            onClearClicked = onClearVehicleSoftLimitM,
            unit = "m",
            supportingText = "0 — use SDK default. Used for car/truck navigation types",
        )
        IntTextField(
            label = "Vehicle hard limit",
            value = settings.finishDetectorVehicleHardLimitM,
            onValueChanged = { v -> onSetVehicleHardLimitM(v.coerceAtLeast(0)) },
            onClearClicked = onClearVehicleHardLimitM,
            unit = "m",
            supportingText = "0 — use SDK default. Used for car/truck navigation types",
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun FinishDetectorSettingsPreview() =
    FinishDetectorSettingsComposable(
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
            finishDetectorSoftLimitM = 0,
            finishDetectorHardLimitM = 0,
            finishDetectorStraightLineLimitM = 0,
            finishDetectorVehicleSoftLimitM = 0,
            finishDetectorVehicleHardLimitM = 0,
        ),
        onSetSoftLimitM = {},
        onClearSoftLimitM = {},
        onSetHardLimitM = {},
        onClearHardLimitM = {},
        onSetStraightLineLimitM = {},
        onClearStraightLineLimitM = {},
        onSetVehicleSoftLimitM = {},
        onClearVehicleSoftLimitM = {},
        onSetVehicleHardLimitM = {},
        onClearVehicleHardLimitM = {},
    )
