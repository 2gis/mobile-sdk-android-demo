package ru.dgis.sdk.demo

import ru.dgis.sdk.DGis
import ru.dgis.sdk.context.ApiKeys
import ru.dgis.sdk.context.Context

fun initializeDGis(appContext: android.content.Context): Context {
    val key = { id: Int -> String
        appContext.resources.getString(id)
    }
    return DGis.initialize(
        appContext, ApiKeys(
            directory = key(R.string.dgis_directory_api_key),
            map = key(R.string.dgis_map_api_key)
        )
    )
}
