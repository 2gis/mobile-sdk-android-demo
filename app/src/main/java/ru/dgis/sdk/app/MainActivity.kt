package ru.dgis.sdk.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ru.dgis.sdk.context.Context
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import ru.dgis.sdk.positioning.registerPlatformMagneticSource


typealias ScreenSelectedCallback = () -> Unit

data class Page(
    val screen: String,
    val select: ScreenSelectedCallback) {

    override fun toString() = screen
}


class MainActivity : AppCompatActivity() {
    private val RECORD_REQUEST_CODE = 101
    private lateinit var sdkContext: Context

    private val pages = listOf(
        Page("Generic Map") {
            val intent = Intent(this@MainActivity, GenericMapActivity::class.java)
            startActivity(intent)
        },
        Page("Custom Style") {
            val intent = Intent(this@MainActivity, MapStyleActivity::class.java)
            startActivity(intent)
        },
        Page("Markers") {
            val intent = Intent(this@MainActivity, MarkersActivity::class.java)
            startActivity(intent)
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupPermissions()
        (applicationContext as Application).registerServices()
    }

    private fun createImpl() {
        setContentView(R.layout.activity_main)

        findViewById<ListView>(R.id.pages).apply {
            adapter = ArrayAdapter(this@MainActivity,
                R.layout.main_list_item,
                R.id.page_name,
                pages)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                pages[position].select()
            }
        }
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!permission) {
            Log.i("APP", "Permission to record denied")
            makeRequest()
        } else {
            createImpl()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            RECORD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                             permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("APP", "Permission has been denied by user")
                } else {
                    Log.i("APP", "Permission has been granted by user")
                    createImpl()
                }
            }
        }
    }
}
