package com.kblack.offlinemap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's colour roles that Material 3's ColorScheme has no slot for (spec 1a / 1b).
 *
 * Resolved *alongside* `MaterialTheme.colorScheme`, never inside it, so a custom token can never
 * be mistaken for a stock one. Read as `LocalOfflineMapColors.current.warning` (or the
 * [offlineMapColors] shorthand).
 *
 * This replaces [CustomColors] for all new UI. CustomColors still exists only because the
 * pre-reskin screens read its template-era fields (taskCardBgColor, tabHeaderBgColor, …); it
 * goes away once those screens are rebuilt.
 *
 * Three groups:
 *  1. **Warning** — sharp-turn / caution. Not an M3 role. [warning] is fill / icon only in the
 *     light scheme (3.95:1 against white): never put body text on it, use [warningContainer]
 *     with [onWarningContainer] instead.
 *  2. **App surfaces** — the four colours the design boards painted with no name. They follow
 *     the theme.
 *  3. **Personalization ceiling** — the surfaces dynamic colour and a user accent may never
 *     retint: turn card, HUD numerals, sharp-turn amber, End navigation (spec 1m). Identical in
 *     both palettes on purpose.
 */
@Immutable
data class OfflineMapColors(
    // 1. Warning
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    // 2. App surfaces
    val mapSurface: Color,
    val navSurface: Color,
    val onNavSurface: Color,
    val outlineVariantOnNav: Color,
    // 3. Personalization ceiling — same value in every scheme
    val turnCard: Color,
    val onTurnCard: Color,
    val hudNumeral: Color,
    val sharpTurnAmber: Color,
    val onSharpTurnAmber: Color,
    val endNavigation: Color,
    val onEndNavigation: Color,
)

val lightOfflineMapColors =
    OfflineMapColors(
        warning = warningLight,
        onWarning = onWarningLight,
        warningContainer = warningContainerLight,
        onWarningContainer = onWarningContainerLight,
        mapSurface = mapSurfaceLight,
        navSurface = navSurfaceLight,
        onNavSurface = onNavSurfaceLight,
        outlineVariantOnNav = outlineVariantOnNavLight,
        turnCard = turnCardFixed,
        onTurnCard = onTurnCardFixed,
        hudNumeral = hudNumeralFixed,
        sharpTurnAmber = sharpTurnAmberFixed,
        onSharpTurnAmber = onSharpTurnAmberFixed,
        endNavigation = endNavigationFixed,
        onEndNavigation = onEndNavigationFixed,
    )

val amoledOfflineMapColors =
    OfflineMapColors(
        warning = warningAmoled,
        onWarning = onWarningAmoled,
        warningContainer = warningContainerAmoled,
        onWarningContainer = onWarningContainerAmoled,
        mapSurface = mapSurfaceAmoled,
        navSurface = navSurfaceAmoled,
        onNavSurface = onNavSurfaceAmoled,
        outlineVariantOnNav = outlineVariantOnNavAmoled,
        turnCard = turnCardFixed,
        onTurnCard = onTurnCardFixed,
        hudNumeral = hudNumeralFixed,
        sharpTurnAmber = sharpTurnAmberFixed,
        onSharpTurnAmber = onSharpTurnAmberFixed,
        endNavigation = endNavigationFixed,
        onEndNavigation = onEndNavigationFixed,
    )

/** Defaults to the light palette so a preview without OfflinemapTheme still renders legibly. */
val LocalOfflineMapColors = staticCompositionLocalOf { lightOfflineMapColors }

val MaterialTheme.offlineMapColors: OfflineMapColors
    @Composable
    @ReadOnlyComposable
    get() = LocalOfflineMapColors.current
