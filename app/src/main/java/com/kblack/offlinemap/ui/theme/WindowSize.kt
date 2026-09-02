package com.kblack.offlinemap.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes — the statement of intent from spec 8d, in code.
 *
 * Written down because a navigation app spends real time in a car mount, and because
 * "we only designed portrait" is a decision whether or not anyone makes it deliberately.
 *
 * | Class | Range | Status |
 * |---|---|---|
 * | [Compact] width | < 600dp — phone portrait | Fully supported. The primary target. |
 * | [CompactHeight] | < 480dp tall — phone landscape | Fully supported. HUD moves to a leading column. |
 * | [Medium] width | 600–840dp — tablet portrait, unfolded | Supported by stretch, not redesigned. |
 * | [Expanded] width | > 840dp — tablet landscape, desktop | **Out of scope for this release.** |
 *
 * Expanded is out of scope *explicitly*. The app runs and is usable there — it falls back to the
 * landscape layout with a wider map — but no list-detail pane, navigation rail or multi-pane
 * route planner is designed. Saying so is the point: an undesigned expanded layout that nobody
 * decided about is a bug report waiting to happen; one that was decided about is a backlog item.
 *
 * ## Why landscape is a different layout rather than the same one squashed
 *
 * Stacked landscape would leave the map an 844x120 letterbox — too little forward view to be
 * worth tilting the camera for. So the HUD moves to a [LandscapeHudColumnWidth] leading column
 * and the map keeps the rest, which still gives a near-square viewport showing the road ahead.
 * Sheets become that column; nothing slides up over a landscape map.
 *
 * The turn card keeps its Level 4 elevation and extraLarge corners in landscape. Only its width
 * and the numeral size change — see [LandscapeManeuverNumeralSize].
 */
enum class WindowSizeClass {
    /** < 600dp wide. Phone portrait. Everything outside the landscape boards targets this. */
    Compact,

    /** < 480dp tall. Phone landscape — the car-mount case. */
    CompactHeight,

    /** 600-840dp wide. Tablet portrait / unfolded. Stretched, not redesigned. */
    Medium,

    /** > 840dp wide. Out of scope for this release; falls back to the landscape layout. */
    Expanded,
}

/**
 * Breakpoints and the landscape layout constants. All from spec 8d.
 */
object WindowSizeDefaults {
    /** Below this width the window is [WindowSizeClass.Compact]. */
    val CompactWidthMax: Dp = 600.dp

    /** Above this width the window is [WindowSizeClass.Expanded]. */
    val MediumWidthMax: Dp = 840.dp

    /** Below this height the window is [WindowSizeClass.CompactHeight] — phone landscape. */
    val CompactHeightMax: Dp = 480.dp

    /**
     * Width of the leading column that carries the maneuver, the ETA row and End navigation in
     * landscape. One thumb region; the map gets everything to its trailing side.
     */
    val LandscapeHudColumnWidth: Dp = 352.dp

    /**
     * Max content width in [WindowSizeClass.Medium]. Portrait layouts centre at this width
     * against surface gutters rather than stretching.
     *
     * Lists deliberately do NOT become two columns: a five-item region list in two columns reads
     * worse, not better.
     */
    val MediumMaxContentWidth: Dp = 600.dp

    /**
     * The maneuver numeral shrinks in landscape, where vertical space is the scarce axis.
     *
     * NOTE — this is the one number where the spec contradicts itself: the type sheet (1c) sets
     * `displayMediumEmphasized` at 45sp, while the landscape sheet (8d) describes it dropping
     * "from 56 to 42". [EmphasizedTypography.displayMediumEmphasized] follows the type sheet at
     * 45sp because a type scale is authoritative for type; this constant follows 8d's landscape
     * figure. Resolve the discrepancy in the spec before treating either number as settled.
     */
    val LandscapeManeuverNumeralSize = 42

    /**
     * In landscape the map cluster stays on the trailing edge but **vertically centres** rather
     * than sitting bottom-right: in landscape that corner is the furthest point from either
     * thumb. Compass and 3D collapse into one overflow button so the column never exceeds three
     * targets plus the FAB.
     */
    const val LandscapeMaxClusterTargets = 3
}

/**
 * The current window size class, derived from the configuration.
 *
 * Height is checked first: a phone in a car mount is short before it is wide, and the landscape
 * layout is chosen by the *absence of vertical room*, not by the presence of horizontal room.
 * Checking width first would classify a short-but-wide window as Medium and hand it a portrait
 * layout with no room for it.
 */
@Composable
@ReadOnlyComposable
fun currentWindowSizeClass(): WindowSizeClass {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp.dp
    val heightDp = config.screenHeightDp.dp

    return when {
        heightDp < WindowSizeDefaults.CompactHeightMax -> WindowSizeClass.CompactHeight
        widthDp < WindowSizeDefaults.CompactWidthMax -> WindowSizeClass.Compact
        widthDp <= WindowSizeDefaults.MediumWidthMax -> WindowSizeClass.Medium
        else -> WindowSizeClass.Expanded
    }
}

/**
 * True when the HUD should render as a leading column rather than stacked over the map.
 *
 * [WindowSizeClass.Expanded] is included because its documented fallback is the landscape
 * layout — see the class docs on [WindowSizeClass].
 */
@Composable
@ReadOnlyComposable
fun useLandscapeHudLayout(): Boolean = when (currentWindowSizeClass()) {
    WindowSizeClass.CompactHeight, WindowSizeClass.Expanded -> true
    WindowSizeClass.Compact, WindowSizeClass.Medium -> false
}
