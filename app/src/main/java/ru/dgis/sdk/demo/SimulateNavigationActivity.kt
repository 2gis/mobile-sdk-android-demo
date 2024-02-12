package ru.dgis.sdk.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.demo.databinding.ActivitySimulateNavigationBinding
import ru.dgis.sdk.demo.vm.MarkerUserData
import ru.dgis.sdk.demo.vm.RouteSearchPointWithMarker
import ru.dgis.sdk.demo.vm.SimulateNavigationViewModel
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.map.DragBeginData
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapDirection
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.ScreenPoint
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.imageFromResource
import ru.dgis.sdk.routing.Route
import ru.dgis.sdk.routing.RouteDistance
import ru.dgis.sdk.routing.RoutePoint
import ru.dgis.sdk.routing.RouteSearchPoint
import ru.dgis.sdk.routing.SegmentGeoPoint
import ru.dgis.sdk.routing.minus
import kotlin.coroutines.resume
import kotlin.math.abs

val initialStartPoint = RouteSearchPoint(GeoPoint(latitude = 25.173121, longitude = 55.255839))
val initialFinishPoint = RouteSearchPoint(GeoPoint(latitude = 25.218415, longitude = 55.284252))

/**
 * Activity demonstrates how to simulate the movement of an object based on provided geographical points.
 * Utilizes a route from RouteEditor for calculating distances between points and segment lengths.
 * Can be adapted to use any ordered collection of geographical points.
 * We encourage you to use hardware acceleration if applicable, see AndroidManifest.xml
 */
class SimulateNavigationActivity : AppCompatActivity(), TouchEventsObserver {
    // Initialization of essential components and variables.
    private val binding by lazy { ActivitySimulateNavigationBinding.inflate(layoutInflater) }
    private val vm by viewModels<SimulateNavigationViewModel>()
    private val sdkContext by lazy { application.sdkContext }

    // Map and marker management.
    private lateinit var mapObjectManager: MapObjectManager
    private lateinit var map: Map

    // Default speed for marker animation, in km/h.
    private var speed: Float = 60.0f

    private var isToastShown = false

    // AnimatedMarker wraps a DGis Marker to facilitate animated movements.
    private val carMarker: AnimatedMarker = AnimatedMarker(
        Marker(
            MarkerOptions(
                position = GeoPointWithElevation(Latitude(0.0), Longitude(0.0)),
                icon = null
            )
        )
    )

    // Coroutine Job for managing marker movement animations.
    private var movingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupWindowInsets()
        setupSpeedSlider()
        setupMapAndMarkers()

