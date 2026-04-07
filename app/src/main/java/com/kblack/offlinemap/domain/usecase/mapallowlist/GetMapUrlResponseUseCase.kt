package com.kblack.offlinemap.domain.usecase.mapallowlist

import com.kblack.offlinemap.domain.repository.MapAllowlistRepository

class GetMapUrlResponseUseCase(
    private val mapAllowlistRepository: MapAllowlistRepository,
) {
    operator fun invoke(url: String): Int {
        return mapAllowlistRepository.getMapUrlResponse(url)
    }
}


