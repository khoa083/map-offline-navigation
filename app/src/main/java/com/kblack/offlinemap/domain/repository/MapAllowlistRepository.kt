package com.kblack.offlinemap.domain.repository

import com.kblack.offlinemap.domain.models.MapModel


interface MapAllowlistRepository {
    suspend fun loadMapAllowlist(url: String): List<MapModel>?
}

