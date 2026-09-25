package ru.dgis.sdk.demo.compose.examples.objects

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.asFlow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectsScreen(mapViewModel: ReadyMapControllerViewModel, mapOptions: ComposeExampleMapOptions) {
    val controller = mapViewModel.mapController
    var selectedObject by remember { mutableStateOf<RenderedObjectInfo?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()

    LaunchedEffect(controller) {
        controller.renderedObjectObserver.objectTapped.asFlow().collect { objects ->
            selectedObject = objects.firstOrNull() ?: return@collect
        }
    }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = mapOptions.copyrightOptions
    )

    selectedObject?.let { mapObject ->
        ModalBottomSheet(
            onDismissRequest = { selectedObject = null },
            sheetState = bottomSheetState
        ) {
            ObjectCard(mapObject) {
                coroutineScope.launch { bottomSheetState.hide() }
                    .invokeOnCompletion { selectedObject = null }
            }
        }
    }
}
