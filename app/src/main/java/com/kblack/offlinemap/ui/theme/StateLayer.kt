package com.kblack.offlinemap.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Interaction states — the Material 3 state-layer opacities, and this app's rules for using them
 * (spec 1d, "INTERACTION STATES").
 *
 * The state layer is the control's **on-** colour drawn over its container at the opacity below.
 * It is not a separate palette: `onSurface` at 10% over `surfaceContainer` is the pressed state
 * of a surface-coloured control; `onPrimary` at 10% over `primary` is the pressed state of a
 * filled button. Reading the opacity from here rather than typing `0.1f` at a call site is what
 * keeps twenty controls agreeing with each other.
 *
 * Values are the Material 3 tokens, not approximations:
 *
 * | State   | Opacity |
 * |---------|---------|
 * | hover   | 0.08 |
 * | focus   | 0.10 |
 * | pressed | 0.10 |
 * | dragged | 0.16 |
 *
 * Disabled is deliberately **not** a state layer: it reduces the container to 12% and the
 * content to 38% of `onSurface`. Drawing a layer on top of a disabled control makes it darker,
 * not obviously inert, which is the opposite of what disabled has to communicate.
 *
 * ## Two app-specific rules that are easy to get wrong
 *
 * 1. **Dragged is the only state that changes elevation.** It draws the 0.16 layer *and* raises
 *    the element to [Elevation.Level3]. It applies to the sheet drag handle and to a draggable
 *    map pin — nothing else in this app is draggable.
 * 2. **Focus is a ring, not a ripple.** See [focusRing]. Ripple stays on press only, where its
 *    expansion usefully marks the touch point. A keyboard or switch-access user needs a mark
 *    that persists while focus rests on the control, which a ripple by definition does not give.
 *
 * Hover is specified for completeness. It fires for stylus and mouse on large-screen Android; it
 * never fires for touch, so it is not a substitute for a pressed state.
 */
object StateLayer {

    /** Pointer or stylus is over the control. Never fires for touch. */
    const val Hover = 0.08f

    /** Control holds input focus — keyboard, D-pad or switch access. Pairs with [focusRing]. */
    const val Focus = 0.10f

    /** Control is being pressed. Same opacity as focus, different trigger. */
    const val Pressed = 0.10f

    /** Control is being dragged. The only state that also changes elevation. */
    const val Dragged = 0.16f

    /** Disabled container opacity — applied to the container colour, with no state layer. */
    const val DisabledContainer = 0.12f

    /** Disabled content opacity — applied to `onSurface` for text and icons. */
    const val DisabledContent = 0.38f

    /**
     * Focus ring stroke width. Decided once, app-wide: a 3dp ring in `primary`.
     *
     * Drawn **inset** rather than outset so it can never be clipped by a parent that happens to
     * have `clipToBounds` — a focus indicator that is invisible on exactly the controls sitting
     * at the edge of a scroll container is worse than none, because it is invisible only
     * sometimes.
     */
    val FocusRingWidth: Dp = 3.dp

    /** How far inside the control's own bounds the focus ring is drawn. */
    val FocusRingInset: Dp = 2.dp

    /** Elevation a control rises to while dragged. Everything else keeps its resting level. */
    val DraggedElevation: Dp = Elevation.Level3
}

/**
 * The single state-layer opacity that should be drawn for [source] right now, or `0f` at rest.
 *
 * Precedence is dragged > pressed > focused > hovered, matching how Material resolves
 * overlapping states: the most active interaction wins rather than the opacities summing. Two
 * layers stacked would read as a third, undefined state.
 *
 * Disabled is not handled here on purpose — see [StateLayer]: it is a container/content opacity
 * change, not a layer, so it belongs to a component's colour resolution rather than to its
 * interaction state.
 */
@Composable
fun stateLayerAlpha(source: InteractionSource): Float {
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    val hovered by source.collectIsHoveredAsState()
    val dragged by source.collectIsDraggedAsState()

    return when {
        dragged -> StateLayer.Dragged
        pressed -> StateLayer.Pressed
        focused -> StateLayer.Focus
        hovered -> StateLayer.Hover
        else -> 0f
    }
}

/**
 * Draws the Material 3 state layer for [source] over this element, in [color] — which must be
 * the control's **on-** colour, not an arbitrary tint.
 *
 * Draw order matters and is handled here: the layer goes *over* the container and *under* the
 * content, so a pressed icon is not washed out by its own state layer. That is why this uses
 * `drawWithContent` rather than a background.
 *
 * ```
 * val interaction = remember { MutableInteractionSource() }
 * Box(
 *     Modifier
 *         .size(LocalSpacing.current.controlEmphasis)
 *         .clip(Moving.control)
 *         .background(MaterialTheme.colorScheme.surfaceContainer)
 *         .stateLayer(interaction, MaterialTheme.colorScheme.onSurface)
 *         .clickable(interaction, indication = null) { /* … */ }
 * )
 * ```
 *
 * Pass `indication = null` to `clickable` when using this, or the platform ripple draws a
 * second, competing layer on press.
 */
@Composable
fun Modifier.stateLayer(
    source: InteractionSource,
    color: Color,
): Modifier {
    val alpha = stateLayerAlpha(source)
    return this.drawWithContent {
        drawContent()
        if (alpha > 0f) drawRect(color = color, alpha = alpha)
    }
}

/**
 * Draws the app's focus ring when [source] holds focus: an inset 3dp ring in [color], defaulting
 * to `primary`.
 *
 * Decided once here rather than per screen, because keyboard and switch-access users need every
 * focusable in the app to mark itself the same way. See [StateLayer.FocusRingWidth] for why the
 * ring is inset rather than outset.
 *
 * [shape] should match the control's own shape — [Moving.control] for a map control,
 * `CircleShape` for the recenter FAB — so the ring traces the control rather than boxing it.
 *
 * Apply this *after* sizing and *before* the click handler, and note that it insets its content
 * by [StateLayer.FocusRingInset] only while focused; give the control a fixed size so that inset
 * does not reflow its contents when focus arrives.
 */
@Composable
fun Modifier.focusRing(
    source: InteractionSource,
    shape: Shape,
    color: Color = MaterialTheme.colorScheme.primary,
): Modifier {
    val focused by source.collectIsFocusedAsState()
    return if (focused) {
        this
            .padding(StateLayer.FocusRingInset)
            .border(StateLayer.FocusRingWidth, color, shape)
    } else {
        this
    }
}
