package com.kblack.offlinemap.models

enum class RouteWeighting(val key: String) {
    Fastest("fastest"),
    Shortest("shortest")
}

enum class TravelMode(val vehicleKey: String) {
    Foot("foot"),
    Motorcycle("motorcycle"),
    Car("car")
}

data class RoutingOptions(
    val travelMode: TravelMode = TravelMode.Motorcycle,
    val weighting: RouteWeighting = RouteWeighting.Fastest,
    val instructionsEnabled: Boolean = true,
    val includeSpeedDetails: Boolean = false,
    val allowDirectFallback: Boolean = true
)
