package ru.dgis.sdk.demo.compose.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import ru.dgis.sdk.map.MapControllerViewModel

data class ComposeScreenData(
    val id: String,
    val title: String,
    val content: @Composable (MapControllerViewModel, ViewModelStoreOwner) -> Unit
)
