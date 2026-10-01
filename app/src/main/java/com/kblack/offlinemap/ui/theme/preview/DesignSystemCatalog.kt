package com.kblack.offlinemap.ui.theme.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.ui.theme.Corner
import com.kblack.offlinemap.ui.theme.Elevation
import com.kblack.offlinemap.ui.theme.EmphasizedTypography
import com.kblack.offlinemap.ui.theme.HudTypography
import com.kblack.offlinemap.ui.theme.LocalSpacing
import com.kblack.offlinemap.ui.theme.contrastRatio
import com.kblack.offlinemap.ui.theme.elevatedSurface
import com.kblack.offlinemap.ui.theme.offlineMapColors
import java.util.Locale

/*
 * The design tokens rendered from code, one preview per spec sheet (1a/1b colour, 1c type,
 * 1d shape, 1o elevation). Open this file in Android Studio's preview pane and compare it with the
 * spec HTML: if a swatch here disagrees with the spec, the token is wrong, not the screen.
 *
 * Previews only — nothing in the app navigates here.
 */

// -------------------------------------------------------------------------------------------
// 1a / 1b — colour
// -------------------------------------------------------------------------------------------

@ThemePreviews
@Composable
private fun ColorRolesPreview() {
    PreviewTheme {
        val c = MaterialTheme.colorScheme
        val x = MaterialTheme.offlineMapColors
        CatalogColumn {
            SectionTitle("M3 roles")
            SwatchRow("primary", c.primary, c.onPrimary)
            SwatchRow("primaryContainer", c.primaryContainer, c.onPrimaryContainer)
            SwatchRow("secondary", c.secondary, c.onSecondary)
            SwatchRow("secondaryContainer", c.secondaryContainer, c.onSecondaryContainer)
            SwatchRow("error", c.error, c.onError)
            SwatchRow("errorContainer", c.errorContainer, c.onErrorContainer)
            SwatchRow("surface", c.surface, c.onSurface)
            SwatchRow("surfaceContainer", c.surfaceContainer, c.onSurface)
            SwatchRow("surfaceContainerHighest", c.surfaceContainerHighest, c.onSurface)
            SwatchRow("surfaceVariant", c.surfaceVariant, c.onSurfaceVariant)
            SwatchRow("inverseSurface", c.inverseSurface, c.inverseOnSurface)
            SectionTitle("App extension · offlineMapColors")
            SwatchRow("warning (icon only)", x.warning, x.onWarning)
            SwatchRow("warningContainer", x.warningContainer, x.onWarningContainer)
            SwatchRow("mapSurface", x.mapSurface, c.onSurface)
            SwatchRow("navSurface", x.navSurface, x.onNavSurface)
            SectionTitle("Personalization ceiling · fixed")
            SwatchRow("turnCard", x.turnCard, x.onTurnCard)
            SwatchRow("sharpTurnAmber (icon only)", x.sharpTurnAmber, x.onSharpTurnAmber)
            SwatchRow("endNavigation", x.endNavigation, x.onEndNavigation)
        }
    }
}

// -------------------------------------------------------------------------------------------
// 1c — type
// -------------------------------------------------------------------------------------------

@ThemePreviews
@FontScalePreviews
@Composable
private fun TypePreview() {
    PreviewTheme {
        val t = MaterialTheme.typography
        CatalogColumn {
            SectionTitle("HUD · Nunito, tabular")
            TypeSample("hudDisplay 76/900", "400 m", HudTypography.hudDisplay)
            TypeSample("hudLarge 44/800", "24 min", HudTypography.hudLarge)
            TypeSample("hudMedium 30/800", "12.4 km", HudTypography.hudMedium)
            SectionTitle("Emphasized tier")
            TypeSample(
                "displayMediumEmphasized",
                "400 m",
                EmphasizedTypography.displayMediumEmphasized,
            )
            TypeSample(
                "headlineSmallEmphasized",
                "Nguyen Trai Street",
                EmphasizedTypography.headlineSmallEmphasized,
            )
            TypeSample("titleLargeEmphasized", "10:32", EmphasizedTypography.titleLargeEmphasized)
            TypeSample(
                "titleMediumEmphasized",
                "Keep left onto CT01",
                EmphasizedTypography.titleMediumEmphasized,
            )
            TypeSample(
                "labelLargeEmphasized",
                "Start navigation",
                EmphasizedTypography.labelLargeEmphasized,
            )
            SectionTitle("Reading ramp")
            TypeSample("titleLarge 22", "Turn right onto Nguyen Trai", t.titleLarge)
            TypeSample("titleMedium 18/600", "Ben Thanh Market", t.titleMedium)
            TypeSample("bodyLarge 16/24", "Le Loi, District 1", t.bodyLarge)
            TypeSample("bodyMedium 14/20", "Tan Binh · 7.6 km", t.bodyMedium)
            TypeSample("labelLarge 14/600", "Directions", t.labelLarge)
            TypeSample("labelSmall 12/500", "DESTINATION", t.labelSmall)
        }
    }
}

