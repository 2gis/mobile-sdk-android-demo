package ru.dgis.sdk.demo.compose.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import ru.dgis.sdk.compose.map.MapComposableState

data class ComposeScreenData(
    val id: String,
    val title: String,
    val content: @Composable (MapComposableState, ViewModelStoreOwner) -> Unit
)
