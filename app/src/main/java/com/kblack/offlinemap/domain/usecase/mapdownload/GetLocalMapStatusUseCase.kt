package com.kblack.offlinemap.domain.usecase.mapdownload

import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.MapDownloadRepository

class GetLocalMapStatusUseCase(
    private val mapDownloadRepository: MapDownloadRepository,
) {
    operator fun invoke(map: MapModel): MapDownloadStatus {
        return mapDownloadRepository.getLocalMapStatus(map)
    }
}

