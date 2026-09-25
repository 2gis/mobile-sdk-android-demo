package ru.dgis.sdk.demo.compose.examples.navigation.settings

import kotlinx.coroutines.CoroutineScope
import ru.dgis.sdk.map.CameraBehaviour
import ru.dgis.sdk.map.FollowTilt
import ru.dgis.sdk.map.Map

/**
 * Switches the camera of a map between the default follow controller and
 * [PlatformStyleZoomFollowController], selected by [FollowControllerType].
 *
 * @param scope scope of the style zoom pulse of the custom controller.
 */
internal class FollowControllerSwitcher(scope: CoroutineScope) {
    private val topDownBehaviour = CameraBehaviour(
        position = null,
        tilt = FollowTilt.OFF
    )

    private val customFollowController = PlatformStyleZoomFollowController()

    private val zoomPulse = StyleZoomPulseController(
        scope = scope,
        setZoom = customFollowController::setStyleZoom
    )

    fun apply(map: Map, type: FollowControllerType) {
        when (type) {
            FollowControllerType.Default -> {
                zoomPulse.stop()
                map.camera.removeCustomFollowController()
            }

            FollowControllerType.Custom -> {
                map.camera.setCustomFollowController(customFollowController)

                zoomPulse.start()

                map.camera.setBehaviour(topDownBehaviour)
            }
        }
    }

    /** Stops the pulse and returns the default follow controller to [map]. */
    fun reset(map: Map?) {
        zoomPulse.stop()
        map?.camera?.removeCustomFollowController()
    }
}
