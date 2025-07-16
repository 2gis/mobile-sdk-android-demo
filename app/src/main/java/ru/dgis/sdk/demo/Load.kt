package ru.dgis.sdk.demo

import android.util.Log
import java.io.InputStreamReader

fun loadJsonFromAssets(fileName: String): String? {
    return try {
        val inputStream = Application.instance.applicationContext.assets.open(fileName)
        val reader = InputStreamReader(inputStream)
        reader.readText()
    } catch (e: Exception) {
        Log.e("APP", "Error reading JSON file", e)
        null
    }
}
