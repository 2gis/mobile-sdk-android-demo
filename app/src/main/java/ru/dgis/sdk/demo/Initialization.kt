package ru.dgis.sdk.demo

import ru.dgis.sdk.DGis
import ru.dgis.sdk.ApiKeys
import ru.dgis.sdk.Context
import ru.dgis.sdk.LogLevel
import ru.dgis.sdk.LogOptions

fun initializeDGis(appContext: android.content.Context): Context {
    val key = { id: Int -> String
        appContext.resources.getString(id)
    }
    return DGis.initialize(
        appContext, ApiKeys(
            directory = key(R.string.dgis_directory_api_key),
            map = key(R.string.dgis_map_api_key)
        ),
        logOptions = LogOptions(
            customLevel = LogLevel.WARNING,
            customSink = createLogSink()
        )
    )
}
