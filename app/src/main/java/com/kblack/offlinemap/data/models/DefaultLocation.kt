package com.kblack.offlinemap.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DefaultLocation(
    val name: String,
    val lat: Double,
    val lng: Double,
)
