package ru.dgis.sdk.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_touch_events.*
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.coordinates.Arcdegree
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.directory.FormattingType
import ru.dgis.sdk.map.*
import ru.dgis.sdk.map.Map

class TouchEventsActivity : AppCompatActivity(), TouchEventsObserver {
	private lateinit var sdkContext: Context
	private lateinit var mapView: MapView

	private var map: Map? = null
    private var marker: GeometryMapObject? = null
	private val source: GeometryMapObjectSource? by lazy {
		GeometryMapObjectSourceBuilder(sdkContext).createSource()?.apply {
			map!!.addSource(this)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		sdkContext = initializeDGis(applicationContext)
		setContentView(R.layout.activity_touch_events)

		val initialTilt = 30f
        val maxTilt = 45f

		val mapOptions = MapOptions().apply {
			position = CameraPosition(
				GeoPoint(Arcdegree(55.747833), Arcdegree(37.541892)),
				Zoom(16.5f),
				Tilt(initialTilt),
				Arcdegree(0.0)
			)
		}

		mapView = MapView(this, mapOptions)
        mapView.setTouchEventsObserver(this)
		mapView.getMapAsync {
			map = it
		}
		mapContainer.addView(mapView)
		lifecycle.addObserver(mapView)

        seekBarTilt.progress = (initialTilt / maxTilt * 100).toInt()
        seekBarTilt.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				val camera = map?.camera ?: return
				val position = camera.position().value
				val newTilt = maxTilt * progress / 100f

				camera.setPosition(position.copy(tilt = Tilt(newTilt)))
			}
			override fun onStartTrackingTouch(seekBar: SeekBar?) {}
			override fun onStopTrackingTouch(seekBar: SeekBar?) {}
		})
	}

	override fun onTap(point: ViewportPoint) {
		val source = checkNotNull(this.source) { "source has been initialized here" }
        val map = checkNotNull(this.map) { "map has been initialized here" }

		marker?.let(source::removeObject)

        map.camera.projection().screenToMap(point)?.let { coords ->
			marker = MarkerBuilder()
				.setIconFromResource(R.drawable.ic_marker)
				.setAnchor(0.5f, 0.95f)
				.setPosition(coords)
				.build()
			source.addObject(marker)
		}

        mapView
			.getRenderedObjects(point, ScreenDistance(5f))
			.onResult {  renderedObjects ->
				val dgisObject = renderedObjects.mapNotNull { objectInfo ->
					objectInfo.item.item as? DgisMapObject
				}.firstOrNull() ?: return@onResult

				dgisObject.directoryObject().onResult {
					val obj = it ?: return@onResult

					val title = obj.title()
					val address = obj.formattedAddress(FormattingType.FULL)?.streetAddress

					val msg = if (title != address) {
						"""
							$title
							$address
						""".trimIndent()
					} else {
                        title
					}
					Toast
						.makeText(this, msg, Toast.LENGTH_SHORT)
						.show()
				}
			}
	}

	override fun onLongTouch(point: ViewportPoint) {
        // TODO: подсветить выбранный объект
	}
}