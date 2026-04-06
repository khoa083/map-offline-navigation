package com.kblack.offlinemap.domain.usecase.mapdownload

import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.MapDownloadRepository

class DeleteMapUseCase(
    private val mapDownloadRepository: MapDownloadRepository,
) {
    operator fun invoke(map: MapModel) {
        mapDownloadRepository.deleteMap(map)
    }
}

