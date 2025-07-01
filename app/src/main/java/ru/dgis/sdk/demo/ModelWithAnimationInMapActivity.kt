package ru.dgis.sdk.demo

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import ru.dgis.sdk.Context
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapDirection
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.ModelMapObject
import ru.dgis.sdk.map.ModelMapObjectOptions
import ru.dgis.sdk.map.ModelScale
import ru.dgis.sdk.map.ModelSize
import ru.dgis.sdk.map.MyLocationControllerSettings
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.Padding
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.modelDataFromAsset

class ModelWithAnimationInMapActivity : AppCompatActivity() {
    private val sdkContext: Context by lazy { application.sdkContext }
    lateinit var mapSource: MyLocationMapObjectSource


    private var map: Map? = null
    var movementAnimator: ValueAnimator? = null
    var rotationAnimator: ValueAnimator? = null

    private lateinit var mapView: MapView
    private lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_with_animation)
        root = findViewById(R.id.content)
        mapView = findViewById<MapView>(R.id.mapView).also {
            it.getMapAsync(this::onMapReady)
            it.showApiVersionInCopyrightView = true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun onMapReady(map: Map) {
        this.map = map
        mapSource = MyLocationMapObjectSource(
            sdkContext,
            MyLocationControllerSettings(BearingSource.MAGNETIC)
        )
        //moving camera to car location
        val screenHeight = root.height
        val mapBottomPaddingOffset = (screenHeight * 0.4).roundToTop()
        map.camera.padding = Padding(0, 0, 0, bottom = mapBottomPaddingOffset)
        map.camera.move(
            CameraPosition(
                point = GeoPoint(latitude = 40.209339212102556, longitude = 44.51782621674358),
                zoom = Zoom(40f),
            ), time = Duration.ofMilliseconds(300)
        )
        //loading 3d model
        val mapObjectManager = MapObjectManager(map)
        val fileName = "blue_car.glb"
        val location = GeoPointWithElevation(40.209339212102556, 44.51782621674358)
        val modelData = modelDataFromAsset(sdkContext, fileName)
        val modelObject = ModelMapObject(
            ModelMapObjectOptions(
                position = location,
                data = modelData,
                size = ModelSize(ModelScale(0.055f)),
            )
        )
        map.addSource(mapSource)
        //adding 3d model to map
        mapObjectManager.addObject(modelObject)
        //animating model movements and rotation with animation
        animateCarMovement(modelObject, GeoPoint(40.20949240044926, 44.51422545018235))
        animateCarRotation(modelObject)
    }

    fun Double.roundToTop(): Int {
        val intPart = this.toInt()
        val decimalPart = this - intPart
        return if (decimalPart >= 0.1) intPart + 1 else intPart
    }

    private fun Double.normalizeAngle(): Double = (this % 360 + 360) % 360

    //car rotating animation
    private fun animateCarRotation(modelData: ModelMapObject) {
        val currentAngle = modelData.mapDirection?.value ?: 0.0
        val targetAngle = modelData.mapDirection?.value?.normalizeAngle() ?: return
        val angleDelta = ((targetAngle - currentAngle + 540) % 360) - 180 // Shortest way

        rotationAnimator = ValueAnimator.ofFloat(0f, angleDelta.toFloat()).apply {
            duration = 4000L
            interpolator = LinearInterpolator()
            // Listener to update the model's direction during animation.
            addUpdateListener { animator ->
                val animatedDelta = animator.animatedValue as Float
                val newAngle = (currentAngle + animatedDelta).normalizeAngle()
                modelData.mapDirection = MapDirection(newAngle)
            }
            start()
            doOnEnd {
                rotationAnimator?.start()
            }
        }
    }

    //car movement animation
    private fun animateCarMovement(modelObject: ModelMapObject, newLocation: GeoPoint) {
        val fromLat = 40.209339212102556
        val fromLng = 44.51782621674358
        // Target latitude and longitude for the animation.
        val toLat: Double = newLocation.latitude.value
        val toLng: Double = newLocation.longitude.value
        // Calculate the distance to determine animation duration.
        movementAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000L
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                // Interpolate the position of the modelObject based on animation progress.
                val t = animation.animatedFraction
                modelObject.position = GeoPointWithElevation(
                    latitude = (1 - t) * fromLat + t * toLat,
                    longitude = (1 - t) * fromLng + t * toLng
                )
            }
            start()
            doOnEnd {
                movementAnimator?.start()
            }
        }
    }
}
