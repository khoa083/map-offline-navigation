package com.kblack.offlinemap.domain.models

data class MapInitializationStatus(
    val status: MapInitializationStatusType,
    var error: String = "",
)

enum class MapInitializationStatusType {
    NOT_INITIALIZED,
    INITIALIZING,
    INITIALIZED,
    ERROR,
}

enum class MapDownloadStatusType {
    NOT_DOWNLOADED,
    PARTIALLY_DOWNLOADED,
    IN_PROGRESS,
    UNZIPPING,
    SUCCEEDED,
    FAILED,
}

data class MapDownloadStatus(
    val status: MapDownloadStatusType,
    val totalBytes: Long = 0,
    val receivedBytes: Long = 0,
    val errorMessage: String = "",
    val bytesPerSecond: Long = 0,
    val remainingMs: Long = 0,
)


data class MapModel(
    val mapId: String = "",
    val name: String = "",
    val time: String = "",
    val description: String = "",
    val sizeInBytes: Long = 0L,
    val continent: String = "",
    val allow: Boolean = true,
    val localFileRelativeDirPathOverride: String = "",
    val normalizedName: String = "",
    val downloadFileName: String = "",
    val pmtilesName: String = "",
    val url: String = "",
    val totalBytes: Long = 0L,
)