package com.kblack.offlinemap.ui.utils

import androidx.annotation.DrawableRes
import com.graphhopper.util.Instruction
import com.kblack.offlinemap.R

//todo: FIXME
object NavigationInstructionFormat {

    fun rotationDegrees(sign: Int): Float {
        return when (sign) {
            Instruction.TURN_SLIGHT_LEFT -> -35f
            Instruction.TURN_LEFT -> -90f
            Instruction.TURN_SHARP_LEFT -> -135f
            Instruction.KEEP_LEFT -> -20f
            Instruction.TURN_SLIGHT_RIGHT -> 35f
            Instruction.TURN_RIGHT -> 90f
            Instruction.TURN_SHARP_RIGHT -> 135f
            Instruction.KEEP_RIGHT -> 20f
            Instruction.U_TURN_LEFT,
            Instruction.U_TURN_RIGHT,
            Instruction.U_TURN_UNKNOWN -> 180f
            Instruction.USE_ROUNDABOUT,
            Instruction.LEAVE_ROUNDABOUT,
            Instruction.CONTINUE_ON_STREET,
            Instruction.FINISH,
            Instruction.REACHED_VIA,
            Instruction.UNKNOWN,
            Instruction.IGNORE,
            Instruction.PT_START_TRIP,
            Instruction.PT_TRANSFER,
            Instruction.PT_END_TRIP -> 0f

            else -> 0f
        }
    }

    fun title(sign: Int, name: String): String {
        val action = when (sign) {
            Instruction.UNKNOWN -> "Continue"
            Instruction.U_TURN_UNKNOWN -> "Make a U-turn"
            Instruction.U_TURN_LEFT -> "Make a U-turn left"
            Instruction.KEEP_LEFT -> "Keep left"
            Instruction.LEAVE_ROUNDABOUT -> "Leave roundabout"
            Instruction.TURN_SHARP_LEFT -> "Turn sharp left"
            Instruction.TURN_LEFT -> "Turn left"
            Instruction.TURN_SLIGHT_LEFT -> "Turn slight left"
            Instruction.CONTINUE_ON_STREET -> "Continue on"
            Instruction.TURN_SLIGHT_RIGHT -> "Turn slight right"
            Instruction.TURN_RIGHT -> "Turn right"
            Instruction.TURN_SHARP_RIGHT -> "Turn sharp right"
            Instruction.FINISH -> "Arrive"
            Instruction.REACHED_VIA -> "Reached via point"
            Instruction.USE_ROUNDABOUT -> "Enter roundabout"
            Instruction.KEEP_RIGHT -> "Keep right"
            Instruction.U_TURN_RIGHT -> "Make a U-turn right"
            Instruction.PT_START_TRIP -> "Start trip"
            Instruction.PT_TRANSFER -> "Transfer"
            Instruction.PT_END_TRIP -> "End trip"
            Instruction.IGNORE -> "Continue"
            else -> "Continue"
        }

        val streetName = name.trim()
        return if (streetName.isEmpty()) action else "$action $streetName"
    }

    /**
     * Dedicated maneuver glyph for [sign], ported 1:1 from the design spec's "4e Maneuver icon
     * set" (`res/drawable/ic_maneuver_*.xml`) — replaces the old "rotate one generic arrow"
     * approach for every sign that has a real drawn glyph.
     *
     * The roundabout pair ([Instruction.USE_ROUNDABOUT] / [Instruction.LEAVE_ROUNDABOUT]) is now
     * drawn and wired — see the header comments in `ic_maneuver_use_roundabout.xml` for the
     * construction rules (ring mandatory, approach road tangent not radial, ring at full weight
     * in both, the pair told apart by arrowhead position). Before those assets existed this
     * function returned `null` for both signs, and [ManeuverIcon] fell back to a generic arrow
     * rotated by [rotationDegrees] — which is `0f` for both roundabout signs, so a roundabout
     * rendered on-device as a plain upward arrow: pixel-identical to "continue straight". That
     * was a safety defect, not a cosmetic gap.
     *
     * Callers must still handle `null`: the public-transit signs below have no glyph. See
     * [ManeuverIcon] in `ui/screen/overview/component/ManeuverIcon.kt`, which already does.
     *
     * Public-transit signs ([Instruction.PT_START_TRIP] etc.) also return `null` — this app has
     * no transit UI to draw them for.
     */
    @DrawableRes
    fun iconRes(sign: Int): Int? = when (sign) {
        Instruction.UNKNOWN,
        Instruction.CONTINUE_ON_STREET,
        Instruction.IGNORE -> R.drawable.ic_maneuver_continue_straight

        Instruction.KEEP_LEFT -> R.drawable.ic_maneuver_keep_left
        Instruction.KEEP_RIGHT -> R.drawable.ic_maneuver_keep_right
        Instruction.TURN_SLIGHT_LEFT -> R.drawable.ic_maneuver_turn_slight_left
        Instruction.TURN_SLIGHT_RIGHT -> R.drawable.ic_maneuver_turn_slight_right
        Instruction.TURN_LEFT -> R.drawable.ic_maneuver_turn_left
        Instruction.TURN_RIGHT -> R.drawable.ic_maneuver_turn_right
        Instruction.TURN_SHARP_LEFT -> R.drawable.ic_maneuver_turn_sharp_left
        Instruction.TURN_SHARP_RIGHT -> R.drawable.ic_maneuver_turn_sharp_right
        Instruction.U_TURN_LEFT -> R.drawable.ic_maneuver_uturn_left
        Instruction.U_TURN_RIGHT -> R.drawable.ic_maneuver_uturn_right
        Instruction.U_TURN_UNKNOWN -> R.drawable.ic_maneuver_uturn_unknown
        Instruction.FINISH -> R.drawable.ic_maneuver_arrive
        Instruction.REACHED_VIA -> R.drawable.ic_maneuver_reached_via

        Instruction.USE_ROUNDABOUT -> R.drawable.ic_maneuver_use_roundabout
        Instruction.LEAVE_ROUNDABOUT -> R.drawable.ic_maneuver_leave_roundabout

        else -> null
    }

    /**
     * Maneuvers the spec marks as needing the rider to slow before committing — sharp turns and
     * all three U-turns. These get the fixed warning treatment ([sharpTurnAmberFixed] /
     * [onSharpTurnAmberFixed][com.kblack.offlinemap.ui.theme.CustomColors]) regardless of
     * light/AMOLED theme, so the color — not the neutral glyph — carries the warning. Every other
     * maneuver — roundabouts included — stays in the normal neutral/theme-following treatment.
     * Entering a roundabout is a yield, not a manoeuvre that needs the rider to slow abruptly,
     * so it deliberately does NOT get the amber warning treatment.
     */
    fun isWarningManeuver(sign: Int): Boolean = when (sign) {
        Instruction.TURN_SHARP_LEFT,
        Instruction.TURN_SHARP_RIGHT,
        Instruction.U_TURN_LEFT,
        Instruction.U_TURN_RIGHT,
        Instruction.U_TURN_UNKNOWN -> true

        else -> false
    }
}
