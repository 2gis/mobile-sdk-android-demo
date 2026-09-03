package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ru.dgis.sdk.demo.common.demoMapOwner
import ru.dgis.sdk.demo.compose.createComposeMapControllerOptions
import ru.dgis.sdk.demo.compose.screens.HomeScreen

class ComposeActivity : ComponentActivity() {
    private val mapOwner by demoMapOwner(::createComposeMapControllerOptions)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                mapViewModel = mapOwner.mapViewModel
            )
        }
    }
}
