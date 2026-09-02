package com.kblack.offlinemap.ui.theme

/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// -------------------------------------------------------------------------------------------
// M3 ColorScheme — built from Color.kt's "Cartography Teal" tokens.
//
// amoledScheme is built with darkColorScheme() (M3 still calls its dark variant "dark"
// internally) but every value fed into it comes from the *Amoled tokens in Color.kt, i.e. a
// real #000000 background, not a derived dim of lightScheme. See Color.kt's file header for
// why these are two independent hand-authored palettes rather than one generated from the
// other.
// -------------------------------------------------------------------------------------------

private val lightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
        surfaceTint = surfaceTintLight,
        // The twelve M3 Fixed roles. Identical in both schemes by contract - see the block
        // comment in Color.kt. Unused by this app's own screens; populated so a stock component
        // that asks for one gets an in-palette answer rather than Baseline purple.
        primaryFixed = primaryFixed,
        primaryFixedDim = primaryFixedDim,
        onPrimaryFixed = onPrimaryFixed,
        onPrimaryFixedVariant = onPrimaryFixedVariant,
        secondaryFixed = secondaryFixed,
        secondaryFixedDim = secondaryFixedDim,
        onSecondaryFixed = onSecondaryFixed,
        onSecondaryFixedVariant = onSecondaryFixedVariant,
        tertiaryFixed = tertiaryFixed,
        tertiaryFixedDim = tertiaryFixedDim,
        onTertiaryFixed = onTertiaryFixed,
        onTertiaryFixedVariant = onTertiaryFixedVariant,
    )

private val amoledScheme =
    darkColorScheme(
        primary = primaryAmoled,
        onPrimary = onPrimaryAmoled,
        primaryContainer = primaryContainerAmoled,
        onPrimaryContainer = onPrimaryContainerAmoled,
        secondary = secondaryAmoled,
        onSecondary = onSecondaryAmoled,
        secondaryContainer = secondaryContainerAmoled,
        onSecondaryContainer = onSecondaryContainerAmoled,
        tertiary = tertiaryAmoled,
        onTertiary = onTertiaryAmoled,
        tertiaryContainer = tertiaryContainerAmoled,
        onTertiaryContainer = onTertiaryContainerAmoled,
        error = errorAmoled,
        onError = onErrorAmoled,
        errorContainer = errorContainerAmoled,
        onErrorContainer = onErrorContainerAmoled,
        background = backgroundAmoled,
        onBackground = onBackgroundAmoled,
        surface = surfaceAmoled,
        onSurface = onSurfaceAmoled,
        surfaceVariant = surfaceVariantAmoled,
        onSurfaceVariant = onSurfaceVariantAmoled,
        outline = outlineAmoled,
        outlineVariant = outlineVariantAmoled,
        scrim = scrimAmoled,
        inverseSurface = inverseSurfaceAmoled,
        inverseOnSurface = inverseOnSurfaceAmoled,
        inversePrimary = inversePrimaryAmoled,
        surfaceDim = surfaceDimAmoled,
        surfaceBright = surfaceBrightAmoled,
        surfaceContainerLowest = surfaceContainerLowestAmoled,
        surfaceContainerLow = surfaceContainerLowAmoled,
        surfaceContainer = surfaceContainerAmoled,
        surfaceContainerHigh = surfaceContainerHighAmoled,
        surfaceContainerHighest = surfaceContainerHighestAmoled,
        surfaceTint = surfaceTintAmoled,
        // The twelve M3 Fixed roles. Identical in both schemes by contract - see the block
        // comment in Color.kt. Unused by this app's own screens; populated so a stock component
        // that asks for one gets an in-palette answer rather than Baseline purple.
        primaryFixed = primaryFixed,
        primaryFixedDim = primaryFixedDim,
        onPrimaryFixed = onPrimaryFixed,
        onPrimaryFixedVariant = onPrimaryFixedVariant,
        secondaryFixed = secondaryFixed,
        secondaryFixedDim = secondaryFixedDim,
        onSecondaryFixed = onSecondaryFixed,
        onSecondaryFixedVariant = onSecondaryFixedVariant,
        tertiaryFixed = tertiaryFixed,
        tertiaryFixedDim = tertiaryFixedDim,
        onTertiaryFixed = onTertiaryFixed,
        onTertiaryFixedVariant = onTertiaryFixedVariant,
    )

