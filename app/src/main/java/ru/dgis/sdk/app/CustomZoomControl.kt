package ru.dgis.sdk.app

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Button
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

class CustomZoomControl @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : MapControl(context, attrs, defStyle) {
    private var model: ZoomControlModel? = null
    private val closeables = mutableListOf<AutoCloseable>()

    init {
        inflate(context, R.layout.custom_zoom_control, this)
    }

    override fun attachToMap(map: Map) {
        model = createZoomControlModel(map)
        attachButton(R.id.zoom_in, ZoomControlButton.ZOOM_IN)
        attachButton(R.id.zoom_out, ZoomControlButton.ZOOM_OUT)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun attachButton(id: Int, button: ZoomControlButton) {
        val model = model!!
        val view = findViewById<Button>(id)
        closeables.add(model.isEnabled(button).connect {
            view.isEnabled = it
            if (!it) {
                model.setPressed(button, false)
            }
        })

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    model.setPressed(button, true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    model.setPressed(button, false)
                }
            }
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun detachFromMap(map: Map) {
        findViewById<Button>(R.id.zoom_in).setOnTouchListener(null)
        findViewById<Button>(R.id.zoom_out).setOnTouchListener(null)
        closeables.forEach(AutoCloseable::close)
        closeables.clear()
        model?.close()
    }
}
