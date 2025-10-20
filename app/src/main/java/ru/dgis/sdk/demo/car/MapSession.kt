package ru.dgis.sdk.demo.car

import android.content.Intent
import android.view.Gravity
import android.view.Surface
import androidx.car.app.AppManager
import androidx.car.app.CarToast
import androidx.car.app.Screen
import ru.dgis.sdk.Future
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.androidauto.AndroidAutoMapSession
import ru.dgis.sdk.androidauto.CopyrightMargins
import ru.dgis.sdk.androidauto.CopyrightPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.RenderedObjectInfo

class MapSession(mapOptions: MapOptions) : AndroidAutoMapSession(mapOptions) {
    private val mainScreen = MainScreen(carContext)
    private var map: Map? = null
    private var customRenderer = CustomRenderer()
    private var getRenderedObjectFuture: Future<List<RenderedObjectInfo>>? = null

    private fun showToast(message: String) {
        carContext.getCarService(AppManager::class.java).showToast(message, CarToast.LENGTH_SHORT)
    }

    override fun onCreateScreen(intent: Intent): Screen {
        return mainScreen
    }

    override fun onSurfaceAvailable(surface: Surface, width: Int, height: Int) {
        customRenderer.start(surface)
    }

    override fun onSurfaceClicked(x: Float, y: Float) {
        getRenderedObjectFuture = map?.getRenderedObjects(centerPoint = ScreenPoint(x = x, y = y))?.apply {
            onResult {
                val objectInfo = it.firstOrNull()
                showToast("$objectInfo")
            }
        }
    }

    override fun onSurfaceDestroyed(surface: Surface) {
        customRenderer.stop()
    }

    override fun onMapReady(map: Map) {
        this.map = map

        mainScreen.setMap(map)

        setCopyrightPosition(
            CopyrightPosition(
                gravity = Gravity.BOTTOM or Gravity.START,
                margins = CopyrightMargins(left = 20, bottom = 20)
            )
        )
    }

    override fun onMapReadyException(exception: Exception) {
        exception.message?.let(::showToast)
    }
}
