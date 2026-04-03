package com.kblack.offlinemap.presentation.ui

import com.graphhopper.util.Instruction

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
}