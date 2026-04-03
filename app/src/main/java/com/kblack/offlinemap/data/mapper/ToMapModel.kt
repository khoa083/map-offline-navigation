package com.kblack.offlinemap.data.mapper

import com.kblack.offlinemap.data.model.MapListResponse
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.ui.Constant.BASE_HUGGINGFACE_URL

fun MapListResponse.toDomain(): MapModel = MapModel(
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