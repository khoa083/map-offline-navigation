package com.kblack.offlinemap.models

data class GeoCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val bearing: Float? = null,
    val bearingAccuracyDegrees: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val time: Long = System.currentTimeMillis()
)
