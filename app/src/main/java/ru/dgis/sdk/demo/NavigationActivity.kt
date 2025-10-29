package ru.dgis.sdk.demo

import android.os.Bundle
import android.util.Log
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
import ru.dgis.sdk.File
import ru.dgis.sdk.ScreenDistance
import ru.dgis.sdk.ScreenPoint
import ru.dgis.sdk.await
import ru.dgis.sdk.demo.common.updateMapCopyrightPosition
import ru.dgis.sdk.demo.databinding.ActivityNavigationBinding
import ru.dgis.sdk.demo.vm.NavigationViewModel
import ru.dgis.sdk.geometry.point
import ru.dgis.sdk.map.CameraChangeReason
import ru.dgis.sdk.map.DgisMapObject
import ru.dgis.sdk.map.GraphicsPreset
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.StyleBuilder
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.statefulChanges
import ru.dgis.sdk.navigation.DefaultNavigationControls
import ru.dgis.sdk.navigation.NavigationView
import ru.dgis.sdk.navigation.State
import ru.dgis.sdk.routing.RouteSearchPoint

class NavigationActivity : AppCompatActivity(), TouchEventsObserver {
    private val sdkContext: Context by lazy { application.sdkContext }

    private val closeables = mutableListOf<AutoCloseable?>()

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
        mapView = findViewById(R.id.mapView)
        routeEditorView = findViewById(R.id.routeEditorView)
        navigationView = findViewById(R.id.navigationView)
        routeEditorSettingsView = findViewById(R.id.route_editing_group)

        mapView.setCopyrightMargins(0, 0, 0, 48 * (resources.displayMetrics.density).toInt())

        findViewById<MapView>(R.id.mapView).apply {
            lifecycle.addObserver(binding.mapView)
            setTouchEventsObserver(this@NavigationActivity)
            getMapAsync {
                initViewModel(it)
                closeables.add(
                    it.camera
                        .statefulChanges(CameraChangeReason.PADDING) { it.camera.padding }
                        .connect { _ ->
                            updateMapCopyrightPosition(
                                binding.content,
                                binding.settingsDrawerInnerLayout
                            )
                        }
                )
                when (it.graphicsPresetHintChannel.value) {
                    GraphicsPreset.LITE -> graphicPreset.check(R.id.litePreset)
                    GraphicsPreset.NORMAL -> graphicPreset.check(R.id.normalPreset)
                    GraphicsPreset.IMMERSIVE -> graphicPreset.check(R.id.immersivePreset)
                    else -> {}
                }
            }
        }

        graphicPreset.setOnCheckedChangeListener { _, checkedId ->
            mapView.getMapAsync { map ->
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

    private fun initViewModel(map: Map) {
        val activity = this
        this.map = map
        StyleBuilder(context = sdkContext).loadStyle(File.fromAsset(sdkContext, "styles_dark.2gis")).apply {
            onResult { s ->
                map.fontIconSizeMultiplier = 2f
                map.interactive = true
                map.style = s
            }
            onError { it ->
                Log.d("initViewModel style setup", it.message ?: "Unknown error")
            }
        }
        viewModel = NavigationViewModel(sdkContext, map, lifecycleScope).also { viewModel ->
            closeables.add(viewModel)
            viewModel.messageCallback = {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.state.collect {
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
                                navigationView.addView(
                                    DefaultNavigationControls(navigationView.context).apply {
                                        isFreeRoamDefault =
                                            viewModel.navigationType != State.NAVIGATION
                                        onFinishClicked = {
                                            viewModel.stopNavigation()
                                        }
                                    }
                                )
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
            binding.mapView.addView(this)
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
                binding.mapView.removeView(anchorView)
                true
            }
            setOnDismissListener {
                binding.mapView.removeView(anchorView)
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
        super.onDestroy()
        closeables.forEach {
            it?.close()
        }
    }
}