/**
 * Extra roles the M3 ColorScheme has no slot for. Field names are kept from the pre-reskin
 * palette on purpose — taskCardBgColor, tabHeaderBgColor, modelInfoIconColor, linkColor and the
 * rest are read by call sites across the app (NavigationMode, RouteInstructionsBottomSheet,
 * UpdateRoutingVehicle, MapControls, MapViewScreen, MapNameAndStatus, MarkdownText) that this
 * reskin does not touch — only the *values* behind each name change here.
 *
 * New fields added for the v2 system design, appended rather than interleaved so the diff
 * against the pre-reskin file stays readable:
 *  - warning / onWarning / warningContainer / onWarningContainer — the sharp-turn / caution
 *    role from Color.kt's warningLight/warningAmoled group. Not a standard ColorScheme slot (M3
 *    has no "warning" tier), so it lives here like the rest of this class.
 *  - turnCardFixed / onTurnCardFixed / hudNumeralFixed / sharpTurnAmberFixed /
 *    onSharpTurnAmberFixed / endNavigationFixed / onEndNavigationFixed — the "personalization
 *    ceiling" tokens (see Color.kt). Identical in both lightCustomColors and amoledCustomColors
 *    on purpose: these must never vary by theme or by a user accent choice.
 */
@Immutable
data class CustomColors(
    val appTitleGradientColors: List<Color> = listOf(),
    val tabHeaderBgColor: Color = Color.Transparent,
    val taskCardBgColor: Color = Color.Transparent,
    val taskBgColors: List<Color> = listOf(),
    val taskBgGradientColors: List<List<Color>> = listOf(),
    val taskIconColors: List<Color> = listOf(),
    val userBubbleBgColor: Color = Color.Transparent,
    val agentBubbleBgColor: Color = Color.Transparent,
    val linkColor: Color = Color.Transparent,
    val successColor: Color = Color.Transparent,
    val recordButtonBgColor: Color = Color.Transparent,
    val waveFormBgColor: Color = Color.Transparent,
    val modelInfoIconColor: Color = Color.Transparent,
    val warningContainerColor: Color = Color.Transparent,
    val warningTextColor: Color = Color.Transparent,
    val errorContainerColor: Color = Color.Transparent,
    val errorTextColor: Color = Color.Transparent,
    val newFeatureContainerColor: Color = Color.Transparent,
    val newFeatureTextColor: Color = Color.Transparent,
    // --- v2 system design additions ---
    val warning: Color = Color.Transparent,
    val onWarning: Color = Color.Transparent,
    val warningContainer: Color = Color.Transparent,
    val onWarningContainer: Color = Color.Transparent,
    val mapSurface: Color = Color.Transparent,
    val navSurface: Color = Color.Transparent,
    val onNavSurface: Color = Color.Transparent,
    val outlineVariantOnNav: Color = Color.Transparent,
    val turnCardFixed: Color = Color.Transparent,
    val onTurnCardFixed: Color = Color.Transparent,
    val hudNumeralFixed: Color = Color.Transparent,
    val sharpTurnAmberFixed: Color = Color.Transparent,
    val onSharpTurnAmberFixed: Color = Color.Transparent,
    val endNavigationFixed: Color = Color.Transparent,
    val onEndNavigationFixed: Color = Color.Transparent,
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

val lightCustomColors =
    CustomColors(
        appTitleGradientColors = listOf(primaryLight, secondaryLight),
        tabHeaderBgColor = primaryLight,
        taskCardBgColor = surfaceContainerLowestLight,
        taskBgColors =
            listOf(
                // Shared task color (no task-specific palette)
                primaryContainerLight,
            ),
        taskBgGradientColors =
            listOf(
                // Shared task gradient (no task-specific palette)
                listOf(secondaryLight, primaryLight),
            ),
        taskIconColors =
            listOf(
                // Shared task icon color (no task-specific palette)
                primaryLight,
            ),
        agentBubbleBgColor = surfaceContainerLight,
        userBubbleBgColor = secondaryContainerLight,
        linkColor = primaryLight,
        successColor = Color(0xFF3D860B),
        recordButtonBgColor = errorLight,
        waveFormBgColor = outlineVariantLight,
        modelInfoIconColor = outlineLight,
        warningContainerColor = warningContainerLight,
        warningTextColor = onWarningContainerLight,
        errorContainerColor = errorContainerLight,
        errorTextColor = onErrorContainerLight,
        newFeatureContainerColor = secondaryContainerLight,
        newFeatureTextColor = onSecondaryContainerLight,
        warning = warningLight,
        onWarning = onWarningLight,
        warningContainer = warningContainerLight,
        onWarningContainer = onWarningContainerLight,
        mapSurface = mapSurfaceLight,
        navSurface = navSurfaceLight,
        onNavSurface = onNavSurfaceLight,
        outlineVariantOnNav = outlineVariantOnNavLight,
        turnCardFixed = turnCardFixed,
        onTurnCardFixed = onTurnCardFixed,
        hudNumeralFixed = hudNumeralFixed,
        sharpTurnAmberFixed = sharpTurnAmberFixed,
        onSharpTurnAmberFixed = onSharpTurnAmberFixed,
        endNavigationFixed = endNavigationFixed,
        onEndNavigationFixed = onEndNavigationFixed,
    )

val amoledCustomColors =
    CustomColors(
        appTitleGradientColors = listOf(primaryAmoled, secondaryAmoled),
        tabHeaderBgColor = primaryContainerAmoled,
        taskCardBgColor = surfaceContainerHighAmoled,
        taskBgColors =
            listOf(
                // Shared task color (no task-specific palette)
                surfaceContainerAmoled,
            ),
        taskBgGradientColors =
            listOf(
                // Shared task gradient (no task-specific palette)
                listOf(secondaryAmoled, primaryAmoled),
            ),
        taskIconColors =
            listOf(
                // Shared task icon color (no task-specific palette)
                primaryAmoled,
            ),
        agentBubbleBgColor = surfaceContainerLowAmoled,
        userBubbleBgColor = secondaryContainerAmoled,
        linkColor = primaryAmoled,
        successColor = Color(0xFFA1CE83),
        recordButtonBgColor = errorAmoled,
        waveFormBgColor = outlineVariantAmoled,
        modelInfoIconColor = outlineAmoled,
        warningContainerColor = warningContainerAmoled,
        warningTextColor = onWarningContainerAmoled,
        errorContainerColor = errorContainerAmoled,
        errorTextColor = onErrorContainerAmoled,
        newFeatureContainerColor = secondaryContainerAmoled,
        newFeatureTextColor = onSecondaryContainerAmoled,
        warning = warningAmoled,
        onWarning = onWarningAmoled,
        warningContainer = warningContainerAmoled,
        onWarningContainer = onWarningContainerAmoled,
        mapSurface = mapSurfaceAmoled,
        navSurface = navSurfaceAmoled,
        onNavSurface = onNavSurfaceAmoled,
        outlineVariantOnNav = outlineVariantOnNavAmoled,
        turnCardFixed = turnCardFixed,
        onTurnCardFixed = onTurnCardFixed,
        hudNumeralFixed = hudNumeralFixed,
        sharpTurnAmberFixed = sharpTurnAmberFixed,
        onSharpTurnAmberFixed = onSharpTurnAmberFixed,
        endNavigationFixed = endNavigationFixed,
        onEndNavigationFixed = onEndNavigationFixed,
    )

val MaterialTheme.customColors: CustomColors
    @Composable @ReadOnlyComposable get() = LocalCustomColors.current

/**
 * Controls the color of the phone's status bar icons based on whether the app is using a dark
 * theme.
 */
@Composable
fun StatusBarColorController(useDarkTheme: Boolean) {
    val view = LocalView.current
    val currentWindow = (view.context as? Activity)?.window

    if (currentWindow != null) {
        SideEffect {
            WindowCompat.setDecorFitsSystemWindows(currentWindow, false)
            val controller = WindowCompat.getInsetsController(currentWindow, view)
            controller.isAppearanceLightStatusBars = !useDarkTheme // Set to true for light icons
        }
    }
}

/**
 * @param darkTheme Selects amoledScheme vs lightScheme. Defaults to the system setting via
 *   isSystemInDarkTheme and, unlike the pre-reskin build, is no longer overridden — the
 *   previous val darkTheme: Boolean = true //todo: fixme forced Amoled regardless of this
 *   parameter or the system setting; that bug is fixed here. Pass AppThemeMode through this
 *   boolean at the call site (themeMode == AppThemeMode.Amoled) once a persisted user setting
 *   exists — see ThemeSettings.kt for why "follow system" isn't wired in by default.
 * @param dynamicColor Android 12+ dynamic (wallpaper-derived) color, sampled and clamped per
 *   the spec's "Personalization ceiling" note — see DynamicColor.kt for the full contract. Only
 *   the primary/secondary/tertiary triad (the roles behind map controls, the FAB, the route
 *   line, settings accents and list selection) is ever replaced by a wallpaper tone, and only
 *   when that tone clears the app's own contrast floor against its on-color; everything else,
 *   including background/surface/error and the fixed tokens, always stays Cartography Teal.
 *   Below API 31, or when this is false, the static scheme is used unchanged.
 */
@Composable
fun OfflinemapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    StatusBarColorController(useDarkTheme = darkTheme)

    val staticScheme =
        when {
            darkTheme -> amoledScheme
            else -> lightScheme
        }
    val colorScheme =
        rememberEffectiveColorScheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            staticScheme = staticScheme,
        )

    val customColorsPalette = if (darkTheme) amoledCustomColors else lightCustomColors

    CompositionLocalProvider(
        LocalCustomColors provides customColorsPalette,
        LocalSpacing provides Spacing(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }

    // Make sure the navigation bar stays transparent on manual theme changes.
    LaunchedEffect(darkTheme) {
        val window = (view.context as Activity).window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}
