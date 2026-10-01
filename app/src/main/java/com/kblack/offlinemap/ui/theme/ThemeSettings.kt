package com.kblack.offlinemap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * User-facing theme mode (spec 1m, "Carried over from the current build": light / dark /
 * AMOLED become a real user choice instead of the old hard-forced dark theme).
 *
 * The two real palettes are Light (outdoor/daytime) and Amoled (true-black night). [System]
 * follows the platform dark-mode signal and maps it to Amoled — the same mapping
 * MainActivity's splash exit already assumes when it picks its end colour, so the default
 * keeps today's behaviour.
 *
 * The spec also mentions a *standard* dark theme ("keeps #101614 as background instead"), but
 * names only that one value, not a full palette. It is deliberately not added here until the
 * remaining roles are specified — a half-specified scheme is how Baseline purple leaks in.
 */
enum class AppThemeMode {
    System,
    Light,
    Amoled,
    ;

    /** The concrete palette this mode draws, given the platform's current dark-mode signal. */
    fun resolve(isSystemInDarkTheme: Boolean): ResolvedTheme =
        when (this) {
            System -> if (isSystemInDarkTheme) ResolvedTheme.Amoled else ResolvedTheme.Light
            Light -> ResolvedTheme.Light
            Amoled -> ResolvedTheme.Amoled
        }
}

/**
 * The palette actually on screen. Separate from [AppThemeMode] because [AppThemeMode.System]
 * is a preference, not a palette — components only ever need to know which palette they are
 * drawn in.
 */
enum class ResolvedTheme {
    Light,
    Amoled,
}

/**
 * Provided by OfflinemapTheme. Read it through [isAmoledTheme] rather than
 * `isSystemInDarkTheme()`: with an explicit Light/Amoled choice the system signal is no
 * longer the source of truth.
 */
val LocalResolvedTheme = staticCompositionLocalOf { ResolvedTheme.Light }

/**
 * True when the current palette is the true-black AMOLED one. Elevation, borders and any other
 * "shadow vs outline" decision branches on this (see [Elevation]).
 */
@Composable
@ReadOnlyComposable
fun isAmoledTheme(): Boolean = LocalResolvedTheme.current == ResolvedTheme.Amoled

/** Same as [isAmoledTheme], for call sites that already read everything off MaterialTheme. */
val MaterialTheme.resolvedTheme: ResolvedTheme
    @Composable
    @ReadOnlyComposable
    get() = LocalResolvedTheme.current
