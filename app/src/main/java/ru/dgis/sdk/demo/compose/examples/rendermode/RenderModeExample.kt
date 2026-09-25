package ru.dgis.sdk.demo.compose.examples.rendermode

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val RenderModeExample = ComposeExample(
    id = "render_mode",
    title = "Render Mode",
    summary = R.string.compose_example_render_mode_summary,
    description = R.string.compose_example_render_mode_description,
    topic = ComposeExampleTopic.Map
) {
    RenderModeScreen(mapViewModel, mapOptions)
}
