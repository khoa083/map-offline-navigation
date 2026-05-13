package com.kblack.offlinemap.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// todo: https://photon.komoot.io/

@JsonClass(generateAdapter = true)
data class GeoLocation(
    val type: String,
    val features: List<Feature>
)

@JsonClass(generateAdapter = true)
data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: LocationProperties
)

@JsonClass(generateAdapter = true)
data class Geometry(
    val type: String,
    val coordinates: List<Double>
) {
    val lng: Double get() = coordinates.getOrNull(0) ?: 0.0
    val lat: Double get() = coordinates.getOrNull(1) ?: 0.0
}

@JsonClass(generateAdapter = true)
data class LocationProperties(
    @param:Json(name = "osm_value") val osmValue: String = "",
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "street") val street: String = "",
    @param:Json(name = "locality") val locality: String = "",
    @param:Json(name = "district") val district: String = "",
    @param:Json(name = "city") val city: String = "",
    @param:Json(name = "country") val country: String = "",
    @param:Json(name = "postcode") val postcode: String = ""
)
