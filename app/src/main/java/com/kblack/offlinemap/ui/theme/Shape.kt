package com.kblack.offlinemap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shape — the Material 3 corner scale including the Expressive additions, and this app's two
 * registers (spec 1d).
 *
 * **Nothing in this app uses a radius that is not on [Corner].** That is the whole point of a
 * scale: a radius typed at a call site cannot be changed system-wide, cannot be reasoned about
 * against its neighbours, and drifts — the design canvas had thirteen off-scale radii in use
 * before this file named the scale, with 24dp appearing nineteen times purely because someone
 * once typed it.
 *
 * ## The two registers, and why they are a rule rather than a preference
 *
 * Expressive rounding earns its place on things read while stopped. Anything tapped one-handed
 * while the vehicle is moving stays at [Corner.Medium] (12dp), so its edges stay unambiguous
 * under a thumb that is not looking. Both registers draw from the same named scale — the split
 * is about *which* token a surface gets, never about inventing a radius.
 *
 * [Shapes] (the slots `MaterialTheme.shapes` exposes) is built from the stopped register,
 * because M3 has no "while driving" axis and most themed components — cards, sheets, menus — are
 * stopped-context by default. Moving-register surfaces are shaped explicitly at the call site
 * via [Moving], so a themed default can never silently soften a control meant to be crisp.
 */
object Corner {
    /** 4dp — progress-track ends, inner corners of a grouped-list middle item. */
    val ExtraSmall: Dp = 4.dp

    /** 8dp — grouped-list inner corners, chips, small status containers. */
    val Small: Dp = 8.dp

    /** 12dp — map controls: zoom pair, compass, 3D toggle. The moving register. */
    val Medium: Dp = 12.dp

    /** 16dp — header icon buttons, leading icon tiles, inline warning containers. */
    val Large: Dp = 16.dp

    /** 20dp — *Expressive.* Nested cards and rows inside a sheet, secondary containers. */
    val LargeIncreased: Dp = 20.dp

    /** 28dp — bottom sheets, top cards, static content cards, grouped-list outer corners. */
    val ExtraLarge: Dp = 28.dp

    /** 32dp — *Expressive.* Device-frame radius, hero containers, empty-state art tiles. */
    val ExtraLargeIncreased: Dp = 32.dp

    /**
     * 48dp — *Expressive.* Currently unused in this app; reserved for a future expanded search
     * container. Listed so that when someone needs a radius larger than 32dp they take this one
     * instead of inventing 40dp.
     */
    val ExtraExtraLarge: Dp = 48.dp

    /**
     * Two hairline decorations sit deliberately off the scale, and only these two.
     *
     * 3dp on linear progress tracks and the sheet drag handle, 2dp on the puck shadow and the
     * pin tip. Their radius is half their own height — they are line caps, not container
     * corners, and putting them on [ExtraSmall] would square them off visually. Neither is ever
     * applied to a tappable or scrollable surface, which is the line that keeps this exception
     * from becoming a loophole.
     */
    val HairlineTrack: Dp = 3.dp
    val HairlineTip: Dp = 2.dp
}

/**
 * Moving register — surfaces touched while the vehicle is in motion. One radius,
 * [Corner.Medium], so every driving control shares an edge treatment the thumb can predict.
 */
object Moving {
    /** Map floating controls: zoom pair, compass, 3D toggle. */
    val control = RoundedCornerShape(Corner.Medium)

    /** Turn card and the End-navigation button — same crisp radius as [control]. */
    val turnCard = RoundedCornerShape(Corner.Medium)
}

/**
 * Stopped register — surfaces read while stopped, where Expressive rounding is welcome.
 * Every value maps to a named token on [Corner]; none is a free-hand radius.
 */
object Stopped {
    val extraSmall = RoundedCornerShape(Corner.ExtraSmall)
    val small = RoundedCornerShape(Corner.Small)
    val medium = RoundedCornerShape(Corner.Large)
    val large = RoundedCornerShape(Corner.LargeIncreased)
    val extraLarge = RoundedCornerShape(Corner.ExtraLarge)

    /** Hero containers, empty-state art tiles, the device-frame radius. */
    val hero = RoundedCornerShape(Corner.ExtraLargeIncreased)

    /** Bottom sheets round only their top corners, whatever radius is in play. */
    fun sheetTop(radius: Dp = Corner.ExtraLarge) =
        RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 0.dp, bottomEnd = 0.dp)
}

/**
 * `MaterialTheme.shapes`. Five slots are set here; the three Expressive slots are not, and
 * cannot be.
 *
 * `Shapes` does declare `largeIncreased`, `extraLargeIncreased` and `extraExtraLarge`, but the
 * eight-parameter constructor that accepts them is **internal** to `androidx.compose.material3`.
 * Only the five-parameter constructor is public, so app code cannot pass those three. Trying to
 * is a compile error: "Cannot access 'constructor(...): Shapes': it is internal".
 *
 * This costs nothing, and it is worth writing down why. Material's own defaults for those three
 * are `ShapeDefaults.LargeIncreased` = 20dp, `ShapeDefaults.ExtraLargeIncreased` = 32dp and
 * `ShapeDefaults.ExtraExtraLarge` = 48dp — the same values as [Corner.LargeIncreased],
 * [Corner.ExtraLargeIncreased] and [Corner.ExtraExtraLarge], checked against the Material 3
 * shape tokens. So `MaterialTheme.shapes.largeIncreased` already returns this app's intended
 * radius; leaving the slot unset changes no pixel.
 *
 * If a future Material 3 release makes that constructor public, or if `Shapes.copy(...)` becomes
 * usable without an experimental opt-in, set them explicitly then — not to change a value, but
 * so the theme states its own scale rather than inheriting one that happens to agree.
 */
