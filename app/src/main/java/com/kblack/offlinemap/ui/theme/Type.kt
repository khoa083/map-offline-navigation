package com.kblack.offlinemap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kblack.offlinemap.R

/**
 * Type system — two families, split by what the reader is doing.
 *
 * NunitoHud — HUD numerals only: the big distance-to-maneuver number, the ETA/remaining/
 * distance figures in the route summary. Nunito already ships in res/font/nunito_*.ttf (8
 * weights) and was previously unused outside a single bodyLarge override — this file is what
 * actually puts it to work. Figures read as tabular in every weight used here so digits don't
 * jitter as a distance counts down; that is a property of Nunito's own numeral table, not
 * something set in code.
 *
 * ReadingFamily — everything the driver reads rather than glances at: street names,
 * instructions, list rows, settings, dialogs. The spec calls for Google Sans Flex, which is
 * not licensed for redistribution in this repo — FontFamily.Default (Roboto on stock Android)
 * is the stand-in. Swap ReadingFamily's definition for the real GoogleSansFlex variable font
 * if/when the license is available; every call site below reads through ReadingFamily so that
 * swap is a one-line change, not a find-and-replace.
 *
 * Minimum on-map text size is 14sp; nothing on an active-navigation surface goes below 16sp —
 * both are enforced by which style each screen is told to use, not by a runtime clamp here.
 */
val NunitoHud = FontFamily(
    Font(R.font.nunito_extralight, FontWeight.ExtraLight),
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

val ReadingFamily = FontFamily.Default

/**
 * HUD numeral styles — not part of Typography's named slots (M3's Typography has no "display
 * used only for digits" role), so they're exposed as their own object. Use these only for the
 * numeral itself (e.g. "400", "24"); pair with a Typography.labelMedium-or-smaller unit label
 * ("m", "min") set in ReadingFamily next to it — see the turn card / route summary screens for
 * the pattern.
 */
object HudTypography {
    val hudDisplay = TextStyle(
        fontFamily = NunitoHud,
        fontWeight = FontWeight.Black, // 900
        fontSize = 76.sp,
        lineHeight = 84.sp,
    )
    val hudLarge = TextStyle(
        fontFamily = NunitoHud,
        fontWeight = FontWeight.ExtraBold, // 800
        fontSize = 44.sp,
        lineHeight = 50.sp,
    )
    val hudMedium = TextStyle(
        fontFamily = NunitoHud,
        fontWeight = FontWeight.ExtraBold, // 800
        fontSize = 30.sp,
        lineHeight = 36.sp,
    )
}

/**
 * Reading type ramp, in ReadingFamily. Minimum size anywhere on an active-navigation surface is
 * 16sp (titleMedium and up); minimum anywhere else on the map surface is 14sp (bodyMedium /
 * labelLarge) — do not introduce a call site using labelSmall directly on the map surface.
 */
val Typography = Typography(
    titleLarge = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * The Material 3 Expressive **emphasized tier** (spec 1c).
 *
 * Expressive adds an emphasized variant of all fifteen type roles — `displayLargeEmphasized`
 * through `labelSmallEmphasized`. Same sizes and line heights as the base roles; the variant
 * carries more weight and tighter tracking.
 *
 * This tier is strictly additive: no size, family or weight in [Typography] or [HudTypography]
 * above changes. Only the five roles this app actually uses are defined here, and they are
 * reserved for the driving HUD and primary actions — so emphasis keeps meaning something.
 * Everything else uses the base roles.
 *
 * Two of these are set in [NunitoHud] rather than [ReadingFamily], which is why they are
 * declared explicitly rather than taken from a library default: no default can know that this
 * app's ETA numerals and maneuver distance belong to the HUD family while its street names and
 * button labels do not.
 */
object EmphasizedTypography {

    /**
     * Primary maneuver distance on the turn card (`1j`, `1k`, `4f`) — the single largest, most
     * glanceable number in the app. Nunito, so it shares the HUD's numeral shapes.
     */
    val displayMediumEmphasized = TextStyle(
        fontFamily = NunitoHud,
        fontWeight = FontWeight.Black, // 900
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    )

    /** Street name under the distance on the turn card (`1j`, `1k`). Read, not glanced. */
    val headlineSmallEmphasized = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    )

    /**
     * Arrival / remaining / distance numerals in the ETA row. Nunito, and tabular by virtue of
     * Nunito's own numeral table — which is what stops the row twitching as digits change.
     */
    val titleLargeEmphasized = TextStyle(
        fontFamily = NunitoHud,
        fontWeight = FontWeight.ExtraBold, // 800
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    )

    /** Next-step row and the arrive row in the expanded step list (`4f`). */
    val titleMediumEmphasized = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )

    /** Primary button labels: Start navigation, End navigation, Update now. */
    val labelLargeEmphasized = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    )
}
