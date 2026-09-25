package ru.dgis.sdk.demo.compose.examples.sharedroute

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.dgis.sdk.compose.map.MapComposable
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.demo.compose.ComposeExampleMapOptions
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.demo.compose.extra.collectTouchEvents

@Composable
fun SharedRouteScreen(
    mapViewModel: ReadyMapControllerViewModel,
    viewModel: SharedRouteScreenViewModel,
    mapOptions: ComposeExampleMapOptions
) {
    val mapController = mapViewModel.mapController

    val pointsState by viewModel.pointsState
    val saveRouteState by viewModel.saveRouteState.collectAsStateWithLifecycle()
    val sharedRouteState by viewModel.sharedRouteState.collectAsStateWithLifecycle()
    val requestId by viewModel.requestId.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var topPanelExpanded by remember { mutableStateOf(true) }
    var bottomPanelExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(message) {
        message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(mapController, viewModel) {
        viewModel.onMapReady(mapController.map)
        mapController.collectTouchEvents(viewModel)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapComposable(
                viewModel = mapViewModel,
                renderOptions = mapOptions.renderOptions,
                copyrightOptions = mapOptions.copyrightOptions
            )

            SharedRouteTopPanel(
                requestId = requestId,
                onRequestIdChange = viewModel::updateRequestId,
                isFetching = sharedRouteState is SharedRouteFetchState.Loading ||
                    saveRouteState is SaveRouteState.Loading,
                onFetch = { viewModel.fetchSharedRoute(requestId) },
                sharedRouteState = sharedRouteState,
                saveRouteState = saveRouteState,
                onClearRoute = viewModel::clearSharedRoute,
                expanded = topPanelExpanded,
                onToggleExpanded = { topPanelExpanded = !topPanelExpanded },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
            )

            SharedRouteBottomPanel(
                pointsState = pointsState,
                onClearPoints = viewModel::clearPoints,
                expanded = bottomPanelExpanded,
                onToggleExpanded = { bottomPanelExpanded = !bottomPanelExpanded },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SharedRouteTopPanel(
    requestId: String,
    onRequestIdChange: (String) -> Unit,
    isFetching: Boolean,
    onFetch: () -> Unit,
    sharedRouteState: SharedRouteFetchState,
    saveRouteState: SaveRouteState,
    onClearRoute: () -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Shared Route",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onToggleExpanded,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = requestId,
                            onValueChange = onRequestIdChange,
                            label = { Text("Request ID") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        FilledTonalIconButton(
                            onClick = onFetch,
                            enabled = !isFetching && requestId.isNotBlank(),
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (isFetching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Fetch shared route"
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = sharedRouteState is SharedRouteFetchState.Error,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (sharedRouteState is SharedRouteFetchState.Error) {
                            ErrorChip(text = "Fetch: ${sharedRouteState.message}")
                        }
                    }

                    AnimatedVisibility(
                        visible = saveRouteState is SaveRouteState.Error,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (saveRouteState is SaveRouteState.Error) {
                            ErrorChip(text = "Save: ${saveRouteState.message}")
                        }
                    }

                    AnimatedVisibility(
                        visible = sharedRouteState is SharedRouteFetchState.Success,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (sharedRouteState is SharedRouteFetchState.Success) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = {
                                        Text("Routes found: ${sharedRouteState.routeCount}")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Place,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = onClearRoute,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text("Clear route")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedRouteBottomPanel(
    pointsState: PointsState,
    onClearPoints: () -> Unit,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val start = pointsState.start
    val finish = pointsState.finish
    val hasAnyPoint = start != null || finish != null

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Route points",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onToggleExpanded,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PointRow(
                        icon = Icons.Filled.Place,
                        iconTint = Color(0xFF1BA136),
                        label = "Start",
                        point = start
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    PointRow(
                        icon = Icons.Filled.Place,
                        iconTint = MaterialTheme.colorScheme.error,
                        label = "Finish",
                        point = finish
                    )

                    AnimatedVisibility(
                        visible = hasAnyPoint,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TextButton(
                            onClick = onClearPoints,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text("Clear points")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PointRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    point: GeoPoint?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = point?.let {
                    "%.6f, %.6f".format(it.latitude.value, it.longitude.value)
                } ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ErrorChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
