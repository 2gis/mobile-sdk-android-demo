package ru.dgis.sdk.demo

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ru.dgis.sdk.context.LogMessage
import ru.dgis.sdk.context.LogSink

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
