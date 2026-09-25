package ru.dgis.sdk.demo.compose.examples.copyright

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel

@Composable
fun CopyrightScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    var copyrightOptions by remember { mutableStateOf(mapOptions.copyrightOptions) }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = copyrightOptions
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column {
            MapCopyrightMarginsConfigurator(
                margins = copyrightOptions.margins,
                onMarginsChange = { copyrightOptions = copyrightOptions.copy(margins = it) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapCopyrightGravityConfigurator(
                    gravity = copyrightOptions.gravity,
                    onGravityChange = { copyrightOptions = copyrightOptions.copy(gravity = it) }
                )

                Checkbox(
                    checked = copyrightOptions.showApiVersion,
                    onCheckedChange = {
                        copyrightOptions = copyrightOptions.copy(showApiVersion = it)
                    }
                )
                Text(text = "Version")
            }
        }
    }
}
