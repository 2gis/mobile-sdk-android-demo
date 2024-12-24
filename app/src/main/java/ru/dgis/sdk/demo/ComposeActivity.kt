package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dgis.sdk.demo.compose.MapViewModel
import ru.dgis.sdk.demo.compose.screens.HomeScreen
import ru.dgis.sdk.map.MapTheme

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel = viewModel<MapViewModel>()

            // Update the theme to match the system settings.
            val theme = if (isSystemInDarkTheme()) {
                MapTheme.defaultDarkTheme
            } else {
                MapTheme.defaultTheme
            }
            viewModel.state.setTheme(theme)

            HomeScreen(
                mapState = viewModel.state
            )
        }
    }
}
