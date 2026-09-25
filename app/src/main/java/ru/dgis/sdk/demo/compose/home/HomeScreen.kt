package ru.dgis.sdk.demo.compose.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExampleTopic
import ru.dgis.sdk.demo.compose.ComposeExampleEnvironment
import ru.dgis.sdk.demo.compose.ComposeExampleLabel
import ru.dgis.sdk.demo.compose.CatalogEntry
import ru.dgis.sdk.demo.compose.ReadyMapControllerViewModel
import ru.dgis.sdk.map.MapAppearance
import ru.dgis.sdk.map.MapControllerState

@Composable
fun HomeScreen(
    screens: List<CatalogEntry>,
    viewModel: HomeScreenViewModel,
    colors: HomeScreenColors = HomeScreenDefaults.colors(),
) {
    val navController = rememberNavController()
    val resolveRule by viewModel.resolveRule.collectAsState()

    NavHost(
        navController,
        startDestination = "home",
        // The default NavHost crossfade fades the map screen over a black window:
        // SurfaceView takes no part in alpha animations and the cover is translucent,
        // so the whole transition looks like a black flash. The map needs no transitions.
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable("home") {
            HomeScreen(
                screens = screens,
                onItemClick = { screenId ->
                    viewModel.onExampleOpened()
                    navController.navigate(screenId)
                },
                keepMapState = resolveRule == MapStateResolveRule.Shared,
                onKeepMapStateChanged = {
                    viewModel.setResolveRule(
                        if (it) MapStateResolveRule.Shared else MapStateResolveRule.Unique
                    )
                },
                colors = colors,
            )
        }
        screens.forEach { screen ->
            composable(screen.example.id) {
                val owner = checkNotNull(LocalViewModelStoreOwner.current)

                val mapViewModel = viewModel.mapViewModel

                when (val state = mapViewModel.state.collectAsState().value) {
                    is MapControllerState.Creating -> MapPlaceholder(state.options.mapAppearance)
                    is MapControllerState.Created -> {
                        val scope = remember(state.controller, owner) {
                            ComposeExampleEnvironment(
                                mapViewModel = ReadyMapControllerViewModel(state.controller),
                                viewModelStoreOwner = owner,
                                sdkContext = viewModel.sdkContext,
                                mapOptions = viewModel.mapOptions,
                            )
                        }
                        screen.example.content(scope)
                    }
                    is MapControllerState.Error -> MapCreationError(state.cause)
                    // Only the owner (HomeScreenViewModel) closes the model; by then the
                    // screen is already leaving the composition, so there is nothing to draw.
                    MapControllerState.Closed -> Unit
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(appearance: MapAppearance?) {
    // The SDK resolves null in the controller options to BySystem with the default themes.
    val background = (appearance ?: MapAppearance.defaultAppearance())
        .effectiveTheme(isSystemInDarkTheme()).loadingBackground
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(background.argb))
    )
}

@Composable
private fun MapCreationError(cause: Exception) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Map creation failed: ${cause.message ?: cause}")
    }
}

@Composable
private fun HomeScreen(
    screens: List<CatalogEntry>,
    onItemClick: (String) -> Unit,
    keepMapState: Boolean,
    onKeepMapStateChanged: (Boolean) -> Unit,
    colors: HomeScreenColors,
) {
    val orderedTabs = remember(screens) {
        listOf(
            ComposeExampleTopic.Navigation,
            ComposeExampleTopic.Map,
            ComposeExampleTopic.Directory,
        ).filter { topic ->
            // Skip empty tabs.
            screens.any { it.example.topic == topic }
        }
    }
    val pagerState = rememberPagerState(pageCount = { orderedTabs.size })
    // Saved state: the catalog leaves the composition while an example is open.
    var filterText by rememberSaveable { mutableStateOf("") }
    val labels = remember(screens) { screens.mapNotNull { it.label }.distinct() }
    // One label or none: nothing to tell apart, so the catalog shows no labels at all.
    val showLabels = labels.size > 1
    var selectedLabelNames by rememberSaveable(labels) { mutableStateOf(labels.map { it.name }) }
    val selectedLabels = labels.filterTo(mutableSetOf()) { it.name in selectedLabelNames }
    var infoScreen by remember { mutableStateOf<CatalogEntry?>(null) }
    var showKeepMapInfo by remember { mutableStateOf(false) }
    var showLabelsInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(color = colors.backgroundColor)
            .padding(horizontal = 8.dp),
    ) {
        FilterTextField(
            text = filterText,
            onTextChange = { filterText = it },
            colors = colors,
        )

        if (showLabels) {
            LabelFilter(
                labels = labels,
                selected = selectedLabels,
                onToggle = { label ->
                    selectedLabelNames = if (label.name in selectedLabelNames) {
                        selectedLabelNames - label.name
                    } else {
                        selectedLabelNames + label.name
                    }
                },
                onInfoClick = { showLabelsInfo = true },
                colors = colors,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // The info follows the label it explains; the switch stays at the right edge.
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = stringResource(R.string.compose_catalog_keep_map),
                    color = colors.textColor,
                )
                InfoButton(
                    contentDescription = stringResource(R.string.compose_catalog_keep_map),
                    onClick = { showKeepMapInfo = true },
                    colors = colors,
                )
            }

            Switch(
                checked = keepMapState,
                onCheckedChange = onKeepMapStateChanged,
                colors = SwitchDefaults.colors(
                    uncheckedTrackColor = colors.switchUncheckedBackgroundColor,
                    uncheckedThumbColor = colors.switchUncheckedThumbColor,
                    checkedTrackColor = colors.switchCheckedBackgroundColor,
                    checkedThumbColor = colors.switchCheckedThumbColor,
                )
            )
        }

        Tabs(
            tabs = orderedTabs,
            pagerState = pagerState,
            colors = colors,
        )

        HorizontalPager(state = pagerState) { tabIndex ->
            TabContent(
                screens = screens,
                topic = orderedTabs[tabIndex],
                labels = selectedLabels.takeIf { showLabels },
                filter = filterText,
                onItemClick = onItemClick,
                onInfoClick = { infoScreen = it },
                colors = colors,
            )
        }

        infoScreen?.let { screen ->
            ExampleInfoSheet(
                screen = screen,
                showLabel = showLabels,
                onOpen = {
                    infoScreen = null
                    onItemClick(screen.example.id)
                },
                onDismiss = { infoScreen = null },
            )
        }

        if (showLabelsInfo) {
            LabelsInfoSheet(labels = labels, onDismiss = { showLabelsInfo = false })
        }

        if (showKeepMapInfo) {
            InfoSheet(
                title = stringResource(R.string.compose_catalog_keep_map),
                onDismiss = { showKeepMapInfo = false },
            ) {
                Text(
                    text = stringResource(R.string.compose_catalog_keep_map_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun FilterTextField(
    text: String,
    onTextChange: (String) -> Unit,
    colors: HomeScreenColors,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            // The top padding is set automatically by the label.
            .padding(bottom = 8.dp),
        label = { Text(text = stringResource(R.string.example_filter)) },
        value = text,
        onValueChange = onTextChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.tabIndicatorColor,
            focusedLabelColor = colors.tabIndicatorColor,
            focusedTextColor = colors.textColor,
            cursorColor = colors.tabIndicatorColor,
        )
    )
}

@Composable
private fun LabelFilter(
    labels: List<ComposeExampleLabel>,
    selected: Set<ComposeExampleLabel>,
    onToggle: (ComposeExampleLabel) -> Unit,
    onInfoClick: () -> Unit,
    colors: HomeScreenColors,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEach { label ->
            FilterChip(
                selected = label in selected,
                onClick = { onToggle(label) },
                label = { Text(text = label.name) },
                leadingIcon = { Icon(imageVector = label.icon, contentDescription = null) },
            )
        }
        InfoButton(
            contentDescription = stringResource(R.string.compose_catalog_about_labels),
            onClick = onInfoClick,
            colors = colors,
        )
    }
}

/** Explains the labels of the examples. */
@Composable
private fun LabelsInfoSheet(labels: List<ComposeExampleLabel>, onDismiss: () -> Unit) {
    InfoSheet(title = stringResource(R.string.compose_catalog_labels_title), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.compose_catalog_labels_description),
            style = MaterialTheme.typography.bodyMedium,
        )
        labels.forEach { label ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = label.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column {
                    Text(
                        text = label.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = stringResource(label.description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun Tabs(
    tabs: List<ComposeExampleTopic>,
    pagerState: PagerState,
    colors: HomeScreenColors,
) {
    val scope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = colors.backgroundColor,
        indicator = {
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(it[pagerState.currentPage])
                    .height(3.dp)
                    .background(color = colors.tabIndicatorColor)
            )
        }
    ) {
        tabs.forEachIndexed { index, topic ->
            Tab(
                modifier = Modifier.fillMaxSize(),
                text = {
                    Text(
                        text = tabTitle(topic),
                        color = colors.tabTextColor,
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                selectedContentColor = colors.tabIndicatorColor,
            )
        }
    }
}

@Composable
private fun TabContent(
    screens: List<CatalogEntry>,
    topic: ComposeExampleTopic,
    labels: Set<ComposeExampleLabel>?,
    filter: String,
    onItemClick: (String) -> Unit,
    onInfoClick: (CatalogEntry) -> Unit,
    colors: HomeScreenColors,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        filterScreens(screens, topic, labels, filter).forEach {
            Item(it, showLabel = labels != null, onItemClick, onInfoClick, colors)
        }
    }
}

@Composable
private fun Item(
    screen: CatalogEntry,
    showLabel: Boolean,
    onItemClick: (String) -> Unit,
    onInfoClick: (CatalogEntry) -> Unit,
    colors: HomeScreenColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(screen.example.id) }
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = screen.example.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colors.textColor,
            )
            Text(
                text = stringResource(screen.example.summary),
                fontSize = 14.sp,
                color = colors.textColor.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        InfoButton(
            contentDescription = stringResource(R.string.compose_catalog_about_example, screen.example.title),
            onClick = { onInfoClick(screen) },
            colors = colors,
        )
        val label = screen.label
        if (showLabel && label != null) {
            // A secondary mark: small and muted, so the title stays the main thing in the row.
            Icon(
                imageVector = label.icon,
                contentDescription = label.name,
                modifier = Modifier.size(18.dp),
                tint = colors.textColor.copy(alpha = 0.4f),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

/** The description of an example, with a button to open it. */
@Composable
private fun ExampleInfoSheet(
    screen: CatalogEntry,
    showLabel: Boolean,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
) {
    InfoSheet(title = screen.example.title, onDismiss = onDismiss) {
        val label = screen.label
        if (showLabel && label != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = label.icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = label.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = stringResource(screen.example.description),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onOpen,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(stringResource(R.string.compose_catalog_open))
        }
    }
}

/** A bottom sheet with a title and an explanation below it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            content()
        }
    }
}

/** Opens an explanation; the tonal circle marks it as a button, unlike flat icons next to it. */
@Composable
private fun InfoButton(
    contentDescription: String,
    onClick: () -> Unit,
    colors: HomeScreenColors,
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = colors.textColor.copy(alpha = 0.06f), shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                tint = colors.textColor.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun tabTitle(topic: ComposeExampleTopic) =
    when (topic) {
        ComposeExampleTopic.Map -> stringResource(R.string.example_tabs_map)
        ComposeExampleTopic.Navigation -> stringResource(R.string.example_tabs_navi)
        ComposeExampleTopic.Directory -> stringResource(R.string.example_tabs_directory)
    }

private fun filterScreens(
    screens: List<CatalogEntry>,
    topic: ComposeExampleTopic,
    labels: Set<ComposeExampleLabel>?,
    filter: String,
): List<CatalogEntry> {
    val normalizedFilter = filter.lowercase()
    return screens
        .filter {
            it.example.topic == topic &&
                (labels == null || it.label == null || it.label in labels) &&
                it.example.title.lowercase().contains(normalizedFilter)
        }
        .sortedBy { it.example.title }
}

@Preview
@Composable
private fun HomeScreenDayPreview() =
    HomeScreenColored(HomeScreenDefaults.lightColors)

@Preview
@Composable
private fun HomeScreenNightPreview() =
    HomeScreenColored(HomeScreenDefaults.darkColors)

@Composable
private fun HomeScreenColored(colors: HomeScreenColors) =
    HomeScreen(
        screens = emptyList(),
        onItemClick = {},
        keepMapState = false,
        onKeepMapStateChanged = {},
        colors = colors,
    )