// -------------------------------------------------------------------------------------------
// 1d — shape
// -------------------------------------------------------------------------------------------

@ThemePreviews
@Composable
private fun ShapePreview() {
    PreviewTheme {
        CatalogColumn {
            SectionTitle("Corner scale")
            ShapeSample("extraSmall 4", RoundedCornerShape(Corner.ExtraSmall))
            ShapeSample("small 8", RoundedCornerShape(Corner.Small))
            ShapeSample("medium 12 · map controls", RoundedCornerShape(Corner.Medium))
            ShapeSample("large 16", RoundedCornerShape(Corner.Large))
            ShapeSample("largeIncreased 20", RoundedCornerShape(Corner.LargeIncreased))
            ShapeSample("extraLarge 28 · sheets", RoundedCornerShape(Corner.ExtraLarge))
            ShapeSample("extraLargeIncreased 32", RoundedCornerShape(Corner.ExtraLargeIncreased))
            ShapeSample("full · FAB, pills", CircleShape)
        }
    }
}

// -------------------------------------------------------------------------------------------
// 1o — elevation
// -------------------------------------------------------------------------------------------

@ThemePreviews
@Composable
private fun ElevationPreview() {
    PreviewTheme {
        CatalogColumn {
            SectionTitle("Levels 0–5 · shadow on light, outline on AMOLED")
            ElevationSample("Level 0 · map, rows", Elevation.Level0)
            ElevationSample("Level 1 · search bar", Elevation.Level1)
            ElevationSample("Level 2 · map controls", Elevation.Level2)
            ElevationSample("Level 3 · FAB, sheets", Elevation.Level3)
            ElevationSample("Level 4 · turn card", Elevation.Level4)
            ElevationSample("Level 5 · dialogs", Elevation.Level5)
        }
    }
}

// -------------------------------------------------------------------------------------------
// Catalog building blocks (preview-only)
// -------------------------------------------------------------------------------------------

@Composable
private fun CatalogColumn(content: @Composable () -> Unit) {
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = LocalSpacing.current.sm),
    )
}

@Composable
private fun SwatchRow(
    name: String,
    container: Color,
    content: Color,
) {
    val ratio = contrastRatio(container, content)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(LocalSpacing.current.touchTargetMin)
                .background(container, RoundedCornerShape(Corner.Small))
                .padding(horizontal = LocalSpacing.current.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = name, color = content, style = MaterialTheme.typography.labelLarge)
        Text(
            text = "%.1f:1".format(Locale.ROOT, ratio),
            color = content,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun TypeSample(
    label: String,
    sample: String,
    style: TextStyle,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = sample, style = style, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ShapeSample(
    label: String,
    shape: Shape,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.md),
    ) {
        Box(
            Modifier
                .size(LocalSpacing.current.controlEmphasis)
                .background(MaterialTheme.colorScheme.primaryContainer, shape)
                .border(1.dp, MaterialTheme.colorScheme.outline, shape),
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ElevationSample(
    label: String,
    level: Dp,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = LocalSpacing.current.xxs)
                .elevatedSurface(level, RoundedCornerShape(Corner.LargeIncreased))
                .height(LocalSpacing.current.controlEmphasis)
                .padding(horizontal = LocalSpacing.current.lg),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
