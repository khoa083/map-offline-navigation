package com.kblack.offlinemap.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation — the six Material 3 levels, and what each one means in this app (spec 1o).
 *
 * Before this file existed the boards used ad-hoc box shadows and the codebase had no shared
 * notion of "how high is this surface", which is how two components that are conceptually at the
 * same depth end up with different shadows. Every elevated surface in the app now names a level
 * from [Elevation] instead of passing a raw dp.
 *
 * ## The level map, from the spec
 *
 * | Level | Value | Surface |
 * |-------|-------|---------|
 * | 0 | 0dp  | Map canvas, screen background, list rows |
 * | 1 | 1dp  | Search bar at rest, region cards |
 * | 2 | 3dp  | Map control cluster: zoom pair, compass, 3D |
 * | 3 | 6dp  | Recenter FAB, bottom sheets, notification card, any dragged element |
 * | 4 | 8dp  | Turn instruction card during navigation |
 * | 5 | 12dp | Dialogs: required update, permission blocked, download confirm |
 *
 * Level 4 is the highest *persistent* surface in the app: the turn card must never be visually
 * beneath anything while navigating. Level 5 is modal only — nothing at Level 5 is dismissible
 * by scrolling.
 *
 * ## Why AMOLED needs a different mechanism, not a different number
 *
 * On the warm light surface, elevation is carried by two cues pointing the same way: a soft
 * shadow *and* a container-colour step (surfaceContainerLow → surfaceContainerHighest). That
 * redundancy is why the light screens still read correctly in direct sunlight, where a shadow
 * alone washes out.
 *
 * On a true-black AMOLED background neither cue works. A shadow has nothing to fall on, and a
 * tonal lift is invisible below roughly 8% luminance. So on AMOLED this app expresses elevation
 * as **a 1dp outlineVariant border plus a container step** — #000000 → #080B0A → #101614 →
 * #18201D → #202A26 — and drops shadow entirely. Levels 4 and 5 add a second cue rather than a
 * bigger nothing: Level 4 uses a filled primaryContainer surface (the night turn card), Level 5
 * a full-screen scrim at 60%.
 *
 * Prefer [Modifier.elevatedSurface] over assembling shadow, border and background by hand: it
 * already encodes that split, so a component written once behaves correctly in both schemes.
 */
object Elevation {

    /** Level 0 — flat on the surface. Grouping comes from shape and container colour, not depth. */
    val Level0: Dp = 0.dp

    /** Level 1 — lifts off the map without casting a visible edge. */
    val Level1: Dp = 1.dp

    /** Level 2 — reads as tappable hardware over the map. Rises to [Level3] while pressed. */
    val Level2: Dp = 3.dp

    /** Level 3 — recenter FAB, sheets, notification card, and every dragged element. */
    val Level3: Dp = 6.dp

    /** Level 4 — the turn card. Highest persistent surface; nothing may sit above it. */
    val Level4: Dp = 8.dp

    /** Level 5 — modal dialogs only. */
    val Level5: Dp = 12.dp

    /** Scrim opacity behind a Level 5 modal on AMOLED, where a shadow cannot do the work. */
    const val AmoledModalScrimAlpha = 0.60f

    /** Scrim opacity behind a modal on the light scheme (spec 1a: `scrim #000000 @ 32%`). */
    const val LightModalScrimAlpha = 0.32f

    /**
     * Shadow to draw for [level] in the current scheme.
     *
     * Returns [Level0] on AMOLED regardless of the level asked for: a shadow on #000000 is
     * wasted overdraw. Pair it with [borderWidth] and a container-colour step instead.
     */
    fun shadowElevation(level: Dp, isAmoled: Boolean): Dp = if (isAmoled) Level0 else level

    /**
     * Border width to draw for [level] in the current scheme.
     *
     * AMOLED gets a 1dp `outlineVariant` hairline for any level above 0 — the cue that replaces
     * the shadow. The light scheme gets none, because there the shadow plus the container step
     * already carry the depth and a border would read as a second, competing edge.
     */
    fun borderWidth(level: Dp, isAmoled: Boolean): Dp =
        if (isAmoled && level > Level0) 1.dp else 0.dp
}

/**
 * The container colour that carries [level] on the current scheme, as a tonal step.
 *
 * This is the second half of the elevation cue and the *only* half on AMOLED. Read it instead of
 * reaching for a specific `surfaceContainer*` role at the call site, so a component's depth and
 * its background can never drift apart.
 *
 * Levels 4 and 5 deliberately share `surfaceContainerHighest`: the tonal ladder has five steps
 * and the level scale has six, and a modal at Level 5 is already separated from everything below
 * it by a scrim, so it does not need a sixth tone to be legible.
 */
@Composable
@ReadOnlyComposable
fun elevatedContainerColor(level: Dp): Color {
    val scheme = MaterialTheme.colorScheme
    return when (level) {
        Elevation.Level0 -> scheme.surface
        Elevation.Level1 -> scheme.surfaceContainerLow
        Elevation.Level2 -> scheme.surfaceContainer
        Elevation.Level3 -> scheme.surfaceContainerHigh
        Elevation.Level4 -> scheme.surfaceContainerHighest
        Elevation.Level5 -> scheme.surfaceContainerHighest
        else -> scheme.surface
    }
}

/**
 * Applies the whole elevation treatment for [level] in one place: shadow (light only), container
 * colour, and the AMOLED hairline border — clipped to [shape].
 *
 * ```
 * Box(
 *     Modifier
 *         .elevatedSurface(Elevation.Level2, Moving.control, isAmoled)
 *         .size(LocalSpacing.current.controlEmphasis)
 * )
 * ```
 *
 * [isAmoled] is passed in rather than read from a CompositionLocal on purpose: "dark theme" and
 * "AMOLED" are separate ideas in this app — a standard dark theme keeps #101614 as its
 * background and still wants shadows, while the AMOLED variant is true black and does not. A
 * component must be told which one it is in, not guess from `isSystemInDarkTheme()`.
 */
@Composable
fun Modifier.elevatedSurface(
    level: Dp,
    shape: Shape,
    isAmoled: Boolean,
    containerColor: Color? = null,
): Modifier {
    val container = containerColor ?: elevatedContainerColor(level)
    val shadow = Elevation.shadowElevation(level, isAmoled)
    val border = Elevation.borderWidth(level, isAmoled)
    val outline = MaterialTheme.colorScheme.outlineVariant

    var m = this
    if (shadow > Elevation.Level0) m = m.shadow(shadow, shape)
    m = m.clip(shape).background(container)
    if (border > 0.dp) m = m.border(border, outline, shape)
    return m
}
