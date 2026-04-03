package com.kblack.offlinemap.domain.models

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val bearing: Float? = null,
    val bearingAccuracyDegrees: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val time: Long = System.currentTimeMillis(),
) {
    val isStale: Boolean
        get() = System.currentTimeMillis() - time > 30_000

    val hasGoodAccuracy: Boolean
        get() = accuracy < 50f

    val hasBearing: Boolean
        get() = bearing != null && bearing >= 0f

    val hasBearingAccuracy: Boolean
        get() = bearingAccuracyDegrees != null && bearingAccuracyDegrees >= 0f
}
