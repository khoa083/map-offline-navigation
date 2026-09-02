package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kblack.offlinemap.models.Route
import com.kblack.offlinemap.models.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import org.maplibre.spatialk.turf.transformation.simplify

/** Number of discrete redraws used to reveal the route line. */
private const val ROUTE_REVEAL_STEPS = 12

/** Total wall-clock time over which the route line is revealed. */
private const val ROUTE_REVEAL_DURATION_MS = 500L

/**
 * Routes with more points than this skip the step animation entirely and are drawn in one shot.
 * Each step re-serializes the (sliced) line and forces MapLibre to re-parse the GeoJSON source on
 * the native side, so animating a very long route means many expensive re-parses of an
 * increasingly large payload -- the perceived lag reported for long routes came from this.
 */
private const val ROUTE_REVEAL_MAX_ANIMATED_POINTS = 300

/**
 * Raw GraphHopper route geometry (decoded 1:1 from the on-device routing engine's `PointList` in
 * [com.kblack.offlinemap.data.repository.RoutingRepository], with no simplification) routinely
 * has several thousand points for anything beyond a short trip. Above this count, the line is
 * simplified with [simplify] before it's ever turned into a [Position] list -- simplifying only
 * changes how the line is *drawn*, never [Route.points] itself (turn instructions, distance,
 * etc. all keep using the untouched route).
 */
private const val ROUTE_SIMPLIFY_ABOVE_POINTS = 500

/**
 * Simplify tolerance in decimal degrees (~5m at the equator). Visually indistinguishable from the
 * raw geometry at any zoom level a phone screen renders at, while cutting a dense GraphHopper
 * geometry down to a small fraction of its raw point count.
 */
private const val ROUTE_SIMPLIFY_TOLERANCE = 0.00005

@Stable
data class AnimatedRouteLine(
    val geoJson: String?,
    val isDashLine: Boolean,
)

/**
 * Progressively reveals [route] as a GeoJSON line, and reports whether it should be
 * rendered dashed (foot travel mode).
 *
 * Perf notes (both were real, both were the "vẽ route bị lag" symptom, from two different causes):
 *
 * 1. The original implementation drove the reveal off an
 *    [androidx.compose.animation.core.Animatable] whose value changes every choreographer frame
 *    (~60/s). Each frame re-sliced the full point list and re-serialized it with
 *    `LineString.toJson()`, and MapLibre re-parses that GeoJSON on the native side every time the
 *    source data changes. Fixed by stepping the reveal to a fixed, small number of updates
 *    ([ROUTE_REVEAL_STEPS]) spread over [ROUTE_REVEAL_DURATION_MS], so the number of expensive
 *    native re-parses is bounded regardless of how many points the route has.
 *
 * 2. Separately -- and this is the part fix #1 did NOT cover -- building the [Position] list and
 *    calling `LineString(...).toJson()` used to happen inline inside `remember { ... }`, i.e.
 *    synchronously during Compose composition on the main thread. A raw (unsimplified) GraphHopper
 *    route easily has several thousand points; serializing all of them synchronously blocks the UI
 *    thread for the duration, which reads as a stutter/freeze right when a route is drawn --
 *    independent of the reveal-step optimization above, and worse the longer/denser the route.
 *    Fixed by (a) simplifying routes over [ROUTE_SIMPLIFY_ABOVE_POINTS] points with
 *    [org.maplibre.spatialk.turf.transformation.simplify] before doing anything else with them,
 *    and (b) moving the coordinate mapping + JSON serialization into a [Dispatchers.Default]
 *    coroutine via [LaunchedEffect] instead of a synchronous `remember` block, so composition
 *    itself never blocks on it.
 */
@Composable
fun rememberAnimatedRouteLine(
    route: Route?,
    travelMode: TravelMode,
): AnimatedRouteLine {
    var geoJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(route) {
        if (route == null || route.points.size < 2) {
            geoJson = null
            return@LaunchedEffect
        }

        // Coordinate mapping, simplification and JSON serialization all happen off the main
        // thread -- see perf note #2 above. Nothing here touches Route.points itself, only this
        // local rendering-only copy.
        val routeCoords = withContext(Dispatchers.Default) {
            val rawPositions = route.points.map {
                Position(longitude = it.longitude, latitude = it.latitude)
            }
            if (rawPositions.size > ROUTE_SIMPLIFY_ABOVE_POINTS) {
                LineString(rawPositions)
                    .simplify(tolerance = ROUTE_SIMPLIFY_TOLERANCE, highestQuality = false)
                    .coordinates
            } else {
                rawPositions
            }
        }

        if (routeCoords.size < 2) {
            geoJson = null
            return@LaunchedEffect
        }

        suspend fun buildJson(count: Int): String = withContext(Dispatchers.Default) {
            Feature<LineString, JsonObject?>(
                geometry = LineString(routeCoords.take(count)),
                properties = null,
            ).toJson()
        }

        if (routeCoords.size > ROUTE_REVEAL_MAX_ANIMATED_POINTS) {
            geoJson = buildJson(routeCoords.size)
            return@LaunchedEffect
        }

        val stepDelayMs = ROUTE_REVEAL_DURATION_MS / ROUTE_REVEAL_STEPS
        for (step in 1..ROUTE_REVEAL_STEPS) {
            val fraction = step / ROUTE_REVEAL_STEPS.toFloat()
            val revealCount = (((routeCoords.size - 1) * fraction).toInt()).coerceAtLeast(1) + 1
            geoJson = buildJson(revealCount)
            if (step != ROUTE_REVEAL_STEPS) delay(stepDelayMs)
        }
        geoJson = buildJson(routeCoords.size)
    }

    return AnimatedRouteLine(
        geoJson = geoJson,
        isDashLine = travelMode == TravelMode.Foot,
    )
}
