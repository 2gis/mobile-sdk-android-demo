package ru.dgis.sdk.demo.compose.examples.navigation.settings

import ru.dgis.sdk.map.EmptyFollowController
import ru.dgis.sdk.map.FollowValue
import ru.dgis.sdk.map.NewValuesNotifier
import ru.dgis.sdk.map.StyleZoom
import java.util.EnumSet

/**
 * Custom follow controller
 *
 * Call [setStyleZoom] to update the value; the controller notifies the camera via
 * [NewValuesNotifier] so the change is applied.
 */
internal class PlatformStyleZoomFollowController : EmptyFollowController() {
    private var notifier: NewValuesNotifier? = null
    private var styleZoomValue: StyleZoom? = null

    override fun availableValues(): EnumSet<FollowValue> = EnumSet.of(FollowValue.STYLE_ZOOM)

    override fun setNewValuesNotifier(notifier: NewValuesNotifier?) {
        this.notifier = notifier
    }

    fun setStyleZoom(styleZoom: StyleZoom?) {
        styleZoomValue = styleZoom
        notifier?.sendNotification()
    }

    override fun styleZoom(): StyleZoom? = styleZoomValue
}
