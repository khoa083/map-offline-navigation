package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kblack.offlinemap.models.GeoCoordinate
import com.kblack.offlinemap.models.Route
import com.kblack.offlinemap.models.TravelMode
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson

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

@Stable
data class AnimatedRouteLine(
    val geoJson: String?,
    val isDashLine: Boolean,
)

/**
 * Progressively reveals [route] as a GeoJSON line, and reports whether it should be
 * rendered dashed (foot travel mode).
 *
 * Perf note: the previous implementation drove the reveal off an [androidx.compose.animation.core.Animatable]
 * whose value changes every choreographer frame (~60/s). Each frame re-sliced the full point list and
 * re-serialized it with `LineString.toJson()`, and MapLibre re-parses that GeoJSON on the native side
 * every time the source data changes -- this is what caused the visible stutter while a route was being
 * drawn, independent of route length. Here the reveal is stepped explicitly to a fixed number of updates
 * ([ROUTE_REVEAL_STEPS]) spread over [ROUTE_REVEAL_DURATION_MS], so the number of expensive
 * JSON rebuilds is bounded regardless of how many points the route has.
 */
@Composable
fun rememberAnimatedRouteLine(
    route: Route?,
    travelMode: TravelMode,
): AnimatedRouteLine {
    val routePoints = remember(route) {
        mutableStateListOf<GeoCoordinate>().apply { addAll(route?.points.orEmpty()) }
    }
    val routeCoords = remember(routePoints) {
        routePoints.map { Position(longitude = it.longitude, latitude = it.latitude) }
    }

    var revealCount by remember(routeCoords) {
        mutableIntStateOf(if (routeCoords.size >= 2) 1 else 0)
    }

    LaunchedEffect(routeCoords) {
        if (routeCoords.size < 2) return@LaunchedEffect
        if (routeCoords.size > ROUTE_REVEAL_MAX_ANIMATED_POINTS) {
            revealCount = routeCoords.size
            return@LaunchedEffect
        }
        val stepDelayMs = ROUTE_REVEAL_DURATION_MS / ROUTE_REVEAL_STEPS
        for (step in 1..ROUTE_REVEAL_STEPS) {
            val fraction = step / ROUTE_REVEAL_STEPS.toFloat()
            revealCount = (((routeCoords.size - 1) * fraction).toInt()).coerceAtLeast(1) + 1
            if (step != ROUTE_REVEAL_STEPS) delay(stepDelayMs)
        }
        revealCount = routeCoords.size
    }

    val geoJson = remember(routeCoords, revealCount) {
        if (revealCount < 2) null
        else Feature<LineString, JsonObject?>(
            geometry = LineString(routeCoords.take(revealCount)),
            properties = null,
        ).toJson()
    }

    return AnimatedRouteLine(
        geoJson = geoJson,
        isDashLine = travelMode == TravelMode.Foot,
    )
}