val Shapes = Shapes(
    extraSmall = Stopped.extraSmall,
    small = Stopped.small,
    medium = Stopped.medium,
    large = Stopped.large,
    extraLarge = Stopped.extraLarge,
    // largeIncreased / extraLargeIncreased / extraExtraLarge are deliberately absent - the
    // constructor overload that takes them is internal to Material 3. See the docs above; their
    // defaults already equal this app's Corner values, so nothing renders differently. For a
    // shape outside these five slots, use Corner directly at the call site.
)

/**
 * Position of an item inside a grouped/sectioned list (spec 1d, "GROUPED LIST · 28 / 8 / 28").
 * [Single] is a group of exactly one row.
 */
enum class GroupedListPosition { Single, First, Middle, Last }

/**
 * Corner shape for one row of a grouped list, per [position].
 *
 * [outerRadius] is the group's outward-facing corners — the top of the first row, the bottom of
 * the last. [innerRadius] is every corner bordering the next row in the same group. The defaults
 * are the spec's 28/8; pass something else only for a deliberately different grouping.
 *
 * Use this rather than re-deriving the corner combination by hand at each call site: hand-rolled
 * versions are how a list ends up with three rows agreeing and the fourth not.
 */
fun groupedListItemShape(
    position: GroupedListPosition,
    outerRadius: Dp = Corner.ExtraLarge,
    innerRadius: Dp = Corner.Small,
): RoundedCornerShape =
    when (position) {
        GroupedListPosition.Single ->
            RoundedCornerShape(outerRadius)

        GroupedListPosition.First ->
            RoundedCornerShape(
                topStart = outerRadius,
                topEnd = outerRadius,
                bottomStart = innerRadius,
                bottomEnd = innerRadius,
            )

        GroupedListPosition.Middle ->
            RoundedCornerShape(innerRadius)

        GroupedListPosition.Last ->
            RoundedCornerShape(
                topStart = innerRadius,
                topEnd = innerRadius,
                bottomStart = outerRadius,
                bottomEnd = outerRadius,
            )
    }

/**
 * Shape morph — the one place it is allowed (spec 1d).
 *
 * The morph applies to the **switch thumb only**: the AMOLED toggle and keep-screen-on rows in
 * Settings, and the units segmented control. It runs `MaterialShapes.Circle` →
 * `MaterialShapes.Cookie4Sided` on press and back on release, driven by
 * [Motion.expressiveFastSpatial] — damping 0.6, stiffness 800 — so the thumb's shape and its
 * travel land together rather than finishing at different moments.
 *
 * Everywhere else is off limits, and the reason is not taste. Map zoom, compass, 3D and recenter
 * must never deform under the thumb: at 60 km/h a control that squashes reads as lag, not as
 * delight. Those controls change state through the state layer ([StateLayer]) instead, which
 * costs no geometry.
 *
 * When the morph is implemented, use `androidx.compose.material3.MaterialShapes` with `Morph`
 * rather than interpolating a `RoundedCornerShape` by hand — a hand-rolled interpolation between
 * a circle and a four-lobed cookie does not pass through the shapes the system expects.
 */
object ShapeMorph {
    /**
     * Resting and pressed thumb shapes, as the `MaterialShapes` names to pass to `Morph`.
     *
     * These are strings rather than shape objects on purpose: `MaterialShapes` is an
     * experimental Material 3 API, and binding this file to it would make the whole design
     * system fail to compile the day that API is renamed. The switch component resolves the
     * names at its own call site, where the opt-in annotation belongs.
     */
    const val RestingShapeName = "MaterialShapes.Circle"
    const val PressedShapeName = "MaterialShapes.Cookie4Sided"

    /** Damping of the spatial spring that drives the morph. Matches the thumb's own travel. */
    const val SpringDamping = Motion.Expressive.FastSpatialDamping

    /** Stiffness of that spring, so shape and position land together rather than separately. */
    const val SpringStiffness = Motion.Expressive.FastSpatialStiffness
}

/**
 * The zoom pair is ONE segmented control, not two buttons (spec 1q).
 *
 * At 3dp apart, two live 56dp targets had a visible seam that was too small to aim into and too
 * large to ignore: a press landing in it hit nothing. Joined into a single container with a
 * hairline divider and no gap, every press lands on a live target.
 *
 * Use [zoomInShape] and [zoomOutShape] for the two halves and draw a
 * [Spacing.segmentedDivider]-wide `outlineVariant` line between them. Do not re-derive these
 * corners at the call site, and do not reintroduce a gap.
 */
object SegmentedPair {
    /** Top half - outer corners on top, square where it meets its partner. */
    val zoomInShape = RoundedCornerShape(
        topStart = Corner.Medium,
        topEnd = Corner.Medium,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )

    /** Bottom half - square where it meets its partner, outer corners below. */
    val zoomOutShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = Corner.Medium,
        bottomEnd = Corner.Medium,
    )

    /** The container both halves sit in, when one is drawn behind them. */
    val containerShape = RoundedCornerShape(Corner.Medium)
}

/**
 * Maneuver glyph sizes (spec 4e). One glyph set — never a rotated arrow — drawn at three fixed
 * sizes depending on context. There is no in-between size.
 */
object ManeuverIconSize {
    val turnCard = 56.dp
    val stepList = 28.dp
    val inline = 20.dp
}
