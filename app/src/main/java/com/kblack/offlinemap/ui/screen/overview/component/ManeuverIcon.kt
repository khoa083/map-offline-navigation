package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.ui.utils.NavigationInstructionFormat

/**
 * Renders the maneuver glyph for [sign] using the dedicated `ic_maneuver_*` vector set
 * (`res/drawable/`, ported 1:1 from the design spec's "4e Maneuver icon set").
 *
 * This is the ONLY place in the app that should decide which drawable represents a GraphHopper
 * [com.graphhopper.util.Instruction] sign. Screens must call this instead of reaching for
 * `Icons.Filled.ArrowUpward` + `Modifier.rotate(...)` directly, and must always pass a
 * theme-derived [tint] (e.g. `MaterialTheme.colorScheme.onSurface`, or a fixed token such as
 * `MaterialTheme.customColors.onTurnCardFixed` / `onSharpTurnAmberFixed` where the spec calls for
 * a fixed-regardless-of-theme treatment) — never a hardcoded `Color.White` or `Color.Black`. A
 * hardcoded tint is exactly the bug this component exists to remove: the same icon reading fine
 * on one screen/theme and disappearing on another because its color never adapted.
 *
 * The set is now complete for every sign this app can produce, roundabouts included. The
 * fallback branch below stays for the public-transit signs (`PT_START_TRIP`, `PT_TRANSFER`,
 * `PT_END_TRIP`), which have no glyph because this app has no transit UI, and as a guard against
 * any future GraphHopper sign we have not drawn — it renders a generic arrow rotated by heading
 * rather than crashing or drawing nothing.
 *
 * Do not treat that fallback as acceptable for a real driving manoeuvre. It was previously the
 * path taken by both roundabout signs, whose rotation is `0f`, so a roundabout rendered as a
 * plain upward arrow — indistinguishable from "continue straight". If a new driving sign ever
 * lands here, draw it rather than leaving it to the fallback.
 */
@Composable
fun ManeuverIcon(
    sign: Int,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = LocalContentColor.current,
) {
    val resId = NavigationInstructionFormat.iconRes(sign)
    if (resId != null) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = null,
            tint = tint,
            modifier = modifier,
        )
    } else {
        // Fallback only — see kdoc above. Remove this branch once every sign has a real glyph.
        Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = tint,
            modifier = modifier.rotate(NavigationInstructionFormat.rotationDegrees(sign)),
        )
    }
}
