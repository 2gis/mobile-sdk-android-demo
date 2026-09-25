package ru.dgis.sdk.demo.compose.extra

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import ru.dgis.sdk.Channel

/**
 * Represents channel events as a [Flow]: the channel connection is established when
 * collection starts and closed when the coroutine is cancelled.
 *
 * Note for composables: channel property access returns a new wrapper instance on every
 * call, so do not call this in a composable body directly — wrap it in `remember(key)`
 * with a stable external key, or collect it inside a `LaunchedEffect` with such a key.
 */
fun <T> Channel<T>.asFlow(): Flow<T> = callbackFlow {
    val connection = connect { trySend(it) }
    awaitClose { connection.close() }
}
