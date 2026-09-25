package ru.dgis.sdk.demo.compose.examples.searchitem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.dgis.sdk.Context
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.search.defaultcontrols.DefaultSearchResultItemState
import ru.dgis.sdk.compose.search.defaultcontrols.SearchResultItemComposable
import ru.dgis.sdk.compose.search.defaultcontrols.SearchResultItemComposableDefaults
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.asFlow
import ru.dgis.sdk.directory.DirectoryObject
import ru.dgis.sdk.directory.SearchManager
import ru.dgis.sdk.map.DgisMapObject

@Composable
fun SearchItemScreen(
    mapViewModel: ReadyMapControllerViewModel,
    sdkContext: Context,
    mapOptions: ComposeExampleMapOptions
) {
    val mapController = mapViewModel.mapController
    val searchManager by remember {
        mutableStateOf(SearchManager.createOnlineManager(sdkContext))
    }
    var directoryObject by remember { mutableStateOf<DirectoryObject?>(null) }

    LaunchedEffect(mapController, searchManager) {
        mapController.renderedObjectObserver.objectTapped.asFlow().collect { objects ->
            val objectInfo = objects.firstOrNull() ?: return@collect
            when (val mapObject = objectInfo.item.item) {
                is DgisMapObject -> {
                    searchManager
                        .searchByDirectoryObjectIds(listOf(mapObject.id))
                        .onComplete(
                            resultCallback = {
                                directoryObject = it.firstOrNull()
                            },
                            errorCallback = {
                                directoryObject = null
                            }
                        )
                }

                else -> {
                    directoryObject = null
                }
            }
        }
    }

    MapComposable(
        viewModel = mapViewModel,
        renderOptions = mapOptions.renderOptions,
        copyrightOptions = mapOptions.copyrightOptions
    )

    directoryObject?.let {
        Dialog(
            onDismissRequest = { directoryObject = null }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = SearchResultItemComposableDefaults.colors().backgroundColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            ) {
                SearchResultItemComposable(DefaultSearchResultItemState(it, null))
            }
        }
    }
}
