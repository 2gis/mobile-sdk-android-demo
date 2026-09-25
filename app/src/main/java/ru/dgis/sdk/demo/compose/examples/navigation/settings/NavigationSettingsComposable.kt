package ru.dgis.sdk.demo.compose.examples.navigation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.dgis.sdk.compose.navigation.R
import ru.dgis.sdk.demo.compose.components.EnumToggle
import ru.dgis.sdk.demo.compose.examples.navigation.NavigationType

private const val itemHeight = 32
private const val titleTextSize = 16
private const val itemTextSize = 14
private const val subitemTextSize = 12

private enum class NavigationSettingsScreen {
    Root,
    AlternativeRoute,
    FreeRoam,
    FinishDetector
}

/**
 * Compose navigation settings dialog.
 *
 * This composable shows a modal dialog that lets the user configure navigation-related
 * options before starting navigation.
 *
 * Internally it:
 * - uses [ComposeNavigationSettingsViewModel] to hold the values while the dialog is shown
 * - displays a small navigation stack (root + sub-screens)
 *
 * The dialog does not start navigation itself. The caller decides what to do on [onOk]
 * and [onCancel]; the values are applied with [applySettings].
 */
@Composable
fun NavigationSettingsComposable(
    settingsViewModel: ComposeNavigationSettingsViewModel,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    val composeSettings by settingsViewModel.state.collectAsStateWithLifecycle()

    var screenStack by remember { mutableStateOf(listOf(NavigationSettingsScreen.Root)) }
    val screen = screenStack.last()

    val navigateTo: (NavigationSettingsScreen) -> Unit = { target ->
        screenStack = screenStack + target
    }
    val navigateBack: () -> Unit = {
        screenStack = if (screenStack.size > 1) screenStack.dropLast(1) else screenStack
    }

    BackHandler(enabled = screenStack.size > 1) {
        navigateBack()
    }

    Dialog(
        onDismissRequest = {
            if (screenStack.size > 1) {
                screenStack = listOf(NavigationSettingsScreen.Root)
            }
        }
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsHeader(
                    screen = screen,
                    onBack = navigateBack,
                    onClose = {
                        onCancel()
                    }
                )

                when (screen) {
                    NavigationSettingsScreen.Root -> {
                        NavigationSettingsRoot(
                            settings = composeSettings,
                            onSetFreeRoamEnabled = settingsViewModel::setFreeRoamEnabled,
                            onSetUseSimulation = settingsViewModel::setUseSimulation,
                            onChangeAllowableSpeedExcess = { delta ->
                                val newValue = (composeSettings.allowableSpeedExcessKph + delta)
                                    .coerceAtLeast(0f)
                                settingsViewModel.setAllowableSpeedExcessKph(newValue)
                            },
                            onChangeSimulationSpeed = { delta ->
                                val newValue = (composeSettings.simulationSpeedKph + delta)
                                    .coerceAtLeast(0f)
                                settingsViewModel.setSimulationSpeedKph(newValue)
                            },
                            onSetNavigationTypeOrdinal = settingsViewModel::setNavigationTypeOrdinal,
                            onSetFollowControllerTypeOrdinal = settingsViewModel::setFollowControllerTypeOrdinal,
                            onOpenAlternativeRouteSettings = { navigateTo(NavigationSettingsScreen.AlternativeRoute) },
                            onOpenFreeRoamSettings = { navigateTo(NavigationSettingsScreen.FreeRoam) },
                            onOpenFinishDetectorSettings = { navigateTo(NavigationSettingsScreen.FinishDetector) },
                            onOk = onOk,
                            onCancel = onCancel
                        )
                    }

                    NavigationSettingsScreen.AlternativeRoute -> {
                        AlternativeRouteSettingsComposable(
                            settings = composeSettings,
                            onSetMinTimeGainSec = settingsViewModel::setAlternativeMinTimeGainSec,
                            onClearMinTimeGainSec = settingsViewModel::clearAlternativeMinTimeGainSec,
                            onSetMinLengthGainM = settingsViewModel::setAlternativeMinLengthGainM,
                            onClearMinLengthGainM = settingsViewModel::clearAlternativeMinLengthGainM,
                            onSetTimeoutSec = settingsViewModel::setAlternativeSearchTimeoutSec,
                            onClearTimeoutSec = settingsViewModel::clearAlternativeSearchTimeoutSec
                        )
                    }

                    NavigationSettingsScreen.FreeRoam -> {
                        FreeRoamSettingsComposable(
                            settings = composeSettings,
                            onSetCacheDistanceOnRouteM = settingsViewModel::setFreeRoamCacheDistanceOnRouteM,
                            onClearCacheDistanceOnRouteM = settingsViewModel::clearFreeRoamCacheDistanceOnRouteM,
                            onSetCacheRadiusOnRouteM = settingsViewModel::setFreeRoamCacheRadiusOnRouteM,
                            onClearCacheRadiusOnRouteM = settingsViewModel::clearFreeRoamCacheRadiusOnRouteM,
                            onSetCacheRadiusM = settingsViewModel::setFreeRoamCacheRadiusM,
                            onClearCacheRadiusM = settingsViewModel::clearFreeRoamCacheRadiusM
                        )
                    }

                    NavigationSettingsScreen.FinishDetector -> {
                        FinishDetectorSettingsComposable(
                            settings = composeSettings,
                            onSetSoftLimitM = settingsViewModel::setFinishDetectorSoftLimitM,
                            onClearSoftLimitM = settingsViewModel::clearFinishDetectorSoftLimitM,
                            onSetHardLimitM = settingsViewModel::setFinishDetectorHardLimitM,
                            onClearHardLimitM = settingsViewModel::clearFinishDetectorHardLimitM,
                            onSetStraightLineLimitM = settingsViewModel::setFinishDetectorStraightLineLimitM,
                            onClearStraightLineLimitM = settingsViewModel::clearFinishDetectorStraightLineLimitM,
                            onSetVehicleSoftLimitM = settingsViewModel::setFinishDetectorVehicleSoftLimitM,
                            onClearVehicleSoftLimitM = settingsViewModel::clearFinishDetectorVehicleSoftLimitM,
                            onSetVehicleHardLimitM = settingsViewModel::setFinishDetectorVehicleHardLimitM,
                            onClearVehicleHardLimitM = settingsViewModel::clearFinishDetectorVehicleHardLimitM
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    screen: NavigationSettingsScreen,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (screen != NavigationSettingsScreen.Root) {
            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        val title = when (screen) {
            NavigationSettingsScreen.AlternativeRoute -> "Alternative route settings"
            NavigationSettingsScreen.FreeRoam -> "Freeroam settings"
            NavigationSettingsScreen.FinishDetector -> "Finish detector settings"
            NavigationSettingsScreen.Root -> "Navigator settings"
        }

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            fontSize = titleTextSize.sp,
            overflow = TextOverflow.Ellipsis,
            textAlign = if (screen == NavigationSettingsScreen.Root) TextAlign.Center else TextAlign.Left,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }
    }
}

@Composable
private fun NavigationSettingsRoot(
    settings: ComposeNavigationSettingsState,
    onSetFreeRoamEnabled: (Boolean) -> Unit,
    onSetUseSimulation: (Boolean) -> Unit,
    onChangeAllowableSpeedExcess: (Float) -> Unit,
    onChangeSimulationSpeed: (Float) -> Unit,
    onSetNavigationTypeOrdinal: (Int) -> Unit,
    onSetFollowControllerTypeOrdinal: (Int) -> Unit,
    onOpenAlternativeRouteSettings: () -> Unit,
    onOpenFreeRoamSettings: () -> Unit,
    onOpenFinishDetectorSettings: () -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    SubtitleItem(
        text = "General",
        titleSize = titleTextSize.sp
    )

    SwitchItem(
        text = "Freeroam",
        checked = settings.isFreeRoamEnabled,
        onChecked = onSetFreeRoamEnabled
    )

    SubmenuItem(
        mainText = "Freeroam settings",
        subText = if (settings.isFreeRoamEnabled) "Enabled" else "Disabled",
        onClick = onOpenFreeRoamSettings
    )

    HorizontalDivider()

    SubtitleItem(
        text = "Simulation",
        titleSize = titleTextSize.sp
    )

    SwitchItem(
        text = "Use simulation",
        checked = settings.useSimulation,
        enabled = !settings.isFreeRoamEnabled,
        onChecked = onSetUseSimulation
    )

    SimulationSpeedItem(
        speedKph = settings.simulationSpeedKph,
        onSpeedChanged = onChangeSimulationSpeed
    )

    SpeedLimitItem(
        speedKph = settings.allowableSpeedExcessKph,
        onSpeedChanged = onChangeAllowableSpeedExcess
    )

    HorizontalDivider()

    SubtitleItem(
        text = "Route",
        titleSize = titleTextSize.sp
    )

    SubmenuItem(
        mainText = "Alternative route settings",
        subText = "Thresholds & timeout",
        onClick = onOpenAlternativeRouteSettings
    )

    HorizontalDivider()

    SubtitleItem(
        text = "Finish detector",
        titleSize = titleTextSize.sp
    )

    SubmenuItem(
        mainText = "Finish detector settings",
        subText = "Soft/hard limits & straight line distance",
        onClick = onOpenFinishDetectorSettings
    )

    SubtitleItem(
        text = "Route type",
        titleSize = titleTextSize.sp
    )

    EnumToggleItem(
        value = NavigationType.entries
            .getOrNull(settings.navigationTypeOrdinal)
            ?: NavigationType.Car,
        onSelected = { onSetNavigationTypeOrdinal(it.ordinal) }
    )

    SubtitleItem(
        text = "Follow controller type",
        titleSize = titleTextSize.sp
    )

    EnumToggleItem(
        value = FollowControllerType.entries
            .getOrNull(settings.followControllerTypeOrdinal)
            ?: FollowControllerType.Default,
        onSelected = { onSetFollowControllerTypeOrdinal(it.ordinal) }
    )

    ConfirmItem(
        onOk = onOk,
        onCancel = onCancel
    )
}

@Composable
private fun SimulationSpeedItem(
    speedKph: Float,
    onSpeedChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier.height(itemHeight.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TwoLineText(
            modifier = Modifier.weight(1f),
            mainText = "Simulation speed",
            subText = "%.0f km/h".format(speedKph)
        )

        Button(
            onClick = { onSpeedChanged(-5f) }
        ) {
            Text(text = "-")
        }

        Button(
            onClick = { onSpeedChanged(+5f) }
        ) {
            Text(text = "+")
        }
    }
}

@Composable
private fun SubtitleItem(
    text: String,
    titleSize: TextUnit,
    align: TextAlign = TextAlign.Left
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        fontSize = titleSize,
        overflow = TextOverflow.Ellipsis,
        textAlign = align,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SwitchItem(
    text: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChecked: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 16.sp,
            overflow = TextOverflow.Ellipsis,
            color = if (enabled) Color.Unspecified else Color.Gray
        )

        Switch(
            modifier = Modifier.height(itemHeight.dp),
            checked = checked,
            enabled = enabled,
            onCheckedChange = onChecked
        )
    }
}

@Composable
private fun SpeedLimitItem(
    speedKph: Float,
    onSpeedChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier.height(itemHeight.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TwoLineText(
            modifier = Modifier.weight(1f),
            mainText = "Permissible speeding",
            subText = "%.0f km/h".format(speedKph)
        )

        Button(
            onClick = { onSpeedChanged(-1f) }
        ) {
            Text(text = "-")
        }

        Button(
            onClick = { onSpeedChanged(+1f) }
        ) {
            Text(text = "+")
        }
    }
}

@Composable
private fun SubmenuItem(
    mainText: String,
    subText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(itemHeight.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TwoLineText(
            modifier = Modifier.weight(1f),
            mainText = mainText,
            subText = subText
        )

        Icon(
            modifier = Modifier.rotate(-90f),
            imageVector = ImageVector.vectorResource(R.drawable.dgis_navi_ic_collapse),
            contentDescription = null
        )
    }
}

@Composable
private fun TwoLineText(
    modifier: Modifier,
    mainText: String,
    subText: String? = null
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = mainText,
            fontSize = itemTextSize.sp,
            overflow = TextOverflow.Ellipsis
        )

        subText?.let {
            Text(
                text = it,
                color = Color.Gray,
                fontSize = subitemTextSize.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private inline fun <reified T : Enum<T>> EnumToggleItem(
    value: T,
    crossinline onSelected: (T) -> Unit
) {
    val buttonMinHeight = 40.dp
    val gap = 8.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        enumValues<T>().forEach { option ->
            EnumToggle(
                value = option,
                isSelected = value == option,
                modifier = Modifier
                    .weight(1f)
                    .sizeIn(minHeight = buttonMinHeight),
                onSelected = onSelected
            )
        }
    }
}

@Composable
private fun ConfirmItem(
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.padding(top = itemHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopEnd
        ) {
            Button(onClick = onCancel) {
                Text(text = "Cancel")
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            Button(onClick = onOk) {
                Text(text = "GO!")
            }
        }
    }
}

@Composable
@Preview
private fun NavigationSettingsPreview() =
    NavigationSettingsRoot(
        settings = ComposeNavigationSettingsState(
            isFreeRoamEnabled = false,
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
            finishDetectorStraightLineLimitM = 0
        ),
        onSetFreeRoamEnabled = {},
        onSetUseSimulation = {},
        onChangeAllowableSpeedExcess = {},
        onChangeSimulationSpeed = {},
        onSetNavigationTypeOrdinal = {},
        onSetFollowControllerTypeOrdinal = {},
        onOpenAlternativeRouteSettings = {},
        onOpenFreeRoamSettings = {},
        onOpenFinishDetectorSettings = {},
        onOk = {},
        onCancel = {}
    )
