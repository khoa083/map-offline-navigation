package com.kblack.offlinemap.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptics — the channel that works when the driver cannot look (spec 1r).
 *
 * Specified rather than left to each call site, because an unstated haptic policy is either
 * invented per screen or forgotten entirely, and both failures are invisible in review.
 *
 * ## Two rules that decide everything else
 *
 * 1. **Haptics are never the only carrier of a state.** Every moment below also has a visible
 *    change, per the standing rule on spec 1q. A vibration adds a second channel; it never
 *    replaces the first. This is what keeps the app usable with system haptics switched off.
 * 2. **Silence is a design decision, not an omission.** Distance ticks, ETA updates and map pans
 *    deliberately produce nothing. A pulse every 10 m would be noise, and noise trains a user to
 *    ignore the channel — at which point the one event that matters (going off route) gets
 *    ignored too.
 *
 * Everything here is gated on the OS touch-feedback preference. Compose's [LocalHapticFeedback]
 * already respects it, which is why this file goes through that rather than driving a Vibrator
 * directly: a custom vibration path would bypass the user's own setting.
 *
 * Ordinary control presses — buttons, switches, list rows, segmented controls, the map cluster —
 * use the platform default via `LocalHapticFeedback` at the call site. No custom curves, no long
 * buzzes, nothing routed through this object.
 */
object Haptics {

    /**
     * Approaching a maneuver, fired 150 m out, once per maneuver.
     *
     * Fires at the same moment the turn card promotes the distance, so the pulse and the visual
     * change are one event rather than two. Once per maneuver — re-firing on distance ticks is
     * the noise failure described in the class docs.
     */
    fun approachingManeuver(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    /**
     * Approaching a sharp turn or U-turn, 150 m out. A double pulse — the only doubled effect in
     * the app.
     *
     * Reserved for exactly the maneuvers whose amber container already means "slow down", so the
     * doubling reinforces a warning the eye may already have caught rather than introducing an
     * unexplained new signal. Call this *instead of* [approachingManeuver], not after it.
     *
     * The caller is responsible for the gap between the two pulses; keep it short (~120 ms) so
     * the pair reads as one doubled effect rather than two separate maneuvers.
     */
    fun approachingSharpManeuver(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    /**
     * Going off route. Fires together with the off-route banner.
     *
     * [HapticFeedbackType.Reject] rather than Confirm on purpose: this is the one event a driver
     * must notice without looking, and it is the only negative event in the set. Using the same
     * effect as every other moment would make it indistinguishable from a normal maneuver cue.
     */
    fun offRoute(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Reject)
    }

    /**
     * Back on route.
     *
     * Closes the loop opened by [offRoute]. Without it, the absence of a second buzz is
     * ambiguous — the driver cannot tell "recovered" from "still off route and the app has
     * stopped telling me".
     */
    fun backOnRoute(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    /** Arrival, with the arrive glyph and the summary sheet. */
    fun arrival(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    /**
     * Download complete — **only while the app is foregrounded.**
     *
     * Backgrounded, the notification channel already carries its own vibration, and firing both
     * produces a double buzz for one event. The caller must check foreground state; this
     * function deliberately does not, because the lifecycle answer lives with the caller
     * (`AppLifecycleProvider`) rather than with the theme layer.
     */
    fun downloadComplete(haptic: HapticFeedback) {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
    }
}

/**
 * Convenience accessor so a call site reads `val haptic = rememberAppHaptics()` and then
 * `Haptics.offRoute(haptic)`, rather than reaching into the composition local inline and
 * obscuring which moment is being fired.
 */
@Composable
fun rememberAppHaptics(): HapticFeedback = LocalHapticFeedback.current
