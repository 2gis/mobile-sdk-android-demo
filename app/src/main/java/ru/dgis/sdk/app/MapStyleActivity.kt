package ru.dgis.sdk.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.viewModels
import com.google.android.gms.common.util.IOUtils
import com.google.android.material.progressindicator.CircularProgressIndicator
import java9.util.concurrent.CompletableFuture
import ru.dgis.sdk.app.vm.MapStyleViewModel
import ru.dgis.sdk.map.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

internal const val MAP_STYLE_FILE = 4433

class MapStyleActivity : AppCompatActivity() {
    private lateinit var rootContainer: FrameLayout
    private val viewModel: MapStyleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_style)

        rootContainer = findViewById<FrameLayout>(R.id.rootContainer)

        viewModel.style.observe(this, this::onStyleChanged)

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
            viewModel.loadStyle(pullFile(stream))
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

    private fun pullFile(inStream: InputStream) : CompletableFuture<String> {
        return CompletableFuture.supplyAsync {
            val destinationFile = File.createTempFile("style-", ".2gis")
            FileOutputStream(destinationFile).use { outStream ->
                IOUtils.copyStream(inStream, outStream)
            }
            destinationFile.absolutePath
        }
    }

    private fun onStyleChanged(style: Style) {
        val options = MapOptions().also {
            it.style = style
        }
        val mapView = MapView(this, options).apply {
            getMapAsync(viewModel::onMapReady)
        }
        rootContainer.apply {
            removeAllViews()
            addView(mapView)
        }
    }
}