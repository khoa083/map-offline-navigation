package com.kblack.offlinemap.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// todo: https://photon.komoot.io/

@JsonClass(generateAdapter = true)
data class GeoLocation(
    val type     : String,
    val features : List<Feature>
)

@JsonClass(generateAdapter = true)
data class Feature(
    val type       : String,
    val geometry   : Geometry,
    val properties : LocationProperties
)

@JsonClass(generateAdapter = true)
data class Geometry(
    val type: String,
    val coordinates: List<Double>
) {
    val lng: Double get() = coordinates.getOrNull(0) ?: 0.0
    val lat: Double get() = coordinates.getOrNull(1) ?: 0.0
}

// https://nominatim.org/release-docs/develop/api/Search/
@JsonClass(generateAdapter = true)
data class LocationProperties(
    @param:Json(name = "osm_id") val osmId: Long = 0L,
    @param:Json(name = "osm_value") val osmValue: String = "",
    val name: String = "",
    val street: String = "",
    val locality: String = "",
    val district: String = "",
    val city: String = "",
    val county: String = "",
    val state: String = "",
    val country: String = "",
    val postcode: String = ""
)
