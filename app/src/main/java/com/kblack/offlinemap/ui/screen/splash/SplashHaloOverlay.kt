package com.kblack.offlinemap.ui.screen.splash

import android.os.SystemClock
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.R
import kotlinx.coroutines.delay

// Design canvas turn 11c, "Real-init progress": halo loop 0-900ms, standard-decelerate
// cubic-bezier(0,0,0,1), one halo emitted every 450ms -- two rings staggered by that
// offset reproduces "two halos in flight" once the loop has been running a moment.
private const val HALO_CYCLE_MS = 900
private const val HALO_STAGGER_MS = 450
private val HaloEasing = CubicBezierEasing(0f, 0f, 0f, 1f)

// Only show the halo once init has genuinely run past ~1.2s (design: "so a fast device
// never sees it and a slow one never sees a bare repeating draw with no acknowledgement").
private const val HALO_THRESHOLD_MS = 1200L

// Only fade in the explanatory text past 2.5s -- "an explanation for an unusually slow
// first run, not a permanent loading label" (design canvas turn 11c).
private const val TEXT_THRESHOLD_MS = 2500L
private const val TEXT_FADE_IN_MS = 200

private const val HALO_MIN_SCALE = 1.0f
private const val HALO_MAX_SCALE = 1.7f
private const val HALO_START_ALPHA = 0.5f
private const val HALO_BASE_RADIUS_DP = 28f

/**
 * Conditional "still working" affordance for a slow cold start, per design canvas turn
 * 11d's recommendation: "Ship 11b [the route-draws-itself splash], with 11c's halo as a
 * conditional layer."
 *
 * Why this is plain Compose animation, not a second AnimatedVectorDrawable in the OS
 * splash theme: androidx.core.splashscreen cannot play AVD animation at all on API < 31
 * (see the comments in res/drawable-v31/avd_launcher_route.xml) -- a second AVD halo would
 * ALSO be static-only on those devices, which defeats a "still working" signal entirely.
 * Compose animation has no such platform restriction on any API level this app supports
 * (minSdk 26), so real motion for a slow init lives here, gated purely on elapsed time --
 * never on API level. This is not a second splash screen: it is only ever visible while
 * [isStillLoading] is true, which is exactly the same window the OS SplashScreen's
 * keep-on-screen condition (in MainActivity.kt) is already holding the platform splash
 * open for -- by the time isStillLoading flips to false, the OS splash's own exit
 * animation is what takes over, and this overlay simply disappears.
 *
 * @param isStillLoading mirrors HomeViewModel.uiState.value.loadingMapAllowlist. The halo
 *   and text both disappear the instant this flips to false -- this overlay never delays
 *   anything on its own, matching the design's "the splash lasts exactly as long as the
 *   app genuinely is not ready" rule.
 * @param coldStartElapsedRealtimeMs the value of SystemClock.elapsedRealtime() captured at
 *   the very start of MainActivity.onCreate(), so "how long has init actually taken" is
 *   measured from cold start, not from whenever this composable happens to enter.
 */
@Composable
fun SplashHaloOverlay(
    isStillLoading: Boolean,
    coldStartElapsedRealtimeMs: Long,
    modifier: Modifier = Modifier,
) {
    if (!isStillLoading) return

    var showHalo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val haloDelayMs = HALO_THRESHOLD_MS - (SystemClock.elapsedRealtime() - coldStartElapsedRealtimeMs)
        if (haloDelayMs > 0) delay(haloDelayMs)
        showHalo = true

        val textDelayMs = TEXT_THRESHOLD_MS - (SystemClock.elapsedRealtime() - coldStartElapsedRealtimeMs)
        if (textDelayMs > 0) delay(textDelayMs)
        showText = true
    }

    if (!showHalo) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        HaloRings()

        if (showText) {
            val textAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = TEXT_FADE_IN_MS),
                label = "splash_text_alpha",
            )
            Text(
                text = stringResource(R.string.splash_preparing_offline_maps),
                color = Color.White,
                modifier = Modifier
                    .padding(top = 96.dp)
                    .alpha(textAlpha),
            )
        }
    }
}

@Composable
private fun HaloRings(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "splash_halo")

    val ring1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = HALO_CYCLE_MS, easing = HaloEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "splash_halo_ring_1",
    )
    val ring2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = HALO_CYCLE_MS, easing = HaloEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(HALO_STAGGER_MS, StartOffsetType.FastForward),
        ),
        label = "splash_halo_ring_2",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseRadiusPx = HALO_BASE_RADIUS_DP.dp.toPx()
        for (progress in listOf(ring1, ring2)) {
            val scale = HALO_MIN_SCALE + (HALO_MAX_SCALE - HALO_MIN_SCALE) * progress
            val alpha = HALO_START_ALPHA * (1f - progress)
            if (alpha <= 0f) continue
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = baseRadiusPx * scale,
                center = center,
            )
        }
    }
}
