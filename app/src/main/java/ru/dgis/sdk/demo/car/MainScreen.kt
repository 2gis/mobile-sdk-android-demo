package ru.dgis.sdk.demo.car

import androidx.annotation.DrawableRes
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.core.graphics.drawable.IconCompat
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.map.Map

class MainScreen(carContext: CarContext) : Screen(carContext) {
    private var model: MainScreenModel? = null

    private fun buildCarIcon(@DrawableRes id: Int): CarIcon {
        return CarIcon.Builder(IconCompat.createWithResource(carContext, id)).build()
    }

    fun setMap(map: Map) {
        model = MainScreenModel(map)
    }

    override fun onGetTemplate(): Template {
        val zoomInAction = Action.Builder()
            .setIcon(buildCarIcon(R.drawable.ic_zoom_in))
            .setOnClickListener { model?.zoomIn() }
            .build()

        val zoomOutAction = Action.Builder()
            .setIcon(buildCarIcon(R.drawable.ic_zoom_out))
            .setOnClickListener { model?.zoomOut() }
            .build()

        val myLocationAction = Action.Builder()
            .setIcon(buildCarIcon(R.drawable.ic_nav_point))
            .setOnClickListener { model?.recenter() }
            .build()

        return NavigationTemplate.Builder()
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.BACK)
                    .build()
            )
            .setMapActionStrip(
                ActionStrip.Builder()
                    .addAction(Action.PAN)
                    .addAction(zoomInAction)
                    .addAction(zoomOutAction)
                    .addAction(myLocationAction)
                    .build()
            )
            .build()
    }
}
