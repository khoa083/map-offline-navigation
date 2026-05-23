package com.kblack.offlinemap.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DefaultLocation(
    val name  : String = "",
    val lat   : Double = 0.0,
    val lng   : Double = 0.0,
)
