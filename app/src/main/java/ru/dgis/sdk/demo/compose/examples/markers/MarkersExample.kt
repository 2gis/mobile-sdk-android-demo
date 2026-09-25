package ru.dgis.sdk.demo.compose.examples.markers

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val MarkersExample = ComposeExample(
    id = "markers",
    title = "Markers",
    summary = R.string.compose_example_markers_summary,
    description = R.string.compose_example_markers_description,
    topic = ComposeExampleTopic.Map,
) {
    MarkersScreen(mapViewModel, sdkContext, mapOptions)
}
