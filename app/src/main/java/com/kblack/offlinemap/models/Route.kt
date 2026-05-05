package com.kblack.offlinemap.models

data class RoutePathDetail(
    val firstIndex: Int,
    val lastIndex: Int,
    val value: String
)

data class Route(
    val distanceMeters: Double,
    val durationMillis: Long,
    val points: List<GeoCoordinate>,
    val instructions: List<RouteInstruction>,
    val speedDetails: Map<String, List<RoutePathDetail>>,
    val isDirectFallback: Boolean,
    val debugInfo: String
)
