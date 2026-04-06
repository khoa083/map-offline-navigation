package com.kblack.offlinemap.domain.repository

import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapModel

interface MapDownloadRepository {

    fun downloadMap(
        map: MapModel,
        onStatusUpdated: (map: MapModel, status: MapDownloadStatus) -> Unit,
    )

    fun cancelDownloadMap(map: MapModel)

    fun cancelAll(onComplete: () -> Unit)

    fun getLocalMapStatus(map: MapModel): MapDownloadStatus

    fun deleteMap(map: MapModel)

    fun getStyleJsonPath(map: MapModel): String?
    fun getGraphPath(map: MapModel): String?

}