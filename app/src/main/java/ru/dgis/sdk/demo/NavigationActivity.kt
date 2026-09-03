package ru.dgis.sdk.demo

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import ru.dgis.sdk.Context
import ru.dgis.sdk.ScreenDistance
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.await
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.common.attachMapView
import ru.dgis.sdk.demo.common.awaitMapControllerOrShowError
import ru.dgis.sdk.demo.common.bindMapControls
import ru.dgis.sdk.demo.common.collectTouchEvents
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.common.updateMapCopyrightPosition
import ru.dgis.sdk.demo.databinding.ActivityNavigationBinding
import ru.dgis.sdk.demo.vm.NavigationViewModel
import ru.dgis.sdk.geometry.point
import ru.dgis.sdk.map.CameraChangeReason
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.CopyrightMargins
import ru.dgis.sdk.map.DgisMapObject
import ru.dgis.sdk.map.GraphicsPreset
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapController
import ru.dgis.sdk.map.MapCopyrightOptions
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.statefulChanges
import ru.dgis.sdk.navigation.DefaultNavigationControls
import ru.dgis.sdk.navigation.NavigationView
import ru.dgis.sdk.navigation.State
import ru.dgis.sdk.routing.RouteSearchPoint

class NavigationActivity : AppCompatActivity(), TouchEventsObserver {
    private val sdkContext: Context by lazy { application.sdkContext }

    private val closeables = mutableListOf<AutoCloseable?>()
    private val mapOwner by demoMapOwner(
        CameraPosition(GeoPoint(55.740444, 37.619524), Zoom(12f))
    )

    private var mapController: MapController? = null
    private var navigationControls: DefaultNavigationControls? = null
    private var viewModel: NavigationViewModel? = null

    private lateinit var graphicPreset: RadioGroup
    private lateinit var map: Map
    private lateinit var mapView: MapView
    private lateinit var binding: ActivityNavigationBinding
    private lateinit var routeEditorView: View
    private lateinit var routeEditorSettingsView: View
    private lateinit var navigationView: NavigationView
    private lateinit var startNavigationButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        graphicPreset = findViewById(R.id.graphicPreset)
        graphicPreset.check(R.id.normalPreset)
        mapView = binding.mapContainer.attachMapView(
            mapOwner.mapViewModel,
            copyrightOptions = MapCopyrightOptions(
                margins = CopyrightMargins(
                    bottom = (48 * resources.displayMetrics.density).toInt()
                )
            )
        )
        routeEditorView = findViewById(R.id.routeEditorView)
        navigationView = findViewById(R.id.navigationView)
        routeEditorSettingsView = findViewById(R.id.route_editing_group)

