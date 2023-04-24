package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import ru.dgis.sdk.Context
import ru.dgis.sdk.demo.databinding.ActivityNavigationBinding
import ru.dgis.sdk.demo.vm.NavigationViewModel
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.ScreenPoint
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.navigation.DefaultNavigationControls
import ru.dgis.sdk.navigation.NavigationView


class NavigationActivity : AppCompatActivity(), TouchEventsObserver {
    private lateinit var sdkContext: Context

    private var viewModel: NavigationViewModel? = null

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var routeEditorView: View
    private lateinit var navigationView: NavigationView
    private lateinit var startNavigationButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = (applicationContext as Application).sdkContext

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        routeEditorView = findViewById(R.id.routeEditorView)
        navigationView = findViewById(R.id.navigationView)

        findViewById<MapView>(R.id.mapView).apply {
            lifecycle.addObserver(binding.mapView)
            setTouchEventsObserver(this@NavigationActivity)
            getMapAsync(::initViewModel)
        }

        findViewById<SwitchMaterial>(R.id.simulationSwitch).apply {
            setOnCheckedChangeListener { _, isChecked ->
                viewModel?.useSimulation = isChecked
            }
        }

        findViewById<Spinner>(R.id.routeTypeSpinner).apply {
            adapter = ArrayAdapter.createFromResource(
                context,
                R.array.route_search_types,
                android.R.layout.simple_spinner_item
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    viewModel?.routeType = when (pos) {
                        1 -> NavigationViewModel.RouteType.PEDESTRIAN
                        2 -> NavigationViewModel.RouteType.BICYCLE
                        else -> NavigationViewModel.RouteType.CAR
                    }
                }
            }
        }

        startNavigationButton = findViewById<FloatingActionButton>(R.id.startButton).apply {
            setOnClickListener {
                viewModel?.startNavigation()
            }
        }
    }

    private fun initViewModel(map: Map) {
        val activity = this
        viewModel = NavigationViewModel(sdkContext, map, lifecycleScope).also { viewModel ->
            viewModel.messageCallback = {
                Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            }
            lifecycleScope.launchWhenStarted {
                viewModel.state.collect {
                    navigationView.removeAllViews()
                    when (it) {
                        NavigationViewModel.State.ROUTE_EDITING -> {
                            routeEditorView.visibility = View.VISIBLE
                            navigationView.navigationManager = null
                        }
                        NavigationViewModel.State.NAVIGATION -> {
                            routeEditorView.visibility = View.INVISIBLE
                            navigationView.navigationManager = viewModel.navigationManager
                            navigationView.addView(DefaultNavigationControls(navigationView.context).apply {
                                defaultState = viewModel.navigationType
                                onFinishClicked = {
                                    viewModel.stopNavigation()
                                }
                            })
                        }
                    }
                }
            }
            lifecycleScope.launchWhenStarted {
                viewModel.canStartNavigation.collect {
                    startNavigationButton.isEnabled = it
                }
            }
        }
    }

    private fun showMenu(point: ScreenPoint) {
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
                    viewModel?.onMenuAction(point, action)
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
        showMenu(point)
    }

    override fun onTap(point: ScreenPoint) {
        viewModel?.onTap(point)
    }

    override fun onBackPressed() {
        viewModel?.let {
            if (it.state.value == NavigationViewModel.State.NAVIGATION) {
                it.stopNavigation()
                return
            }
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.close()
    }
}
