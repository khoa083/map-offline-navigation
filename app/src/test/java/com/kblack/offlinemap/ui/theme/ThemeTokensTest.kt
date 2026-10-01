package com.kblack.offlinemap.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the design tokens against the spec's outdoor-legibility floor (1a): 4.5:1 for any pair
 * that can carry text, 3:1 for icon-only fills and control edges. A token edit that breaks a
 * floor fails here instead of on a phone in sunlight.
 */
class ThemeTokensTest {
    private val textFloor = 4.5
    private val iconFloor = 3.0

    private fun assertContrast(
        name: String,
        background: Color,
        foreground: Color,
        floor: Double,
    ) {
        val ratio = contrastRatio(background, foreground)
        assertTrue("$name is %.2f:1, floor is $floor:1".format(ratio), ratio >= floor)
    }

    @Test
    fun `light text pairs clear 4_5 to 1`() {
        listOf(
            Triple("primary", primaryLight, onPrimaryLight),
            Triple("primaryContainer", primaryContainerLight, onPrimaryContainerLight),
            Triple("secondary", secondaryLight, onSecondaryLight),
            Triple("secondaryContainer", secondaryContainerLight, onSecondaryContainerLight),
            Triple("tertiary", tertiaryLight, onTertiaryLight),
            Triple("tertiaryContainer", tertiaryContainerLight, onTertiaryContainerLight),
            Triple("error", errorLight, onErrorLight),
            Triple("errorContainer", errorContainerLight, onErrorContainerLight),
            Triple("background", backgroundLight, onBackgroundLight),
            Triple("surface", surfaceLight, onSurfaceLight),
            Triple("surfaceVariant", surfaceVariantLight, onSurfaceVariantLight),
            Triple("surfaceContainerHighest", surfaceContainerHighestLight, onSurfaceLight),
            Triple("inverseSurface", inverseSurfaceLight, inverseOnSurfaceLight),
            Triple("warningContainer", warningContainerLight, onWarningContainerLight),
            Triple("mapSurface", mapSurfaceLight, onSurfaceLight),
            Triple("navSurface", navSurfaceLight, onNavSurfaceLight),
        ).forEach { (name, bg, fg) -> assertContrast("light $name", bg, fg, textFloor) }
    }

    @Test
    fun `amoled text pairs clear 4_5 to 1`() {
        listOf(
            Triple("primary", primaryAmoled, onPrimaryAmoled),
            Triple("primaryContainer", primaryContainerAmoled, onPrimaryContainerAmoled),
            Triple("secondary", secondaryAmoled, onSecondaryAmoled),
            Triple("secondaryContainer", secondaryContainerAmoled, onSecondaryContainerAmoled),
            Triple("tertiary", tertiaryAmoled, onTertiaryAmoled),
            Triple("tertiaryContainer", tertiaryContainerAmoled, onTertiaryContainerAmoled),
            Triple("error", errorAmoled, onErrorAmoled),
            Triple("errorContainer", errorContainerAmoled, onErrorContainerAmoled),
            Triple("background", backgroundAmoled, onBackgroundAmoled),
            Triple("surface", surfaceAmoled, onSurfaceAmoled),
            Triple("surfaceVariant", surfaceVariantAmoled, onSurfaceVariantAmoled),
            Triple("surfaceContainerHighest", surfaceContainerHighestAmoled, onSurfaceAmoled),
            Triple("inverseSurface", inverseSurfaceAmoled, inverseOnSurfaceAmoled),
            Triple("warning", warningAmoled, onWarningAmoled),
            Triple("warningContainer", warningContainerAmoled, onWarningContainerAmoled),
            Triple("navSurface", navSurfaceAmoled, onNavSurfaceAmoled),
        ).forEach { (name, bg, fg) -> assertContrast("amoled $name", bg, fg, textFloor) }
    }

    @Test
    fun `personalization ceiling tokens are identical in both palettes`() {
        val l = lightOfflineMapColors
        val a = amoledOfflineMapColors
        assertEquals(l.turnCard, a.turnCard)
        assertEquals(l.onTurnCard, a.onTurnCard)
        assertEquals(l.hudNumeral, a.hudNumeral)
        assertEquals(l.sharpTurnAmber, a.sharpTurnAmber)
        assertEquals(l.onSharpTurnAmber, a.onSharpTurnAmber)
        assertEquals(l.endNavigation, a.endNavigation)
        assertEquals(l.onEndNavigation, a.onEndNavigation)
    }

    @Test
    fun `fixed HUD tokens clear their floors`() {
        assertContrast("turnCard", turnCardFixed, onTurnCardFixed, textFloor)
        assertContrast("endNavigation", endNavigationFixed, onEndNavigationFixed, textFloor)
        // Amber is a fill/icon colour (spec 1a: "fill / icon only"). It does NOT clear the text
        // floor against white, so text on it must use warningContainer/onWarningContainer.
        assertContrast("sharpTurnAmber", sharpTurnAmberFixed, onSharpTurnAmberFixed, iconFloor)
        assertContrast("light warning", warningLight, onWarningLight, iconFloor)
    }

    @Test
    fun `no reading style falls below the 12sp floor`() {
        val t = Typography
        listOf(
            t.titleLarge,
            t.titleMedium,
            t.bodyLarge,
            t.bodyMedium,
            t.labelLarge,
            t.labelMedium,
            t.labelSmall,
        ).forEach { assertTrue("${it.fontSize} < 12sp", it.fontSize.value >= 12.sp.value) }
    }

    @Test
    fun `system mode maps dark to amoled`() {
        assertEquals(ResolvedTheme.Amoled, AppThemeMode.System.resolve(isSystemInDarkTheme = true))
        assertEquals(ResolvedTheme.Light, AppThemeMode.System.resolve(isSystemInDarkTheme = false))
        assertEquals(ResolvedTheme.Light, AppThemeMode.Light.resolve(isSystemInDarkTheme = true))
        assertEquals(ResolvedTheme.Amoled, AppThemeMode.Amoled.resolve(isSystemInDarkTheme = false))
    }
}
