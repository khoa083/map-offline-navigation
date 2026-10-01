package com.kblack.offlinemap.ui.theme.preview

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kblack.offlinemap.ui.theme.AppThemeMode
import com.kblack.offlinemap.ui.theme.OfflinemapTheme

/*
 * Preview kit for the reskin. Every new component should carry at least [ThemePreviews]; the
 * HUD (turn card, ETA row) must also carry [FontScalePreviews] and [LandscapePreviews], because
 * those are the two surfaces spec 1q names as at risk at large font scale and spec 8a/8b
 * redesign for landscape.
 *
 * Frame sizes are the spec's own artboards: portrait 412x916 and landscape 844x390.
 */

private const val PORTRAIT_DEVICE = "spec:width=412dp,height=916dp,dpi=420"
private const val LANDSCAPE_DEVICE = "spec:width=844dp,height=390dp,dpi=420"
private const val AMOLED_BACKGROUND = 0xFF000000
private const val LIGHT_BACKGROUND = 0xFFF7F5EC

/**
 * Light and AMOLED side by side. Works through [PreviewTheme]'s [AppThemeMode.System], which maps
 * the preview's night uiMode to the AMOLED palette exactly as the running app does.
 */
@Preview(
    name = "Light",
    group = "Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = LIGHT_BACKGROUND,
)
@Preview(
    name = "AMOLED",
    group = "Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = AMOLED_BACKGROUND,
)
annotation class ThemePreviews

/** 100% and 200% font scale (spec 1q): content must grow or wrap, never truncate. */
@Preview(name = "Font 100%", group = "Font scale", fontScale = 1f, showBackground = true)
@Preview(name = "Font 200%", group = "Font scale", fontScale = 2f, showBackground = true)
annotation class FontScalePreviews

/** Full spec portrait artboard, both palettes. */
@Preview(
    name = "Portrait light",
    group = "Device",
    device = PORTRAIT_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Portrait AMOLED",
    group = "Device",
    device = PORTRAIT_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
annotation class PortraitPreviews

/** Spec 8a/8b landscape artboard (compact height), both palettes. */
@Preview(
    name = "Landscape light",
    group = "Device",
    device = LANDSCAPE_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
@Preview(
    name = "Landscape AMOLED",
    group = "Device",
    device = LANDSCAPE_DEVICE,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
annotation class LandscapePreviews

/**
 * Wraps preview content in the real app theme with dynamic colour off, so previews show the
 * seeded Cartography Teal rather than whatever wallpaper the preview host samples.
 */
@Composable
fun PreviewTheme(
    themeMode: AppThemeMode = AppThemeMode.System,
    content: @Composable () -> Unit,
) {
    OfflinemapTheme(themeMode = themeMode, dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
