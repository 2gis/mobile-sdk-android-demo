package ru.dgis.sdk.demo.compose.examples.searchitem

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val SearchItemExample = ComposeExample(
    id = "search_item",
    title = "Search Item",
    summary = R.string.compose_example_search_item_summary,
    description = R.string.compose_example_search_item_description,
    topic = ComposeExampleTopic.Map
) {
    SearchItemScreen(mapViewModel, sdkContext, mapOptions)
}
