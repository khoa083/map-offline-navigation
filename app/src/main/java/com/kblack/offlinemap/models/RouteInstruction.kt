package com.kblack.offlinemap.models

data class RouteInstruction(
    val sign: Int,
    val name: String,
    val distanceMeters: Double,
    val durationMillis: Long,
    val points: List<GeoCoordinate>
)
