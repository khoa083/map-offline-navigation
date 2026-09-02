package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.LineString
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import org.maplibre.spatialk.turf.transformation.simplify

/** Total wall-clock time over which the route line is drawn from start to end. */
private const val ROUTE_REVEAL_DURATION_MS = 700

/**
 * Hard cap on the number of points ever pushed to MapLibre for the animated route line,
 * regardless of how many raw points GraphHopper returned. This is what makes a real per-frame
 * reveal ([Animatable], driven by the Compose frame clock -- smooth, and correctly bounded to the
 * device's actual frame rate) safe to run again: a raw on-device GraphHopper route easily has
 * several thousand points, and re-serializing + re-parsing a GeoJSON that size on the native side
 * 60 times a second is real, measurable lag -- NOT the animation itself. Bounding the point count
 * here keeps every per-frame rebuild cheap no matter how long or dense the real route is, so nothing
 * ever needs to fall back to an instant, un-animated full-line draw.
 */
private const val ROUTE_RENDER_MAX_POINTS = 400

/**
 * [simplify] tolerance in decimal degrees (~5m at the equator) -- visually indistinguishable from
 * the raw geometry at any zoom a phone screen renders at. Run first; [decimateTo] below is only
 * the hard-cap safety net for geometries (lots of sharp turns in a short span) that simplify alone
 * doesn't reduce enough to satisfy [ROUTE_RENDER_MAX_POINTS].
 */
private const val ROUTE_SIMPLIFY_TOLERANCE = 0.00005

@Stable
data class AnimatedRouteLine(
    val geoJson: String?,
    val isDashLine: Boolean,
)

/**
 * Uniformly keeps at most [maxPoints] of [points], always including the first and last point.
 * Pure safety net for geometries [simplify] doesn't reduce enough on its own -- guarantees the
 * hard cap regardless of route shape.
 */
private fun decimateTo(points: List<Position>, maxPoints: Int): List<Position> {
    if (points.size <= maxPoints) return points
    val stride = (points.size - 1).toDouble() / (maxPoints - 1)
    return List(maxPoints) { i -> points[(i * stride).toInt().coerceAtMost(points.size - 1)] }
}

/**
 * Progressively reveals [route] as a GeoJSON line, and reports whether it should be
 * rendered dashed (foot travel mode).
 *
 * History of the "vẽ route bị lag / không smooth" reports, in order:
 *
 * 1. The very first implementation drove the reveal off an [Animatable] over the FULL, raw
 *    (un-simplified, un-capped) point list. Smooth, but every frame re-serialized and pushed the
 *    complete route to the native GeoJSON source -- for a dense multi-thousand-point route, that
 *    is real lag, worse the longer the route.
 * 2. That was "fixed" by skipping the frame-based animation for anything over 300 raw points and
 *    drawing those in one shot instead -- which killed the lag, but ALSO killed the animation for
 *    almost every real driving route (nearly all of them exceed 300 raw points), which is the
 *    "nháy 1 phát, không có anim nối giữa 2 điểm" complaint.
 * 3. The real fix is here: cap the number of points ever handed to MapLibre to
 *    [ROUTE_RENDER_MAX_POINTS] via [simplify] + [decimateTo] -- once, off the main thread, when
 *    [route] changes -- and THEN run a true per-frame [Animatable] reveal over that bounded point
 *    set. Every route, however dense, animates smoothly, and every per-frame rebuild stays cheap
 *    because the point count is always bounded.
 */
@Composable
fun rememberAnimatedRouteLine(
    route: Route?,
    travelMode: TravelMode,
): AnimatedRouteLine {
    var renderCoords by remember { mutableStateOf<List<Position>?>(null) }
    val revealProgress = remember { Animatable(0f) }

    LaunchedEffect(route) {
        revealProgress.snapTo(0f)

        if (route == null || route.points.size < 2) {
            renderCoords = null
            return@LaunchedEffect
        }

        // Coordinate mapping, simplification and decimation all happen off the main thread. This
        // only affects how the line is *drawn* -- Route.points itself (turn instructions,
        // distance, etc.) is never touched.
        val coords = withContext(Dispatchers.Default) {
            val rawPositions = route.points.map {
                Position(longitude = it.longitude, latitude = it.latitude)
            }
            val simplified = if (rawPositions.size > ROUTE_RENDER_MAX_POINTS) {
                LineString(rawPositions)
                    .simplify(tolerance = ROUTE_SIMPLIFY_TOLERANCE, highestQuality = false)
                    .coordinates
            } else {
                rawPositions
            }
            decimateTo(simplified, ROUTE_RENDER_MAX_POINTS)
        }

        renderCoords = coords
        if (coords.size < 2) return@LaunchedEffect

        // Real per-frame reveal, synced to the device's actual frame rate via the Compose frame
        // clock -- safe now that `coords` is always bounded to ROUTE_RENDER_MAX_POINTS.
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = ROUTE_REVEAL_DURATION_MS, easing = LinearEasing),
        )
    }

    val coords = renderCoords
    val geoJson = remember(coords, revealProgress.value) {
        if (coords == null || coords.size < 2) return@remember null
        val revealCount = (1 + (revealProgress.value * (coords.size - 1)).toInt())
            .coerceIn(2, coords.size)
        Feature<LineString, JsonObject?>(
            geometry = LineString(coords.take(revealCount)),
            properties = null,
        ).toJson()
    }

    return AnimatedRouteLine(
        geoJson = geoJson,
        isDashLine = travelMode == TravelMode.Foot,
    )
}
