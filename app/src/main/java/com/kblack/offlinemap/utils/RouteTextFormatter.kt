package com.kblack.offlinemap.utils

import kotlin.math.roundToInt

object RouteTextFormatter {

    private const val METERS_PER_KM = 1000.0
    private const val METERS_PER_FEET = 0.3048
    private const val METERS_PER_MILE = 1609.344

    fun formatDistanceMeters(distanceMeters: Double, useImperial: Boolean = false): String {
        val safeMeters = distanceMeters.coerceAtLeast(0.0)

        if (!useImperial) {
            if (safeMeters < METERS_PER_KM) {
                return "${safeMeters.roundToInt()} meter"
            }
            val kmTruncated = truncateToSingleDecimal(safeMeters / METERS_PER_KM)
            return "$kmTruncated km"
        }

        if (safeMeters < METERS_PER_MILE) {
            val feet = (safeMeters / METERS_PER_FEET).roundToInt()
            return "$feet feet"
        }

        val milesTruncated = truncateToSingleDecimal(safeMeters / METERS_PER_MILE)
        return "$milesTruncated mi"
    }

    fun formatDurationMillis(durationMillis: Long): String {
        val minutes = (durationMillis / 60000.0).roundToInt().coerceAtLeast(0)
        if (minutes < 60) {
            return "$minutes min"
        }
        val hours = minutes / 60
        val mins = minutes % 60
        return "$hours h: $mins m"
    }

    private fun truncateToSingleDecimal(value: Double): Float {
        return (value * 10).toInt() / 10f
    }
}