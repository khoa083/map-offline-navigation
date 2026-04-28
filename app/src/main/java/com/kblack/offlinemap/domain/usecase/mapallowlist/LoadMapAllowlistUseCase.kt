package com.kblack.offlinemap.domain.usecase.mapallowlist

import com.kblack.offlinemap.data.utils.Constant.ALLOWLIST_URL
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.MapAllowlistRepository

class LoadMapAllowlistUseCase(
    private val mapAllowlistRepository: MapAllowlistRepository,
) {
    suspend operator fun invoke(): List<MapModel>? {
        return mapAllowlistRepository.loadMapAllowlist(ALLOWLIST_URL)
    }
}

