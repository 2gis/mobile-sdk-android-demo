package ru.dgis.sdk.demo.compose.examples.routeEditor

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.collectTouchEvents
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.compose.routeeditor.RouteEditorComposable
import ru.dgis.sdk.map.Padding

@Composable
fun RouteEditorScreen(
    mapViewModel: ReadyMapControllerViewModel,
    viewModel: RouteEditorScreenViewModel,
    mapOptions: ComposeExampleMapOptions,
) {
    val mapController = mapViewModel.mapController
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx: Context = context
        while (ctx is ContextWrapper && ctx !is AppCompatActivity) {
            ctx = ctx.baseContext
        }
        ctx as? AppCompatActivity
    }
    DisposableEffect(Unit) {
        activity?.supportActionBar?.hide()
        onDispose { activity?.supportActionBar?.show() }
    }

    val routeEditorVM = remember { viewModel.roureEditorComposableVM }
    val arePointsSet by viewModel.arePointsSet
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val startPaddingPx =
        remember(density, isLandscape) {
            if (isLandscape) {
                with(density) { RouteEditorScreenViewModel.DEFAULT_PADDING_HORIZONTAL.dp.roundToPx() }
            } else {
                0
            }
        }
    val maxBottomPaddingPx =
        remember(density, configuration) {
            with(density) { (configuration.screenHeightDp.dp * 0.6f).roundToPx() }
        }

    LaunchedEffect(mapController, viewModel) {
        viewModel.onMapReady(mapController.map)
        mapController.collectTouchEvents(viewModel)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapComposable(
            viewModel = mapViewModel,
            renderOptions = mapOptions.renderOptions,
            copyrightOptions = mapOptions.copyrightOptions,
        )

        if (arePointsSet) {
            RouteEditorComposable(
                modifier =
                    Modifier
                        .padding(top = 4.dp)
                        .onSizeChanged { size ->
                            val adjustedWidth = size.width + startPaddingPx

                            val cameraPadding =
                                if (isLandscape) {
                                    Padding(
                                        left = adjustedWidth,
                                        right = RouteEditorScreenViewModel.DEFAULT_PADDING_HORIZONTAL,
                                        top = RouteEditorScreenViewModel.DEFAULT_PADDING_TOP,
                                        bottom = RouteEditorScreenViewModel.DEFAULT_PADDING_BOTTOM,
                                    )
                                } else {
                                    Padding(
                                        left = RouteEditorScreenViewModel.DEFAULT_PADDING_HORIZONTAL,
                                        right = RouteEditorScreenViewModel.DEFAULT_PADDING_HORIZONTAL,
                                        top = RouteEditorScreenViewModel.DEFAULT_PADDING_TOP,
                                        bottom =
                                            size.height.coerceAtMost(
                                                maxBottomPaddingPx,
                                            ) + RouteEditorScreenViewModel.DEFAULT_PADDING_BOTTOM,
                                    )
                                }

                            viewModel.updateCameraPadding(cameraPadding)
                        }
                        .then(
                            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = RouteEditorScreenViewModel.DEFAULT_PADDING_HORIZONTAL.dp)
                            } else {
                                Modifier.align(Alignment.BottomCenter)
                            },
                        ),
                viewModel = routeEditorVM,
                settingsRepository = viewModel.settingsRepository,
                onCloseClick = viewModel::clearPoints,
            )
        }
    }
}
