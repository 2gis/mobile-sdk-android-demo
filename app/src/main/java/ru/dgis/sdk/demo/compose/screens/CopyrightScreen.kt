package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.demo.compose.configurators.MapCopyrightGravityConfigurator
import ru.dgis.sdk.demo.compose.configurators.MapCopyrightMarginsConfigurator
import ru.dgis.sdk.map.MapOptions

@Composable
fun CopyrightScreen(mapState: MapComposableState) {
    MapComposable(state = mapState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column {
            MapCopyrightMarginsConfigurator(
                margins = mapState.copyrightMargins.collectAsState().value,
                onMarginsChange = { mapState.setCopyrightMargins(it) }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapCopyrightGravityConfigurator(
                    gravity = mapState.copyrightGravity.collectAsState().value,
                    onGravityChange = { mapState.setCopyrightGravity(it) }
                )

                Checkbox(
                    checked = mapState.showApiVersionInCopyright.collectAsState().value,
                    onCheckedChange = { mapState.setShowApiVersionInCopyright(it) }
                )
                Text(text = "Version")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CopyrightScreenPreview() {
    CopyrightScreen(mapState = MapComposableState(MapOptions()))
}
