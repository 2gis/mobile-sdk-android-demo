package ru.dgis.sdk.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import com.google.android.material.progressindicator.CircularProgressIndicator
import ru.dgis.sdk.File
import ru.dgis.sdk.demo.vm.MapStyleViewModel
import ru.dgis.sdk.map.*

internal const val MAP_STYLE_FILE = 4433

class MapStyleActivity : AppCompatActivity() {
    private lateinit var rootContainer: FrameLayout
    private val viewModel: MapStyleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_style)

        rootContainer = findViewById<FrameLayout>(R.id.rootContainer)

        viewModel.styleFile.observe(this, this::onStyleChanged)

        if (!viewModel.isStyleSelected) {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER
            }
            val loadingView = CircularProgressIndicator(this).apply {
                layoutParams = params
                isIndeterminate = true
            }
            rootContainer.addView(loadingView)

            requestStylesFile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != MAP_STYLE_FILE) return
        if (resultCode != RESULT_OK) {
            finish()
            return
        }

        val fileUrl = data?.data ?: return

        val stream = contentResolver.openInputStream(fileUrl)
        if (stream != null) {
            viewModel.loadStyle(stream)
            return
        }

        requestStylesFile()
    }

    private fun requestStylesFile() {
        val intent = Intent().apply{
            type = "*/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Map Style"), MAP_STYLE_FILE);
    }

    private fun onStyleChanged(styleFile: File) {
        val options = MapOptions().also {
            it.styleFile = styleFile
        }
        val mapView = MapView(this, options).apply {
            getMapAsync(viewModel::onMapReady)
            showApiVersionInCopyrightView = true
        }
        rootContainer.apply {
            removeAllViews()
            addView(mapView)
        }
    }
}