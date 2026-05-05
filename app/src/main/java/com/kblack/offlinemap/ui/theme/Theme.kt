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
    )

private val darkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )

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
)

val LocalCustomColors = staticCompositionLocalOf { CustomColors() }

val lightCustomColors =
    CustomColors(
        appTitleGradientColors = listOf(Color(0xFF85B1F8), Color(0xFF3174F1)),
        tabHeaderBgColor = Color(0xFF3174F1),
        taskCardBgColor = surfaceContainerLowestLight,
        taskBgColors =
            listOf(
                // Shared task color (no task-specific palette)
                Color(0xFFF1F6FE),
            ),
        taskBgGradientColors =
            listOf(
                // Shared task gradient (no task-specific palette)
                listOf(Color(0xFF669DF6), Color(0xFF3174F1)),
            ),
        taskIconColors =
            listOf(
                // Shared task icon color (no task-specific palette)
                Color(0xFF3174F1),
            ),
        agentBubbleBgColor = Color(0xFFe9eef6),
        userBubbleBgColor = Color(0xFF32628D),
        linkColor = Color(0xFF32628D),
        successColor = Color(0xff3d860b),
        recordButtonBgColor = Color(0xFFEE675C),
        waveFormBgColor = Color(0xFFaaaaaa),
        modelInfoIconColor = Color(0xFFCCCCCC),
        warningContainerColor = Color(0xfffef7e0),
        warningTextColor = Color(0xffe37400),
        errorContainerColor = Color(0xfffce8e6),
        errorTextColor = Color(0xffd93025),
        newFeatureContainerColor = Color(0xFFEEDCFE),
        newFeatureTextColor = Color(0xFF400B84),
    )

val darkCustomColors =
    CustomColors(
        appTitleGradientColors = listOf(Color(0xFF85B1F8), Color(0xFF3174F1)),
        tabHeaderBgColor = Color(0xFF3174F1),
        taskCardBgColor = surfaceContainerHighDark,
        taskBgColors =
            listOf(
                // Shared task color (no task-specific palette)
                Color(0xFF191924),
            ),
        taskBgGradientColors =
            listOf(
                // Shared task gradient (no task-specific palette)
                listOf(Color(0xFF669DF6), Color(0xFF3174F1)),
            ),
        taskIconColors =
            listOf(
                // Shared task icon color (no task-specific palette)
                Color(0xFF669DF6),
            ),
        agentBubbleBgColor = Color(0xFF1b1c1d),
        userBubbleBgColor = Color(0xFF1f3760),
        linkColor = Color(0xFF9DCAFC),
        successColor = Color(0xFFA1CE83),
        recordButtonBgColor = Color(0xFFEE675C),
        waveFormBgColor = Color(0xFFaaaaaa),
        modelInfoIconColor = Color(0xFFCCCCCC),
        warningContainerColor = Color(0xff554c33),
        warningTextColor = Color(0xfffcc934),
        errorContainerColor = Color(0xff523a3b),
        errorTextColor = Color(0xffee675c),
        newFeatureContainerColor = Color(0xFFEEDCFE),
        newFeatureTextColor = Color(0xFF400B84),
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


@Composable
fun OfflinemapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // currently not supporting dynamic color, and defaulting to dark theme, but we will add support for both in the future.
    val darkTheme: Boolean = true //todo: fixme
    val view = LocalView.current

    StatusBarColorController(useDarkTheme = darkTheme)

    val colorScheme =
        when {
            darkTheme -> darkScheme
            else -> lightScheme
        }

    val customColorsPalette = if (dynamicColor) darkCustomColors else lightCustomColors

    CompositionLocalProvider(LocalCustomColors provides customColorsPalette) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }

    // Make sure the navigation bar stays transparent on manual theme changes.
    LaunchedEffect(darkTheme) {
        val window = (view.context as Activity).window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}