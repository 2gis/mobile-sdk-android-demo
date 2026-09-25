package ru.dgis.sdk.demo.compose.examples.controls

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val ControlsExample = ComposeExample(
    id = "controls",
    title = "Map Controls",
    summary = R.string.compose_example_controls_summary,
    description = R.string.compose_example_controls_description,
    topic = ComposeExampleTopic.Map,
) {
    ControlsScreen(mapViewModel, mapOptions)
}
