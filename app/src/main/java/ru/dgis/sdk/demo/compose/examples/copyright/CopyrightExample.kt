package ru.dgis.sdk.demo.compose.examples.copyright

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val CopyrightExample = ComposeExample(
    id = "copyright",
    title = "Copyright",
    summary = R.string.compose_example_copyright_summary,
    description = R.string.compose_example_copyright_description,
    topic = ComposeExampleTopic.Map,
) {
    CopyrightScreen(mapViewModel, mapOptions)
}
