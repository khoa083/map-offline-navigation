package com.kblack.offlinemap.data.model

import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.ui.Constant.BASE_HUGGINGFACE_URL

data class MapAllowlist(
    val maps: List<MapListResponse> = emptyList()
)

data class MapListResponse(
    var mapId       : String  = "",
    var name        : String  = "",
    var time        : String  = "",
    var description : String  = "",
    var sizeInBytes : Long     = 0L,
    var continent   : String  = "",
    var allow       : Boolean = true,
) {
    fun toDomain(): MapModel = MapModel(
        mapId = mapId,
        name = name,
        time = time,
        description = description,
        sizeInBytes = sizeInBytes,
        continent = continent,
        allow = allow,
        normalizedName = "${mapId}_map",
        downloadFileName = "${mapId}_map.tar.zst",
        pmtilesName = "${mapId}.pmtiles",
        url = "${BASE_HUGGINGFACE_URL}${mapId}_map.tar.zst",
        totalBytes = sizeInBytes,
    )
}
