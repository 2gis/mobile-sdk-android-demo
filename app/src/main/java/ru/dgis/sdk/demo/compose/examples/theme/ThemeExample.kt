package ru.dgis.sdk.demo.compose.examples.theme

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val ThemeExample = ComposeExample(
    id = "theme",
    title = "Theme",
    summary = R.string.compose_example_theme_summary,
    description = R.string.compose_example_theme_description,
    topic = ComposeExampleTopic.Map,
) {
    ThemeScreen(mapViewModel, mapOptions)
}
