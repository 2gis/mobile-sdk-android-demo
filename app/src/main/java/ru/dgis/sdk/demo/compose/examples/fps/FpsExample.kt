package ru.dgis.sdk.demo.compose.examples.fps

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val FpsExample = ComposeExample(
    id = "fps",
    title = "Fps",
    summary = R.string.compose_example_fps_summary,
    description = R.string.compose_example_fps_description,
    topic = ComposeExampleTopic.Map,
) {
    FpsScreen(mapViewModel, mapOptions)
}
