package ru.dgis.sdk.demo.compose.examples.routeEditor

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import ru.dgis.sdk.await
import ru.dgis.sdk.compose.routeeditor.publictransport.PublicTransportScheduleProvider
import ru.dgis.sdk.compose.routeeditor.publictransport.RouteSchedule
import ru.dgis.sdk.compose.routeeditor.publictransport.TransportSchedule
import ru.dgis.sdk.directory.DirectoryObject
import ru.dgis.sdk.directory.ObjectType
import ru.dgis.sdk.directory.PublicTransportDirectoryScheduleInfo
import ru.dgis.sdk.directory.PublicTransportIntervalSchedule
import ru.dgis.sdk.directory.PublicTransportSchedule
import ru.dgis.sdk.directory.SearchManager
import ru.dgis.sdk.directory.SearchQueryBuilder
import ru.dgis.sdk.geometry.Geometry

/**
 * Implementation of [PublicTransportScheduleProvider] using Directory API.
 *
 * Encapsulates all logic for searching routes by name,
 * loading and parsing schedules from Directory SDK.
 *
 * @param searchManager The [SearchManager] instance used for searching routes and loading schedules.
 */
class DirectoryScheduleProvider(
    private val searchManager: SearchManager
) : PublicTransportScheduleProvider {

    override suspend fun loadSchedule(
        routeName: String,
        areasOfInterest: List<Geometry>,
        currentMillisUTC: Long
    ): RouteSchedule? {
        try {
            val matchingObjectId = findRouteInAreas(routeName, areasOfInterest)?.id ?: return null

            val routeObject = searchManager
                .searchByDirectoryObjectIds(listOf(matchingObjectId))
                .await()
                .firstOrNull() ?: return null

            val scheduleInfo = routeObject
                .publicTransportScheduleInfo(currentMillisUTC / 1000)
                .await() ?: return null

            return extractScheduleFromInfo(scheduleInfo)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.d("DirScheduleProvider", "Schedule loading failed for route '$routeName'", e)
            return null
        }
    }

    /**
     * Searches for a route by its name in the given areas.
     * Several geometries are searched in parallel.
     * Returns the first object found, or null.
     */
    private suspend fun findRouteInAreas(
        routeName: String,
        areasOfInterest: List<Geometry>
    ): DirectoryObject? {
        if (areasOfInterest.isEmpty()) return null

        // Search in batches of 5 geometries in parallel until something is found
        for (batch in areasOfInterest.chunked(5)) {
            val result = coroutineScope {
                val searchDeferreds = batch.map {
                    async {
                        try {
                            val query = SearchQueryBuilder()
                                .setQueryText(routeName)
                                .setAllowedResultTypes(listOf(ObjectType.ROUTE))
                                .setTerritoryOfInterest(it)
                                .build()
                            searchManager.search(query).await()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                for (deferred in searchDeferreds) {
                    val resultItems = deferred.await()?.firstPage?.items

                    val directoryObject = resultItems?.firstOrNull { item ->
                        item.routeInfos.any { it.name == routeName }
                    }

                    if (directoryObject != null) {
                        // Cancel the remaining requests and return the result
                        searchDeferreds.forEach { it.cancel() }
                        return@coroutineScope directoryObject
                    }
                }
                null
            }

            if (result != null) {
                return result
            }
        }
        return null
    }
}

/**
 * Extracts the schedule from [PublicTransportDirectoryScheduleInfo].
 *
 * Returns a [RouteSchedule] with the main schedule and the nearest departures, or null.
 * Priority: interval schedule → nearest trips → full-day platform schedules
 * (buses, trams, etc.).
 */
private fun extractScheduleFromInfo(
    scheduleInfo: PublicTransportDirectoryScheduleInfo
): RouteSchedule? {
    for ((_, routeSchedule) in scheduleInfo.routeSchedules) {
        // 1. Interval schedule (typical for the metro)
        for ((_, intervalSchedule) in routeSchedule.intervalTrips) {
            return RouteSchedule(intervalSchedule.toTransportSchedule())
        }

        // 2. Nearest trips of a periodic schedule
        for ((_, platformTrips) in routeSchedule.nearTrips) {
            for ((_, nearTripSchedule) in platformTrips) {
                nearTripSchedule.period?.let {
                    return RouteSchedule(TransportSchedule.Interval(it.toUByte().toInt()))
                }
            }
        }

        // 3. Full-day platform schedules (buses, trams, trolleybuses)
        for ((_, platformMap) in routeSchedule.fullDayPlatforms) {
            for ((_, scheduleItems) in platformMap) {
                // Check for an interval schedule first
                for (item in scheduleItems) {
                    item.schedule.asIntervalSchedule?.let {
                        if (it.period > 0) {
                            return RouteSchedule(it.toTransportSchedule())
                        }
                    }
                }

                // Collect the exact departure times
                val preciseDepartures = scheduleItems
                    .filter { it.schedule.asPreciseSchedule != null }
                    .map { it.schedule.toTransportSchedule() }

                if (preciseDepartures.isNotEmpty()) {
                    return RouteSchedule(
                        schedule = preciseDepartures.first(),
                        nextDepartures = preciseDepartures
                    )
                }
            }
        }
    }
    return null
}

private fun PublicTransportSchedule.toTransportSchedule(): TransportSchedule {
    return match(
        intervalSchedule = {
            it.toTransportSchedule()
        },
        preciseSchedule = {
            TransportSchedule.Precise(
                hours = it.preciseTime.hours.toInt(),
                minutes = it.preciseTime.minutes.toInt()
            )
        }
    )
}

private fun PublicTransportIntervalSchedule.toTransportSchedule(): TransportSchedule {
    val periodMin = period.toUByte().toInt()
    return if (periodMin > 0) TransportSchedule.Interval(periodMin) else TransportSchedule.None
}
