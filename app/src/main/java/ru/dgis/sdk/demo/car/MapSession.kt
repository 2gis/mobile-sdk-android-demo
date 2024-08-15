package ru.dgis.sdk.demo.car

import android.content.Intent
import androidx.car.app.AppManager
import androidx.car.app.CarToast
import androidx.car.app.Screen
import ru.dgis.sdk.androidauto.AndroidAutoMapSession
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.ScreenPoint

class MapSession : AndroidAutoMapSession(MapOptions()) {
    private var map: Map? = null

    private fun showToast(message: String) {
        carContext.getCarService(AppManager::class.java)
            .showToast(message, CarToast.LENGTH_SHORT)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        return MainScreen(carContext)
    }

    override fun onMapReady(map: Map) {
        this.map = map
    }

    override fun onMapReadyException(exception: Exception) {
        exception.message?.let(::showToast)
    }

    override fun onMapClicked(x: Float, y: Float) {
        map?.getRenderedObjects(centerPoint = ScreenPoint(x = x, y = y))?.onResult {
            val objectInfo = it.firstOrNull()
            showToast("$objectInfo")
        }
    }
}
