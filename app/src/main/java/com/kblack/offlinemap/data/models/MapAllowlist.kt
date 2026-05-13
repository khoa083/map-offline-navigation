package com.kblack.offlinemap.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MapAllowlist(
    val maps: List<MapListResponse> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MapListResponse(
    var mapId       : String  = "",
    var name        : String  = "",
    var time        : String  = "",
    var description : String  = "",
    var sizeInBytes : Long     = 0L,
    var continent   : String  = "",
    var allow       : Boolean = true,
)
