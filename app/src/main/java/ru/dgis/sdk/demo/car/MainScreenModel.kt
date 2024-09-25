package ru.dgis.sdk.demo.car

import ru.dgis.sdk.map.CameraBehaviour
import ru.dgis.sdk.map.FollowPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.ZoomControlButton
import ru.dgis.sdk.map.ZoomControlModel

class MainScreenModel(private val map: Map) {
    private val zoomControlModel = ZoomControlModel(map)

    fun zoomIn() {
        zoomControlModel.setPressed(ZoomControlButton.ZOOM_IN, true)
        zoomControlModel.setPressed(ZoomControlButton.ZOOM_IN, false)
    }

    fun zoomOut() {
        zoomControlModel.setPressed(ZoomControlButton.ZOOM_OUT, true)
        zoomControlModel.setPressed(ZoomControlButton.ZOOM_OUT, false)
    }

    fun recenter() {
        map.camera.setBehaviour(CameraBehaviour(position = FollowPosition()))
    }
}
