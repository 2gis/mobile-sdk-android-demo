package ru.dgis.sdk.demo.compose.examples.routeEditor

import ru.dgis.sdk.await
import ru.dgis.sdk.directory.SearchManager
import ru.dgis.sdk.routing.RouteSearchPoint

/**
 * Sample implementation of [RoutePointTitleProvider] that fetches titles from the directory service.
 *
 * This provider works when [RouteSearchPoint] has the `objectId` property set (non-zero value).
 * It searches the directory using [SearchManager.searchByIds] to retrieve the object's title.
 *
 * Fallback behavior:
 * - For start and destination points: falls back to coordinate-based title format if objectId is 0
 *   or if the directory search fails/returns no result
 * - For intermediate points: falls back to "Waypoint N" format (where N is the point's index + 1)
 *   if objectId is 0 or if the directory search fails/returns no result
 */
class DirectoryRoutePointTitleProvider(
    private val searchManager: SearchManager,
) {
    suspend fun providePointTitle(point: RouteSearchPoint): String =
        fetchTitleOrFallback(point, point.toCoordinateTitle())

    suspend fun provideIntermediatePointTitle(
        point: RouteSearchPoint,
        index: Int,
    ): String = fetchTitleOrFallback(point, "Waypoint ${index + 1}")

    private suspend fun fetchTitleOrFallback(
        point: RouteSearchPoint,
        fallback: String,
    ): String {
        if (point.objectId.objectId == 0L) {
            return fallback
        }

        return try {
            searchManager.searchByIds(listOf(point.objectId.objectId.toString())).await()
                .firstOrNull()?.title
                ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }
}

private fun RouteSearchPoint.toCoordinateTitle(): String =
    "${coordinates.latitude.value.toString().take(8)}, ${
        coordinates.longitude.value.toString().take(8)
    }"
