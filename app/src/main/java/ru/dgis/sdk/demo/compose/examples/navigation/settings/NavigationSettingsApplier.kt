package ru.dgis.sdk.demo.compose.examples.navigation.settings

import ru.dgis.sdk.Duration
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.navigation.SimulationConstantSpeed
import ru.dgis.sdk.navigation.SimulationSpeedMode
import ru.dgis.sdk.routing.RouteDistance

/**
 * Applies the values of [NavigationSettingsComposable] to the navigation manager.
 * Called right before navigation starts. A zero threshold of alternative routes, free roam
 * or the finish detector keeps the manager's own value.
 */
fun NavigationManager.applySettings(state: ComposeNavigationSettingsState) {
    exceedSpeedLimitSettings.allowableSpeedExcess =
        state.allowableSpeedExcessKph / 3.6f

    alternativeRoutesProviderSettings.apply {
        if (state.alternativeMinTimeGainSec > 0) {
            betterRouteTimeCostThreshold =
                Duration.ofSeconds(state.alternativeMinTimeGainSec.toLong())
        }

        if (state.alternativeMinLengthGainM > 0) {
            betterRouteLengthThreshold = RouteDistance(
                millimeters = state.alternativeMinLengthGainM.toLong() * 1000
            )
        }

        if (state.alternativeSearchTimeoutSec > 0) {
            val timeout = state.alternativeSearchTimeoutSec.coerceAtLeast(5)
            routeSearchDelay = Duration.ofSeconds(timeout.toLong())
        }
    }

    freeRoamSettings.apply {
        if (state.freeRoamCacheDistanceOnRouteM > 0) {
            onRoutePrefetchLength =
                RouteDistance(millimeters = state.freeRoamCacheDistanceOnRouteM.toLong() * 1000)
        }
        if (state.freeRoamCacheRadiusOnRouteM > 0) {
            onRoutePrefetchRadiusMeters = state.freeRoamCacheRadiusOnRouteM.toDouble()
        }
        if (state.freeRoamCacheRadiusM > 0) {
            prefetchRadiusMeters = state.freeRoamCacheRadiusM.toDouble()
        }
    }

    finishDetector.apply {
        if (state.finishDetectorSoftLimitM > 0) {
            softLimitMeters =
                RouteDistance(millimeters = state.finishDetectorSoftLimitM.toLong() * 1000)
        }
        if (state.finishDetectorHardLimitM > 0) {
            hardLimitMeters =
                RouteDistance(millimeters = state.finishDetectorHardLimitM.toLong() * 1000)
        }
        if (state.finishDetectorStraightLineLimitM > 0) {
            distanceToFinishInStraightLineLimitMeters =
                state.finishDetectorStraightLineLimitM.toDouble()
        }
        if (state.finishDetectorVehicleSoftLimitM > 0) {
            vehicleSoftLimitMeters =
                RouteDistance(millimeters = state.finishDetectorVehicleSoftLimitM.toLong() * 1000)
        }
        if (state.finishDetectorVehicleHardLimitM > 0) {
            vehicleHardLimitMeters =
                RouteDistance(millimeters = state.finishDetectorVehicleHardLimitM.toLong() * 1000)
        }
    }

    if (state.useSimulation) {
        val speedMps = (state.simulationSpeedKph / 3.6f).toDouble()
        simulationSettings.speedMode =
            SimulationSpeedMode(speed = SimulationConstantSpeed(speed = speedMps))
    }
}
