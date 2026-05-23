package com.kblack.offlinemap.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    val version  : Int = 0,
    val maintain : Boolean = false
)
