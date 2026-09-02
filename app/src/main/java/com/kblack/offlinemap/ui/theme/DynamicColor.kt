package com.kblack.offlinemap.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Dynamic color (Monet), sampled from the device wallpaper on Android 12+ and clamped against
 * the app's own contrast floor — per the spec's "Personalization ceiling" note:
 *
 * "Dynamic color (Monet) is sampled but clamped: if a wallpaper-derived tone can't hit the
 * floor against its on-color, it falls back to the seeded teal."
 *
 * Two things this deliberately does NOT do, both straight from the same spec section:
 *  1. It never touches background/surface/outline/error/warning or the fixed tokens
 *     (turnCardFixed, hudNumeralFixed, sharpTurnAmberFixed, endNavigationFixed). The
 *     personalization ceiling names exactly what a user-chosen accent may retint — "map
 *     controls, FAB, route line, settings accents, list selection" — which all draw from the
 *     primary/secondary/tertiary triad, not the neutral or fixed roles. Everything outside that
 *     triad stays the static Cartography Teal value regardless of wallpaper.
 *  2. It clamps per role pair, not as an all-or-nothing switch. If the wallpaper's derived
 *     primary/onPrimary pair fails the floor but its secondary/onSecondary pair passes, the
 *     result uses the dynamic secondary and the static (Cartography Teal) primary — matching
 *     the spec's "if A wallpaper-derived tone can't hit the floor... it falls back", singular.
 */

/**
 * WCAG relative luminance of an sRGB color (0-1 per channel), per the standard piecewise
 * gamma-linearization formula. Used only for the contrast check below — this is not a general
 * color-management utility.
 */
private fun relativeLuminance(color: Color): Double {
    fun linearize(c: Float): Double {
        val cd = c.toDouble()
        return if (cd <= 0.03928) cd / 12.92 else ((cd + 0.055) / 1.055).pow(2.4)
    }
    val r = linearize(color.red)
    val g = linearize(color.green)
    val b = linearize(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/** WCAG contrast ratio between two colors, in [1, 21]. */
fun contrastRatio(a: Color, b: Color): Double {
    val la = relativeLuminance(a)
    val lb = relativeLuminance(b)
    val lighter = max(la, lb)
    val darker = min(la, lb)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * The app's outdoor-legibility floor (see Color.kt's file header): 4.5:1 for anything that can
 * carry text, 3:1 for an icon-only or control-edge role. The three personalizable pairs below
 * (primary, secondary, tertiary and their containers) can all end up under text in this app
 * (e.g. a settings accent label), so they're checked against the stricter 4.5:1 — the same
 * floor the static Cartography Teal palette itself is held to.
 */
private const val CONTRAST_FLOOR = 4.5

/**
 * Swaps in a dynamic (base, onColor) pair only when it clears [CONTRAST_FLOOR]; otherwise keeps
 * the static Cartography Teal pair. Returns the pair to use, never a mix of one dynamic and one
 * static half — half-swapping would recreate the exact problem the floor exists to prevent.
 */
private fun clampedPair(
    dynamicBase: Color,
    dynamicOn: Color,
    staticBase: Color,
    staticOn: Color,
): Pair<Color, Color> =
    if (contrastRatio(dynamicBase, dynamicOn) >= CONTRAST_FLOOR) {
        dynamicBase to dynamicOn
    } else {
        staticBase to staticOn
    }

/**
 * Builds the effective color scheme for [darkTheme] when dynamic color is requested: starts
 * from the static Cartography Teal scheme ([staticScheme]) and swaps in the wallpaper-derived
 * primary/secondary/tertiary (+ container) pairs one at a time, each independently clamped by
 * [clampedPair]. Everything else — background, surface family, outline, error — is left as
 * [staticScheme] defines it; dynamic color in this app is an accent-only feature, not a full
 * repaint (see file header).
 *
 * Requires API 31 (Android 12) — [dynamicLightColorScheme]/[dynamicDarkColorScheme] don't exist
 * below that. Call sites must guard with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.
 */
@RequiresApi(Build.VERSION_CODES.S)
fun clampedDynamicColorScheme(
    context: Context,
    darkTheme: Boolean,
    staticScheme: ColorScheme,
): ColorScheme {
    val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

    val (primary, onPrimary) =
        clampedPair(dynamic.primary, dynamic.onPrimary, staticScheme.primary, staticScheme.onPrimary)
    val (primaryContainer, onPrimaryContainer) =
        clampedPair(
            dynamic.primaryContainer,
            dynamic.onPrimaryContainer,
            staticScheme.primaryContainer,
            staticScheme.onPrimaryContainer,
        )
    val (secondary, onSecondary) =
        clampedPair(dynamic.secondary, dynamic.onSecondary, staticScheme.secondary, staticScheme.onSecondary)
    val (secondaryContainer, onSecondaryContainer) =
        clampedPair(
            dynamic.secondaryContainer,
            dynamic.onSecondaryContainer,
            staticScheme.secondaryContainer,
            staticScheme.onSecondaryContainer,
        )
    val (tertiary, onTertiary) =
        clampedPair(dynamic.tertiary, dynamic.onTertiary, staticScheme.tertiary, staticScheme.onTertiary)
    val (tertiaryContainer, onTertiaryContainer) =
        clampedPair(
            dynamic.tertiaryContainer,
            dynamic.onTertiaryContainer,
            staticScheme.tertiaryContainer,
            staticScheme.onTertiaryContainer,
        )

    return staticScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
    )
}

/**
 * Composable entry point for [OfflinemapTheme]: resolves the current [LocalContext] and Android
 * version, and returns the clamped dynamic scheme when both [dynamicColor] is requested and the
 * platform supports it (API 31+). Returns [staticScheme] unchanged otherwise — pre-Android-12
 * devices and the AMOLED/Light static schemes when the user has dynamic color off.
 */
@Composable
fun rememberEffectiveColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    staticScheme: ColorScheme,
): ColorScheme {
    if (!dynamicColor || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return staticScheme
    }
    val context = LocalContext.current
    return clampedDynamicColorScheme(context, darkTheme, staticScheme)
}
