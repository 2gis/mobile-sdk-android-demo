package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.RGBAImage
import ru.dgis.sdk.map.ImageData
import ru.dgis.sdk.map.MapOptions

@Composable
private fun SnapshotDialog(imageData: ImageData, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Column {
                RGBAImage(imageData.data, imageData.size.width, imageData.size.height)
                Button(onClick = { onDismiss() }) {
                    Text(text = "Close")
                }
            }
        }
    }
}

@Composable
private fun SnapshotButton(enabled: Boolean, onClick: () -> Unit) {
    FilledIconButton(
        onClick = { onClick() },
        enabled = enabled,
        modifier = Modifier
            .padding(5.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_photo_camera_24),
            contentDescription = null
        )
    }
}

@Composable
fun SnapshotScreen(mapState: MapComposableState) {
    var snapshot by remember { mutableStateOf<ImageData?>(null) }

    MapComposable(state = mapState)

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
    ) {
        SnapshotButton(
            enabled = mapState.map.collectAsState().value != null,
            onClick = {
                mapState.takeSnapshot()?.let { it ->
                    it.onComplete(
                        resultCallback = { snapshot = it },
                        errorCallback = {}
                    )
                }
            }
        )
    }

    snapshot?.let {
        SnapshotDialog(it) {
            snapshot = null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenshotScreenPreview() {
    SnapshotScreen(mapState = MapComposableState(MapOptions()))
}
