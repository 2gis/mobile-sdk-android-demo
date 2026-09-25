package ru.dgis.sdk.demo.compose.examples.snapshot

import ru.dgis.sdk.demo.R
import ru.dgis.sdk.demo.compose.ComposeExample
import ru.dgis.sdk.demo.compose.ComposeExampleTopic

val SnapshotExample = ComposeExample(
    id = "snapshot",
    title = "Snapshot",
    summary = R.string.compose_example_snapshot_summary,
    description = R.string.compose_example_snapshot_description,
    topic = ComposeExampleTopic.Map,
) {
    SnapshotScreen(mapViewModel, mapOptions)
}
