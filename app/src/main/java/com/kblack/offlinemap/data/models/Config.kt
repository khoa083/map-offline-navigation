package com.kblack.offlinemap.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Config(
    val version  : Int = 0,
    val maintain : Boolean = false,
    @param:Json(name = "force_update")
    val forceUpdate: Boolean = false,
)
