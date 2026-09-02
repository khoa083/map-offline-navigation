package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.maplibre.compose.location.Location
import org.maplibre.spatialk.geojson.Position

/**
 * Matches [org.maplibre.compose.location.LocationRequest]'s default `minimumInterval` (1 second)
 * -- see `rememberMapLocationState` in `MapLocationAccess.kt` -- and the `animationDuration = 1
 * .seconds` already used for camera-follow in `MapCameraEffects.kt`. Raw GPS fixes only arrive
 * about once a second either way; this keeps the puck's own glide in lockstep with both.
 */
private const val PUCK_ANIM_DURATION_MS = 1000

/**
 * Smooths a raw, snapping [Location] (from `LocationState.location`) into a continuously animated
 * one, so `LocationPuck` glides between fixes instead of jumping.
 *
 * Root cause of the "vị trí bị giật / nhảy chỗ khác khi di chuyển" report:
 * [org.maplibre.compose.location.Location] carries only a single [position][Location.position] --
 * the library's `LocationPuck` renders that position directly, with no interpolation of its own
 * (confirmed against the actual v0.15.0 source: `Location` is a plain immutable data class, and
 * every new value the puck receives is rendered immediately). Real fixes only arrive ~once a
 * second ([PUCK_ANIM_DURATION_MS], matching `LocationRequest`'s default `minimumInterval`), so
 * without this the dot sits still for ~1s and then instantly jumps the whole distance travelled in
 * that second. It's worse than it sounds: `MapCameraEffects`'s `LocationTrackingEffect` already
 * glides the CAMERA smoothly toward each new fix over that same second (see the comment there), so
 * the raw, snapping puck and the smoothly-panning camera visibly fight each other -- right as a fix
 * arrives the dot jumps to its true new coordinate while the camera hasn't caught up yet, then the
 * camera drags the view back under it over the next second. That fight between a stepped puck and a
 * smooth camera is what reads as stutter/teleport while moving.
 *
 * Fix mirrors `rememberAnimatedRouteLine`'s approach for the route line: linearly interpolate
 * ([LinearEasing], so speed reads as constant rather than easing in/out) between the last two
 * fixes' positions over [PUCK_ANIM_DURATION_MS], driven by the real Compose frame clock via
 * [Animatable]. Only [Location.position] is interpolated -- [Location.course]/`speed`/
 * `altitudeAccuracy`/`timestamp` always reflect the latest real fix; interpolating those has no
 * visual benefit, and `timestamp` in particular must stay real for `LocationPuck`'s own
 * "old location" (`oldLocationThreshold`) styling to work correctly.
 *
 * Interruption-safe: if a new fix arrives before the previous glide finishes, the new segment
 * starts from wherever the dot actually is on screen at that instant (computed from the in-flight
 * progress), never from the old target -- otherwise an early fix would itself cause a visible
 * snap-back-then-continue.
 */
@Composable
fun rememberAnimatedPuckLocation(location: Location?): Location? {
    var from by remember { mutableStateOf<Location?>(null) }
    var to by remember { mutableStateOf<Location?>(null) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(location) {
        val target = location ?: return@LaunchedEffect
        val prevFrom = from
        val prevTo = to

        if (prevFrom == null || prevTo == null) {
            // First fix ever received -- nothing to glide from, show it immediately.
            from = target
            to = target
            progress.snapTo(1f)
        } else {
            // Anchor the new glide at the dot's *actual current* interpolated position, not at
            // prevTo, in case this fix interrupted an in-progress glide.
            val t = progress.value.toDouble()
            val fromPos = prevFrom.position.value
            val toPos = prevTo.position.value
            val currentPosition = Position(
                longitude = fromPos.longitude + (toPos.longitude - fromPos.longitude) * t,
                latitude = fromPos.latitude + (toPos.latitude - fromPos.latitude) * t,
            )
            from = prevTo.copy(position = prevTo.position.copy(value = currentPosition))
            to = target
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = PUCK_ANIM_DURATION_MS, easing = LinearEasing),
            )
        }
    }

    val start = from
    val end = to ?: return null
    if (start == null) return end

    val t = progress.value.toDouble()
    return remember(start, end, progress.value) {
        val startPos = start.position.value
        val endPos = end.position.value
        end.copy(
            position = end.position.copy(
                value = Position(
                    longitude = startPos.longitude + (endPos.longitude - startPos.longitude) * t,
                    latitude = startPos.latitude + (endPos.latitude - startPos.latitude) * t,
                ),
            ),
        )
    }
}
