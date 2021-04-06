package ru.dgis.sdk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import ru.dgis.sdk.map.MapView

/*
 Для корректной работы MapView она должна быть присоединена к жизненному циклу - lifecycle.addObserver(mapView)
 Рекомендуется использовать жизненный цикл activity/fragment-a, но, если это не подходит,
 можно создать собственный LifecycleOwner, как показано в примере, и управлять им вручную.
 */
private class CustomLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    fun onCreate() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun onStart() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    fun onStop() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    fun onPause() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    fun onResume() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

class LifecycleActivity : AppCompatActivity() {
    private val lifecycleOwner = CustomLifecycleOwner()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleOwner.onCreate()

        setContentView(R.layout.activity_lifecycle)
        findViewById<MapView>(R.id.mapView).let {
            lifecycleOwner.lifecycle.addObserver(it)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleOwner.onResume()
    }

    override fun onStart() {
        super.onStart()
        lifecycleOwner.onStart()
    }

    override fun onStop() {
        super.onStop()
        lifecycleOwner.onStop()
    }

    override fun onPause() {
        super.onPause()
        lifecycleOwner.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
    }
}
