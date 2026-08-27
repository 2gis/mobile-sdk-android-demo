package ru.dgis.sdk.demo.compose.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.demo.compose.configurators.MapCopyrightGravityConfigurator
import ru.dgis.sdk.demo.compose.configurators.MapCopyrightMarginsConfigurator
import ru.dgis.sdk.demo.compose.demoMapCopyrightOptions
import ru.dgis.sdk.demo.compose.demoMapRenderOptions
import ru.dgis.sdk.demo.compose.previewMapViewModel
import ru.dgis.sdk.map.MapControllerViewModel

@Composable
fun CopyrightScreen(mapViewModel: MapControllerViewModel) {
    var copyrightOptions by remember { mutableStateOf(demoMapCopyrightOptions) }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = demoMapRenderOptions,
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

@Preview(showBackground = true)
@Composable
fun CopyrightScreenPreview() {
    CopyrightScreen(mapViewModel = previewMapViewModel())
}
