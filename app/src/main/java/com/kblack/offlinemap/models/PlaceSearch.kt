package com.kblack.offlinemap.models

data class PlaceSearch(
    val osmValue: String,
    val name: String,
    val street: String,
    val locality: String,
    val district: String,
    val city: String,
    val country: String,
    val postcode: String,
    val lat: Double,
    val lng: Double
)
