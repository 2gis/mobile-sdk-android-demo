package ru.dgis.sdk.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.dgis.sdk.demo.common.asFlow
import ru.dgis.sdk.demo.databinding.ActivityDownloadTerritoriesBinding
import ru.dgis.sdk.demo.vm.DownloadTerritoriesViewModel
import ru.dgis.sdk.demo.vm.Geometry
import ru.dgis.sdk.map.Map
import ru.dgis.sdk.update.Package
import ru.dgis.sdk.update.PackageUpdateStatus

private class TerritoryViewHolder(
    view: View,
    private val scope: CoroutineScope
) : RecyclerView.ViewHolder(view) {
    private var job: Job? = null
    private var offlinePackage: Package? = null

    private val name = view.findViewById<TextView>(R.id.pkgName)!!
    private val progressBar = view.findViewById<ProgressBar>(R.id.pkgProgressBar)!!
    private val installButton = view.findViewById<ImageButton>(R.id.pkgInstallButton)!!
    private val uninstallButton = view.findViewById<ImageButton>(R.id.pkgUninstallButton)!!

    init {
        progressBar.max = 100

        installButton.setOnClickListener {
            offlinePackage?.install()
        }

        uninstallButton.setOnClickListener {
            offlinePackage?.uninstall()
        }
    }

    fun setPackage(pkg: Package) {
        job?.cancel()

        job = scope.launch {
            launch {
                pkg.progressChannel.asFlow().collect {
                    progressBar.progress = it.toInt()
                }
            }

            launch {
                pkg.infoChannel.asFlow().collect {
                    name.text = it.name
                    progressBar.isVisible = it.updateStatus == PackageUpdateStatus.IN_PROGRESS
                    installButton.isVisible = it.updateStatus == PackageUpdateStatus.PAUSED
                    uninstallButton.isVisible = it.installed
                }
            }
        }

        this.offlinePackage = pkg
    }
}

private class PackagesAdapter(
    private val packages: List<Package>,
    private val scope: CoroutineScope
) : RecyclerView.Adapter<TerritoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TerritoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.territories_list_item, parent, false)
        return TerritoryViewHolder(view, scope)
    }

    override fun getItemCount() = packages.size

    override fun onBindViewHolder(holder: TerritoryViewHolder, position: Int) {
        holder.setPackage(packages[position])
    }
}

/**
 * This Activity demonstrates how to use the TerritoryManager to retrieve and display territories.
 *
 * This sample includes three modes for listing territories:
 * 1. All territories.
 * 2. Territories filtered by the current camera position.
 * 3. Territories filtered by the current viewport.
 */
class DownloadTerritoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadTerritoriesBinding
    private val viewModel: DownloadTerritoriesViewModel by viewModels()
    private var geometryFilterJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDownloadTerritoriesBinding.inflate(layoutInflater)

        binding.territoriesRecycleView.adapter = PackagesAdapter(listOf(), lifecycleScope)

        binding.searchView.setOnQueryTextFocusChangeListener(object : View.OnFocusChangeListener {
            private val bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
            private var previousBottomSheetState = bottomSheetBehavior.state

            override fun onFocusChange(view: View?, hasFocus: Boolean) {
                bottomSheetBehavior.isDraggable = hasFocus.not()

                if (hasFocus) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else if (previousBottomSheetState != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                previousBottomSheetState = bottomSheetBehavior.state
            }
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.nameFilter = newText
                return false
            }
        })

        binding.mapView.getMapAsync { map ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.packages.collect { territories ->
                        binding.territoriesRecycleView.adapter = PackagesAdapter(territories, lifecycleScope)
                    }
                }
            }

            binding.radioGroupFilters.setOnCheckedChangeListener { _, checkedId ->
                geometryFilterJob?.cancel()

                geometryFilterJob = when (checkedId) {
                    R.id.radioButtonFilterByPosition -> {
                        startFilterByGeoPoint(map)
                    }

                    R.id.radioButtonFilterByViewport -> {
                        startFilterByGeoRect(map)
                    }

                    else -> {
                        viewModel.geometryFilter = null
                        return@setOnCheckedChangeListener
                    }
                }
            }
        }

        setContentView(binding.root)
    }

    @OptIn(FlowPreview::class)
    private fun startFilterByGeoPoint(map: Map) = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            map.camera.positionChannel
                .asFlow()
                .debounce(512)
                .collect {
                    viewModel.geometryFilter = Geometry.Point(it.point)
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun startFilterByGeoRect(map: Map) = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            map.camera.visibleRectChannel
                .asFlow()
                .debounce(512)
                .collect {
                    viewModel.geometryFilter = Geometry.Rect(it)
                }
        }
    }
}
