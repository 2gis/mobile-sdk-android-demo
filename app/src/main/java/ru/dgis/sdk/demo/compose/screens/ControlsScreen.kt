package ru.dgis.sdk.demo.compose.screens

import android.util.Log
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.demo.compose.MapControls

@Composable
fun ControlsScreen(mapState: MapComposableState) {
    val map by mapState.map.collectAsState()

    LaunchedEffect(mapState) {
        // Display copyright at the bottom left corner to avoid conflict with the MyLocation control.
        mapState.setCopyrightGravity(Gravity.BOTTOM or Gravity.START)

        // Just check custom uri opener.
        mapState.setCopyrightUriOpener { Log.d("ControlsScreen", it) }
    }

    MapComposable(state = mapState)

    map?.let {
        MapControls(map = it)
    }
}