        lifecycleScope.launch {
            val controller = awaitMapControllerOrShowError(mapOwner.mapViewModel) ?: return@launch
            mapController = controller
            map = controller.map
            binding.mapContainer.bindMapControls(controller, mapView)
            initViewModel(controller)
            launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    controller.collectTouchEvents(this@NavigationActivity)
                }
            }
            closeables.add(
                map.camera
                    .statefulChanges(CameraChangeReason.PADDING) { map.camera.padding }
                    .connect { _ ->
                        mapView.updateMapCopyrightPosition(
                            binding.content,
                            binding.settingsDrawerInnerLayout
                        )
                    }
            )
            when (map.graphicsPresetHintChannel.value) {
                GraphicsPreset.LITE -> graphicPreset.check(R.id.litePreset)
                GraphicsPreset.NORMAL -> graphicPreset.check(R.id.normalPreset)
                GraphicsPreset.IMMERSIVE -> graphicPreset.check(R.id.immersivePreset)
                else -> {}
            }
        }

        graphicPreset.setOnCheckedChangeListener { _, checkedId ->
            if (::map.isInitialized) {
                when (checkedId) {
                    R.id.litePreset -> map.graphicsPreset = GraphicsPreset.LITE
                    R.id.normalPreset -> map.graphicsPreset = GraphicsPreset.NORMAL
                    R.id.immersivePreset -> map.graphicsPreset = GraphicsPreset.IMMERSIVE
                }
            }
        }

        findViewById<SwitchMaterial>(R.id.simulationSwitch).apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel?.useSimulation = isChecked
            }
        }
        BottomSheetBehavior.from(findViewById(R.id.settingsDrawerInnerLayout)).apply {
            state = STATE_COLLAPSED
            addBottomSheetCallback(object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    mapView.updateMapCopyrightPosition(
                        binding.content,
                        binding.settingsDrawerInnerLayout
                    )
                }
            })
        }
        initRouteTypeTabs()

        startNavigationButton = findViewById<FloatingActionButton>(R.id.startButton).apply {
            setOnClickListener {
                viewModel?.startNavigation()
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel?.let {
                        when (it.state.value) {
                            NavigationViewModel.State.NAVIGATION -> it.stopNavigation()
                            NavigationViewModel.State.ROUTE_EDITING -> this@NavigationActivity.finish()
                        }
                    }
                }
            }
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val controller = mapController ?: return
        navigationView.post {
            navigationControls?.bindMapControls(controller, mapView)
        }
    }

    private fun initRouteTypeTabs() {
        binding.routeTypeTabsLayout.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // Should match on contentDescription field, because icons don't have text field and
                    // id is useless because of bug
                    // https://issuetracker.google.com/issues/145687658
                    viewModel?.routeType = when (tab?.contentDescription) {
                        getString(R.string.content_description_car) -> NavigationViewModel.RouteType.CAR
                        getString(R.string.content_description_bus) -> NavigationViewModel.RouteType.PUBLIC_TRANSPORT
                        getString(R.string.content_description_bicycle) -> NavigationViewModel.RouteType.BICYCLE
                        getString(R.string.content_description_pedestrian) -> NavigationViewModel.RouteType.PEDESTRIAN
                        getString(R.string.content_description_scooter) -> NavigationViewModel.RouteType.SCOOTER
                        getString(R.string.content_description_taxi) -> NavigationViewModel.RouteType.TAXI
                        else -> NavigationViewModel.RouteType.CAR
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    return
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    return
                }
            })
    }

    private fun initViewModel(controller: MapController) {
        val activity = this
        val map = controller.map
        this.map = map
        viewModel = NavigationViewModel(sdkContext, map, lifecycleScope).also { viewModel ->
            closeables.add(viewModel)
            viewModel.messageCallback = {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.state.collect {
                        navigationControls = null
                        navigationView.removeAllViews()
                        when (it) {
                            NavigationViewModel.State.ROUTE_EDITING -> {
                                routeEditorView.visibility = View.VISIBLE
                                routeEditorSettingsView.visibility = View.VISIBLE
                                navigationView.navigationManager = null
                            }

                            NavigationViewModel.State.NAVIGATION -> {
                                routeEditorView.visibility = View.INVISIBLE
                                routeEditorSettingsView.visibility = View.GONE
                                navigationView.navigationManager = viewModel.navigationManager
                                val controls = DefaultNavigationControls(navigationView.context).apply {
                                    isFreeRoamDefault = viewModel.navigationType != State.NAVIGATION
                                    onFinishClicked = viewModel::stopNavigation
                                }
                                navigationControls = controls
                                navigationView.addView(controls)
                                controls.bindMapControls(controller, mapView)
                            }
                        }
                    }
                }
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.canStartNavigation.collect {
                        startNavigationButton.isEnabled = it
                    }
                }
            }
        }
    }

    private fun showMenu(point: ScreenPoint, routeSearchPoint: RouteSearchPoint) {
        val anchorView = View(this).apply {
            x = point.x
            y = point.y
            layoutParams = ViewGroup.LayoutParams(1, 1)
            mapView.addView(this)
        }

        PopupMenu(this, anchorView, Gravity.CENTER).apply {
            inflate(R.menu.route_points_menu)
            setOnMenuItemClickListener {
                val action = when (it.itemId) {
                    R.id.menuStartPoint -> NavigationViewModel.MenuAction.SELECT_START_POINT
                    R.id.menuFinishPoint -> NavigationViewModel.MenuAction.SELECT_FINISH_POINT
                    R.id.menuClearPoints -> NavigationViewModel.MenuAction.CLEAR_POINTS
                    else -> null
                }
                if (action != null) {
                    viewModel?.onMenuAction(routeSearchPoint, action)
                }
                mapView.removeView(anchorView)
                true
            }
            setOnDismissListener {
                mapView.removeView(anchorView)
            }
        }.show()
    }

    override fun onLongTouch(point: ScreenPoint) {
        val viewModel = viewModel ?: return
        if (viewModel.state.value != NavigationViewModel.State.ROUTE_EDITING) {
            return
        }
        lifecycleScope.launch {
            map.getRenderedObjects(point, ScreenDistance(2.0f)).await().apply {
                when (this.firstOrNull()?.item?.item) {
                    is DgisMapObject -> showMenu(
                        point,
                        RouteSearchPoint(
                            this.first().closestMapPoint.point,
                            objectId = (this.first().item.item as DgisMapObject).id,
                            levelId = this.first().item.levelId
                        )
                    )

                    else -> showMenu(
                        point,
                        RouteSearchPoint(map.camera.projection.screenToMap(point)!!)
                    )
                }
            }
        }
    }

    override fun onTap(point: ScreenPoint) {
        viewModel?.onTap(point)
    }

    override fun onDestroy() {
        closeables.forEach {
            it?.close()
        }
        closeables.clear()
        super.onDestroy()
    }
}
