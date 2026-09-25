package ru.dgis.sdk.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.dgis.sdk.demo.compose.home.HomeScreen

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // The examples take their colors from the theme, which follows the system one.
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                // The theme of the app makes the status bar translucent: keep the examples below it.
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.statusBarsPadding()) {
                        HomeScreen(
                            screens = composeExampleCatalog,
                            viewModel = viewModel { composeHomeScreenViewModel(application.sdkContext) }
                        )
                    }
                }
            }
        }
    }
}
