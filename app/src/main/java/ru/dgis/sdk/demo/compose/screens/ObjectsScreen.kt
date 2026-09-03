package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.collectController
import ru.dgis.sdk.demo.common.asFlow
import ru.dgis.sdk.demo.compose.demoMapCopyrightOptions
import ru.dgis.sdk.demo.compose.demoMapRenderOptions
import ru.dgis.sdk.demo.compose.previewMapViewModel
import ru.dgis.sdk.map.MapControllerViewModel
import ru.dgis.sdk.map.RenderedObjectInfo

@Composable
private fun ObjectCard(mapObject: RenderedObjectInfo?, onClose: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Some object", style = MaterialTheme.typography.headlineSmall)
            Text(text = "$mapObject", modifier = Modifier.padding(top = 10.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ObjectsScreen(mapViewModel: MapControllerViewModel) {
    val controller = mapViewModel.collectController()
    var selectedObject by remember { mutableStateOf<RenderedObjectInfo?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    LaunchedEffect(controller) {
        controller?.renderedObjectObserver?.objectTapped?.asFlow()?.collect { objects ->
            val objectInfo = objects.firstOrNull() ?: return@collect
            selectedObject = objectInfo
            coroutineScope.launch {
                bottomSheetState.show()
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            ObjectCard(selectedObject) {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }
            }
        }
    ) {
        MapComposable(
            viewModel = mapViewModel,
            renderOptions = demoMapRenderOptions,
            copyrightOptions = demoMapCopyrightOptions
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ObjectsScreenPreview() {
    ObjectsScreen(mapViewModel = previewMapViewModel())
}
