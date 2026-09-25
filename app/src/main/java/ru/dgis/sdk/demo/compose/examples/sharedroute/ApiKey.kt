package ru.dgis.sdk.demo.compose.examples.sharedroute

import org.json.JSONObject
import java.io.InputStream
import android.content.Context as AndroidContext

private const val KEY_ASSET_NAME = "dgissdk.key"

/** Reads the API key from the key file of the SDK in the assets of the app. */
fun readApiKeyFromAsset(appContext: AndroidContext): String {
    return appContext.assets.open(KEY_ASSET_NAME).use { stream: InputStream ->
        val raw = stream.bufferedReader(Charsets.UTF_8).readText()
        val jsonStart = raw.indexOf('{')
        require(jsonStart >= 0) { "No JSON object in $KEY_ASSET_NAME" }
        val json = raw.substring(jsonStart)
        JSONObject(json).optString("key").ifBlank {
            throw IllegalStateException("The \"key\" field in $KEY_ASSET_NAME is empty")
        }
    }
}
