package ru.dgis.sdk.demo.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.dgis.sdk.compose.map.MapComposableState
import ru.dgis.sdk.map.MapOptions

@Composable
private fun HomeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 16.sp)
    }
}

@Composable
private fun HomeScreen(navController: NavController) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeButton(text = "Theme", onClick = { navController.navigate("theme") })
        }
        item {
            HomeButton(text = "Copyright", onClick = { navController.navigate("copyright") })
        }
        item {
            HomeButton(text = "Fps", onClick = { navController.navigate("fps") })
        }
        item {
            HomeButton(text = "Objects", onClick = { navController.navigate("objects") })
        }
        item {
            HomeButton(text = "Snapshot", onClick = { navController.navigate("snapshot") })
        }
        item {
            HomeButton(text = "Map Controls", onClick = { navController.navigate("controls") })
        }
        item {
            HomeButton(text = "Markers", onClick = { navController.navigate("markers") })
        }
    }
}

@Composable
fun HomeScreen(mapState: MapComposableState) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("theme") { ThemeScreen(mapState = mapState) }
        composable("copyright") { CopyrightScreen(mapState = mapState) }
        composable("fps") { FpsScreen(mapState = mapState) }
        composable("objects") { ObjectsScreen(mapState = mapState) }
        composable("snapshot") { SnapshotScreen(mapState = mapState) }
        composable("controls") { ControlsScreen(mapState = mapState) }
        composable("markers") { MarkersScreen(mapState = mapState) }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        mapState = MapComposableState(MapOptions())
    )
}
