package ru.dgis.sdk.demo.compose.examples.navigation.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.dgis.sdk.demo.compose.examples.navigation.NavigationType

/**
 * Immutable snapshot of Compose navigation settings used by the settings UI.
 */
data class ComposeNavigationSettingsState(
    val isFreeRoamEnabled: Boolean = false,
    val useSimulation: Boolean = true,
    val allowableSpeedExcessKph: Float = 0f,
    val simulationSpeedKph: Float = 60f,
    val navigationTypeOrdinal: Int = 0,
    val followControllerTypeOrdinal: Int = 0,
    val alternativeMinTimeGainSec: Int = 0,
    val alternativeMinLengthGainM: Int = 0,
    val alternativeSearchTimeoutSec: Int = 0,
    val freeRoamCacheDistanceOnRouteM: Int = 0,
    val freeRoamCacheRadiusOnRouteM: Int = 0,
    val freeRoamCacheRadiusM: Int = 0,
    val finishDetectorSoftLimitM: Int = 0,
    val finishDetectorHardLimitM: Int = 0,
    val finishDetectorStraightLineLimitM: Int = 0,
    val finishDetectorVehicleSoftLimitM: Int = 0,
    val finishDetectorVehicleHardLimitM: Int = 0,
) {
    /** The route type selected by [navigationTypeOrdinal]. */
    val navigationType: NavigationType
        get() = NavigationType.entries.getOrNull(navigationTypeOrdinal) ?: NavigationType.Car
}

/**
 * ViewModel for Compose navigation settings.
 */
class ComposeNavigationSettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(ComposeNavigationSettingsState())
    val state: StateFlow<ComposeNavigationSettingsState> = _state

    fun setFreeRoamEnabled(enabled: Boolean) {
        if (enabled && _state.value.useSimulation) {
            _state.value = _state.value.copy(
                isFreeRoamEnabled = true,
                useSimulation = false,
            )
        } else {
            _state.value = _state.value.copy(isFreeRoamEnabled = enabled)
        }
    }

    fun setUseSimulation(enabled: Boolean) {
        if (_state.value.isFreeRoamEnabled && enabled) {
            return
        }
        _state.value = _state.value.copy(useSimulation = enabled)
    }

    fun setAllowableSpeedExcessKph(value: Float) {
        _state.value = _state.value.copy(allowableSpeedExcessKph = value)
    }

    fun setSimulationSpeedKph(value: Float) {
        _state.value = _state.value.copy(simulationSpeedKph = value)
    }

    fun setNavigationTypeOrdinal(value: Int) {
        _state.value = _state.value.copy(navigationTypeOrdinal = value)
    }

    fun setFollowControllerTypeOrdinal(value: Int) {
        _state.value = _state.value.copy(followControllerTypeOrdinal = value)
    }

    fun setAlternativeMinTimeGainSec(value: Int) {
        _state.value = _state.value.copy(alternativeMinTimeGainSec = value)
    }

    fun clearAlternativeMinTimeGainSec() {
        setAlternativeMinTimeGainSec(0)
    }

    fun setAlternativeMinLengthGainM(value: Int) {
        _state.value = _state.value.copy(alternativeMinLengthGainM = value)
    }

    fun clearAlternativeMinLengthGainM() {
        setAlternativeMinLengthGainM(0)
    }

    fun setAlternativeSearchTimeoutSec(value: Int) {
        _state.value = _state.value.copy(alternativeSearchTimeoutSec = value)
    }

    fun clearAlternativeSearchTimeoutSec() {
        setAlternativeSearchTimeoutSec(0)
    }

    fun setFreeRoamCacheDistanceOnRouteM(value: Int) {
        _state.value = _state.value.copy(freeRoamCacheDistanceOnRouteM = value)
    }

    fun clearFreeRoamCacheDistanceOnRouteM() {
        setFreeRoamCacheDistanceOnRouteM(0)
    }

    fun setFreeRoamCacheRadiusOnRouteM(value: Int) {
        _state.value = _state.value.copy(freeRoamCacheRadiusOnRouteM = value)
    }

    fun clearFreeRoamCacheRadiusOnRouteM() {
        setFreeRoamCacheRadiusOnRouteM(0)
    }

    fun setFreeRoamCacheRadiusM(value: Int) {
        _state.value = _state.value.copy(freeRoamCacheRadiusM = value)
    }

    fun clearFreeRoamCacheRadiusM() {
        setFreeRoamCacheRadiusM(0)
    }

    fun setFinishDetectorSoftLimitM(value: Int) {
        _state.value = _state.value.copy(finishDetectorSoftLimitM = value)
    }

    fun clearFinishDetectorSoftLimitM() {
        setFinishDetectorSoftLimitM(0)
    }

    fun setFinishDetectorHardLimitM(value: Int) {
        _state.value = _state.value.copy(finishDetectorHardLimitM = value)
    }

    fun clearFinishDetectorHardLimitM() {
        setFinishDetectorHardLimitM(0)
    }

    fun setFinishDetectorStraightLineLimitM(value: Int) {
        _state.value = _state.value.copy(finishDetectorStraightLineLimitM = value)
    }

    fun clearFinishDetectorStraightLineLimitM() {
        setFinishDetectorStraightLineLimitM(0)
    }

    fun setFinishDetectorVehicleSoftLimitM(value: Int) {
        _state.value = _state.value.copy(finishDetectorVehicleSoftLimitM = value)
    }

    fun clearFinishDetectorVehicleSoftLimitM() {
        setFinishDetectorVehicleSoftLimitM(0)
    }

    fun setFinishDetectorVehicleHardLimitM(value: Int) {
        _state.value = _state.value.copy(finishDetectorVehicleHardLimitM = value)
    }

    fun clearFinishDetectorVehicleHardLimitM() {
        setFinishDetectorVehicleHardLimitM(0)
    }
}
