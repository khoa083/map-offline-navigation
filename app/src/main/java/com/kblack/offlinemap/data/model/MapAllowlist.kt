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
)
