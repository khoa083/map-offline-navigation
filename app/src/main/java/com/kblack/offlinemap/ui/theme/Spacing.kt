package com.kblack.offlinemap.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Padding / gap / inset / control-size scale, taken from the design spec (Offline Map M3
 * Expressive Spec). Every value below appears in that spec — this is not a generic 8dp-grid
 * guess. Reach for a named field here before writing a raw `.dp` literal at a call site; if a
 * spacing genuinely isn't covered, add it here with a note on where it came from rather than
 * inlining it.
 *
 * Naming follows a t-shirt scale for generic gaps (xxs..xxxl) plus named roles for values that
 * recur for a specific structural reason, so call sites read as intent rather than as a magic
 * number with a matching t-shirt label.
 *
 * ## Control sizes are a two-tier scale, and the tiers are not interchangeable
 *
 * The spec collapsed an earlier mess of 40 / 44 / 52dp variants of the same function into
 * exactly two tiers plus one exception:
 *
 * - [touchTargetMin] **48dp** — the standard tier and the platform minimum. Header icons, back
 *   buttons, list-row affordances, search-bar actions.
 * - [controlEmphasis] **56dp** — the emphasis tier. The map control cluster: zoom pair, compass,
 *   3D toggle. Bigger because they are aimed at one-handed while the vehicle is moving.
 * - [recenterFabSize] **64dp** — the recenter FAB alone. It is the only full-round control on
 *   the map, so it is findable by shape; the size is what makes it findable by feel.
 *
 * Nothing else is a control size. If a new control does not fit one of these three, the question
 * is which tier it belongs to, not what number looks right.
 */
@Immutable
data class Spacing(
    // -----------------------------------------------------------------------------------------
    // Generic t-shirt scale
    // -----------------------------------------------------------------------------------------
    val xxs: Dp = 4.dp, // icon-to-label gaps, chip internal padding
    val xs: Dp = 6.dp, // tight inline gaps (e.g. unit label next to a HUD numeral)
    val sm: Dp = 8.dp, // row internal gaps, small icon touch padding
    val md: Dp = 12.dp, // default item spacing inside a column/row
    val lg: Dp = 16.dp, // card internal padding, section gaps
    val xl: Dp = 20.dp, // sheet content padding
    val xxl: Dp = 24.dp, // section-to-section spacing, dialog padding
    val xxxl: Dp = 28.dp, // large bottom-sheet top padding / handle clearance

    // -----------------------------------------------------------------------------------------
    // Layout roles
    // -----------------------------------------------------------------------------------------
    val screenEdge: Dp = 16.dp, // left/right margin for full-screen content
    val cardPadding: Dp = 16.dp, // internal padding for a Stopped-register card
    val cardGap: Dp = 12.dp, // gap between stacked cards in a list
    val sheetHandleHeight: Dp = 4.dp, // bottom-sheet drag-handle bar height
    val sheetHandleWidth: Dp = 32.dp, // bottom-sheet drag-handle bar width
    val sheetHandleTopInset: Dp = 10.dp, // space above the handle before sheet content starts
    val listRowVertical: Dp = 12.dp, // vertical padding inside a search-result / region row
    val listRowIconGap: Dp = 12.dp, // gap between a row's leading icon and its text block
    val progressTrackHeight: Dp = 6.dp, // straight (non-wavy) route/ETA track — spec 1m

    // -----------------------------------------------------------------------------------------
    // Control sizes — see the class docs; three values, no others
    // -----------------------------------------------------------------------------------------

    /** Standard tier and the platform minimum for anything tappable. */
    val touchTargetMin: Dp = 48.dp,

    /**
     * Emphasis tier: the map control cluster — zoom pair, compass, 3D toggle.
     *
     * Was 52dp here until this pass, which matched neither the spec's 56dp tier nor the 48dp
     * standard tier — it was a third size for no stated reason, which is exactly what the two-
     * tier scale exists to prevent.
     */
    val controlEmphasis: Dp = 56.dp,

    /**
     * The recenter FAB, and nothing else. Full-round at 64dp so it is the one control on the
     * map identifiable by shape and size without looking.
     *
     * Was 56dp here until this pass, which collided with the emphasis tier and removed the
     * size difference the "findable by shape alone" rule depends on.
     */
    val recenterFabSize: Dp = 64.dp,

    // -----------------------------------------------------------------------------------------
    // Separation — spec 1q
    // -----------------------------------------------------------------------------------------

    /**
     * Minimum clear space between two adjacent touch targets. 48dp of *size* is not enough on
     * its own: two live targets closer than this read as one, and a press landing between them
     * hits neither.
     */
    val touchTargetSeparation: Dp = 8.dp,

    /**
     * Gap between controls in the map cluster. Comfortably above [touchTargetSeparation].
     */
    val mapClusterGap: Dp = 12.dp,

    /**
     * Divider inside the joined zoom control. The zoom pair is deliberately **one segmented
     * control** rather than two buttons: at 3dp apart they had a visible seam that was too small
     * to aim into and too large to ignore, so a press landing in it did nothing. Joined, with a
     * hairline `outlineVariant` divider and no gap, every press lands on a live target.
     */
    val segmentedDivider: Dp = 1.dp,
)

/**
 * `LocalSpacing.current` at any call site inside `OfflinemapTheme` — provided there alongside
 * `LocalCustomColors`, same pattern, same lifetime.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }
