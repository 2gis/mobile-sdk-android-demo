package ru.dgis.sdk.demo

import ru.dgis.sdk.DGis
import ru.dgis.sdk.Context
import ru.dgis.sdk.LogLevel
import ru.dgis.sdk.LogOptions

fun initializeDGis(appContext: android.content.Context): Context {
    return DGis.initialize(
        appContext,
        logOptions = LogOptions(
            customLevel = LogLevel.WARNING,
            customSink = createLogSink()
        )
    )
}
