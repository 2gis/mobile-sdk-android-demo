package ru.dgis.sdk.demo.compose.examples.objects

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val ObjectsExample = ComposeExample(
    id = "objects",
    title = "Objects",
    summary = R.string.compose_example_objects_summary,
    description = R.string.compose_example_objects_description,
    topic = ComposeExampleTopic.Map
) {
    ObjectsScreen(mapViewModel, mapOptions)
}
