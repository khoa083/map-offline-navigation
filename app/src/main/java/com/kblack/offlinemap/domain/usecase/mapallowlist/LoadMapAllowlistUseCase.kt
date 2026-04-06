package com.kblack.offlinemap.domain.usecase.mapallowlist

import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.MapAllowlistRepository

class LoadMapAllowlistUseCase(
    private val mapAllowlistRepository: MapAllowlistRepository,
) {
    suspend operator fun invoke(url: String): List<MapModel>? {
        return mapAllowlistRepository.loadMapAllowlist(url)
    }
}

