package ru.dgis.sdk.app

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.StyleBuilder
import java.lang.ref.WeakReference


// todo: это не должны быть константы в SDK?
private const val NIGHT_MODE_ATTR = "night_on"
private const val DIMENSION_ATTR = "is_2d"


class NightThemeActivity : AppCompatActivity() {
    private lateinit var sdkContext : Context
    private lateinit var menu: PopupMenu
    private var map: Map? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = initializeDGis(applicationContext)

        setContentView(R.layout.activity_night_theme)

        val menuButton = findViewById<FloatingActionButton>(R.id.toggleAttributeMenu).apply {
            isEnabled = false
            setOnClickListener {
                menu.show()
            }
        }
        menu = PopupMenu(this, menuButton, Gravity.END, 0, R.style.GeometryPopupTheme)
        menu.inflate(R.menu.attributes_menu)
        menu.setOnMenuItemClickListener { item ->
            item.isChecked = !item.isChecked
            when (item.itemId) {
                R.id.darkMode -> map?.setStyleAttribute(NIGHT_MODE_ATTR, item.isChecked)
                R.id.flatMode -> map?.setStyleAttribute(DIMENSION_ATTR, item.isChecked)
            }
            true
        }

        val viewContext = WeakReference(this)

        // TODO: we can better!
        StyleBuilder(sdkContext).getDefaultStyle().onResult { nightStyle ->
            viewContext.get()?.let { activity ->
                nightStyle!!
                    .styleAttributes()
                    .setAttributeValue(NIGHT_MODE_ATTR, true)

                val mapOptions = MapOptions().apply {
                    style = nightStyle
                }
                val mapView = MapView(activity, mapOptions)

                activity.findViewById<LinearLayout>(R.id.map_container).apply {
                    addView(mapView)
                }
                activity.lifecycle.addObserver(mapView)

                mapView.getMapAsync {
                    map = it
                    menuButton.isEnabled = true
                }
            }
        }
    }
}