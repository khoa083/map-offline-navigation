package com.kblack.offlinemap.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.animation.PathInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.kblack.offlinemap.ui.navigation.MapNavGraph
import com.kblack.offlinemap.ui.screen.splash.SplashHaloOverlay
import com.kblack.offlinemap.ui.viewmodel.HomeViewModel
import com.kblack.offlinemap.ui.theme.OfflinemapTheme
import dagger.hilt.android.AndroidEntryPoint

// Splash colours -- literal values, matching res/values/themes.xml's
// Theme.Offlinemap.Starting. Deliberately NOT read from OfflinemapTheme /
// MaterialTheme.colorScheme: at the moment the OS splash's exit animation runs, Compose
// has not necessarily composed the app theme yet, and the design brief is explicit that
// reaching for a Compose-only token here can render black.
private const val SPLASH_BACKGROUND_COLOR = 0xFF0B4F45.toInt()

// BUGFIX (found via on-device video review): the exit crossfade previously animated to a
// single hardcoded #E4E7DE (the design canvas's light "map surface" assumption), regardless
// of which scheme the app was actually about to show. OfflinemapTheme's darkTheme parameter
// defaults to isSystemInDarkTheme() with no override (see Theme.kt's own doc comment -- the
// old "forced Amoled regardless" bug is already fixed there), so a device in system dark
// mode lands on Color.kt's backgroundAmoled (#000000), not a light background at all. Pixel
// sampling of the reported video confirmed the real Home screen behind the splash was
// near-black (~RGB 24,31,27), matching backgroundAmoled, not the light constant coded here --
// that mismatch is what produced the visible flash/mismatch in the report.
// Values below are Color.kt's backgroundLight / backgroundAmoled, duplicated as literals for
// the same reason SPLASH_BACKGROUND_COLOR is: this runs before Compose has composed
// OfflinemapTheme, so MaterialTheme.colorScheme.background is not safely readable yet.
private const val SPLASH_EXIT_END_COLOR_LIGHT = 0xFFF7F5EC.toInt() // Color.kt: backgroundLight
private const val SPLASH_EXIT_END_COLOR_DARK = 0xFF000000.toInt() // Color.kt: backgroundAmoled

// Exit sequence, design canvas turn 11b: "the road's stroke extends past the icon bounds
// onto the first real map frame" over 420ms, easing emphasized-accelerate
// cubic-bezier(0.3, 0.0, 0.8, 0.15), background crossfading from SPLASH_BACKGROUND_COLOR to
// whichever of SPLASH_EXIT_END_COLOR_LIGHT/_DARK actually matches the app's current scheme
// (see that constant's doc comment). This is the one place the platform SplashScreen API
// leaves motion duration uncapped (unlike the <=1000ms icon loop), so it carries the bigger
// motion moment.
private const val SPLASH_EXIT_DURATION_MS = 420L
private const val SPLASH_EXIT_ICON_SCALE = 1.7f

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()

    // Captured before anything else runs, so SplashHaloOverlay can measure "how long has
    // cold-start init actually taken" from true cold start, not from whenever Compose
    // happens to enter composition.
    private val coldStartElapsedRealtimeMs = SystemClock.elapsedRealtime()

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() MUST be the first call in onCreate(), before
        // super.onCreate() and before any setContentView()/setContent() -- calling it
        // later is one of the documented causes of a blank/white splash instead of the
        // themed one (developer.android.com/develop/ui/views/launch/splash-screen/migrate).
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // The real "cold start not ready yet" signal. HomeViewModel.loadMapAllowlist()
        // sets loadingMapAllowlist = false in BOTH its success and catch branches, so this
        // is bounded (it will not hang forever) but bounded by the underlying HTTP client's
        // own timeout, not by any timer this code controls -- a genuinely offline first
        // cold launch can legitimately hold the splash for several seconds.
        //
        // Deliberately NOT MapViewModel.routingReady/isLoading: that only becomes
        // meaningful after the user has picked a specific offline map region, which is a
        // later-stage concern than "has the app finished its very first network fetch".
        splashScreen.setKeepOnScreenCondition {
            homeViewModel.uiState.value.loadingMapAllowlist
        }

        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val emphasizedAccelerate = PathInterpolator(0.3f, 0f, 0.8f, 0.15f)

            // Same signal OfflinemapTheme's darkTheme = isSystemInDarkTheme() default reads
            // (Configuration.uiMode), checked directly here because this listener runs before
            // Compose has necessarily composed that theme -- see the constants' doc comment.
            val isSystemInDarkMode = (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val splashExitEndColor =
                if (isSystemInDarkMode) SPLASH_EXIT_END_COLOR_DARK else SPLASH_EXIT_END_COLOR_LIGHT

            val iconOut = ObjectAnimator.ofPropertyValuesHolder(
                splashScreenViewProvider.iconView,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, SPLASH_EXIT_ICON_SCALE),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, SPLASH_EXIT_ICON_SCALE),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
            ).apply {
                duration = SPLASH_EXIT_DURATION_MS
                interpolator = emphasizedAccelerate
            }

            val backgroundCrossfade = ValueAnimator.ofArgb(
                SPLASH_BACKGROUND_COLOR,
                splashExitEndColor,
            ).apply {
                duration = SPLASH_EXIT_DURATION_MS
                interpolator = emphasizedAccelerate
                addUpdateListener { animator ->
                    splashScreenViewProvider.view.setBackgroundColor(animator.animatedValue as Int)
                }
            }

            AnimatorSet().apply {
                playTogether(iconOut, backgroundCrossfade)
                doOnEnd { splashScreenViewProvider.remove() }
                start()
            }
        }

        homeViewModel.loadMapAllowlist()
        homeViewModel.checkUpdate()
        setContent {
            OfflinemapTheme {
                val navController = rememberNavController()
                val uiState by homeViewModel.uiState.collectAsState()
                Box {
                    MapNavGraph(
                        navController = navController,
                        homeViewModel = homeViewModel
                    )
                    // Conditional halo (design canvas turn 11c), only visible while the
                    // OS splash above is also still being held on screen by
                    // setKeepOnScreenCondition -- see SplashHaloOverlay's own doc comment
                    // for why this is Compose motion rather than a second AVD.
                    SplashHaloOverlay(
                        isStillLoading = uiState.loadingMapAllowlist,
                        coldStartElapsedRealtimeMs = coldStartElapsedRealtimeMs,
                    )
                }
            }
        }
    }

}