        binding.mapView.setTouchEventsObserver(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                vm.routeFlow.collect { route ->
                    movingJob?.cancel()
                    route?.let {
                        startRouteAnimation(it)
                    }
                }
            }
        }
    }

    // Event handling methods for marker dragging atr unrelated to the main simulation functionality.
    override fun onDragBegin(data: DragBeginData) {
        vm.onDragBegin(data)
    }

    // Event handling methods for marker dragging atr unrelated to the main simulation functionality.
    override fun onDragMove(point: ScreenPoint) {
        lifecycleScope.launch {
            map.camera.projection.screenToMap(point)?.let {
                vm.emitDragData(it)
            }
        }
    }

    // Event handling methods for marker dragging atr unrelated to the main simulation functionality.
    override fun onDragEnd() {
        vm.onDragEnd()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSpeedSlider() {
        binding.speedSlider.apply {
            value = speed

            addOnChangeListener { _, value, _ ->
                speed = value
                if (!isToastShown) {
                    Toast.makeText(
                        this@SimulateNavigationActivity,
                        "New speed will be applied on next segment",
                        Toast.LENGTH_LONG
                    ).show()
                    isToastShown = true
                }
            }
        }
    }

    private fun setupMapAndMarkers() {
        binding.mapView.getMapAsync { map ->
            this.map = map
            map.addSource(vm.routeEditorSource)
            mapObjectManager = MapObjectManager(map)

            val startMarker = Marker(
                MarkerOptions(
                    draggable = true,
                    icon = imageFromResource(sdkContext, R.drawable.ic_start),
                    position = GeoPointWithElevation(initialStartPoint.coordinates),
                    userData = MarkerUserData.START
                )
            ).also {
                mapObjectManager.addObject(it)
            }

            val finishMarker = Marker(
                MarkerOptions(
                    draggable = true,
                    icon = imageFromResource(sdkContext, R.drawable.ic_finish),
                    position = GeoPointWithElevation(initialFinishPoint.coordinates),
                    userData = MarkerUserData.FINISH
                )
            ).also {
                mapObjectManager.addObject(it)
            }

            vm.updateStartPoint(RouteSearchPointWithMarker(startMarker, initialStartPoint))
            vm.updateFinishPoint(RouteSearchPointWithMarker(finishMarker, initialFinishPoint))
        }
    }

    private fun startRouteAnimation(route: Route) {
        val routeGeometry = route.geometry
        val startPoint = routeGeometry.calculateGeoPoint(routeGeometry.first!!.point)
        carMarker.apply {
            mapObjectManager.removeObject(this.marker)
            marker.icon = imageFromResource(sdkContext, R.drawable.ic_minivan)
            marker.iconWidth = LogicalPixel(25.0f)
            marker.position = GeoPointWithElevation(startPoint!!.point)
            marker.iconMapDirection = MapDirection(startPoint.bearing.value)
            mapObjectManager.addObject(this.marker)
        }

        movingJob = lifecycleScope.launch {
            var previousPoint: RoutePoint? = null
            route.geometry.entries.asSequence().forEach { entry ->
                val stepDistance = previousPoint?.distance?.let { it1 ->
                    entry.point.distance.minus(it1)
                }
                previousPoint = entry.point
                routeGeometry.calculateGeoPoint(entry.point)?.let { segmentPoint ->
                    animateMarker(carMarker, segmentPoint, stepDistance, speed)
                }
            }
        }
    }

    /**
     * Animates the marker's movement along a given segment of the route, adjusting for the specified speed and distance.
     * The animation synchronously updates the marker's latitude, longitude, and direction.
     *
     * @param marker The AnimatedMarker instance to animate.
     * @param segmentPoint The destination point for the current segment, containing new geographical coordinates and bearing.
     * @param distance Optional distance covered in this animation segment, used to calculate animation duration.
     * @param speed The speed of movement in km/h, used alongside distance to calculate animation duration.
     */
    private suspend fun animateMarker(
        marker: AnimatedMarker,
        segmentPoint: SegmentGeoPoint,
        distance: RouteDistance? = null,
        speed: Float
    ) = suspendCancellableCoroutine { continuation ->
        // convert speed from km/h to mm/sec
        val speedInMm = speed * 277.78
        val animDuration = if (distance == null) {
            300L
        } else {
            (distance.millimeters / speedInMm).times(1000).toLong()
        }

        val latitudeAnimator = ObjectAnimator.ofFloat(
            marker,
            "animatedLatitude",
            marker.marker.position.latitude.value.toFloat(),
            segmentPoint.point.latitude.value.toFloat()
        ).apply {
            interpolator = LinearInterpolator()
            duration = animDuration
        }
        val longitudeAnimator = ObjectAnimator.ofFloat(
            marker,
            "animatedLongitude",
            marker.marker.position.longitude.value.toFloat(),
            segmentPoint.point.longitude.value.toFloat()
        ).apply {
            interpolator = LinearInterpolator()
            duration = animDuration
        }

        val currentDirection = marker.marker.iconMapDirection!!.value
        var endDirection = segmentPoint.bearing.value

        val angleDifference = endDirection - currentDirection
        endDirection = if (abs(angleDifference) > 180) {
            if (angleDifference > 0) {
                currentDirection - (360 - abs(angleDifference))
            } else {
                currentDirection + (360 - abs(angleDifference))
            }
        } else {
            currentDirection + angleDifference
        }

        val directionAnimator = ValueAnimator.ofFloat(currentDirection.toFloat(), endDirection.toFloat()).apply {
            duration = animDuration
            addUpdateListener { animator ->
                marker.marker.iconMapDirection = MapDirection((animator.animatedValue as Float).toDouble())
            }
        }

        AnimatorSet().apply {
            playTogether(latitudeAnimator, longitudeAnimator, directionAnimator)

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    continuation.resume(Unit)
                }
            })

            start()
        }.also { animator ->
            continuation.invokeOnCancellation {
                animator.cancel()
            }
        }
    }
}

/**
 * A helper wrapper class for the DGis Marker to facilitate updating its position with animated transitions.
 * Since the position's fields are immutable, operations to change the marker's geographical position are performed via this wrapper.
 */
private class AnimatedMarker(val marker: Marker) {
    @Suppress("unused")
    var animatedLatitude: Float
        get() = marker.position.latitude.value.toFloat()
        set(value) {
            marker.position = marker.position.copy(latitude = Latitude(value.toDouble()))
        }

    @Suppress("unused")
    var animatedLongitude: Float
        get() = marker.position.longitude.value.toFloat()
        set(value) {
            marker.position = marker.position.copy(longitude = Longitude(value.toDouble()))
        }
}
