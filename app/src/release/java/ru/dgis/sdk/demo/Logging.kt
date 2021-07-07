package ru.dgis.sdk.demo

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.dgis.sdk.LogMessage
import ru.dgis.sdk.LogSink

private class LogException(val msg: LogMessage) : RuntimeException(msg.text) {
    override fun getStackTrace(): Array<StackTraceElement> {
        return arrayOf(
            StackTraceElement("", "", msg.file, msg.line)
        )
    }
}

private class FirebaseLogSink : LogSink {
    override fun write(message: LogMessage) {
        FirebaseCrashlytics.getInstance().recordException(LogException(message))
    }
}

@Suppress("RedundantNullableReturnType")
fun createLogSink(): LogSink? {
    return FirebaseLogSink()
}
