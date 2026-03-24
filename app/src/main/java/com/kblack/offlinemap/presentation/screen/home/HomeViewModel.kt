package com.kblack.offlinemap.presentation.screen.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kblack.offlinemap.data.model.MapAllowlist
import com.kblack.offlinemap.data.remote.api.MapListRemoteDataSource
import com.kblack.offlinemap.data.repository.MapDownloadRepositoryImpl
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.repository.MapDownloadRepository
import com.kblack.offlinemap.presentation.ui.Constant.ALLOWLIST_URL
import com.kblack.offlinemap.presentation.ui.Constant.TMP_FILE_EXT
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class MapManagerUiState(
    val maps: List<MapModel> = emptyList(),
    val mapDownloadStatus: Map<String, MapDownloadStatus> = emptyMap(),
    val loadingMapAllowlist: Boolean = true,
    val loadingMapAllowlistError: String = "",
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remoteDataSource: MapListRemoteDataSource,
    private val downloadRepository: MapDownloadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {


    private val ALLOWLIST_FILENAME = "map_allowlist.json"

    private val externalFilesDir = context.getExternalFilesDir(null)
    protected val _uiState = MutableStateFlow(createEmptyUiState())
    val uiState = _uiState.asStateFlow()

    fun deleteMap(map: MapModel) {
        deleteDirFromExternalFilesDir(map.normalizedName)

        val curMapDownloadStatus = uiState.value.mapDownloadStatus.toMutableMap()
        curMapDownloadStatus[map.mapId] =
            MapDownloadStatus(status = MapDownloadStatusType.NOT_DOWNLOADED)

        _uiState.update {
            uiState.value.copy(mapDownloadStatus = curMapDownloadStatus)
        }
    }

    fun downloadMap(map: MapModel) {
        setDownloadStatus(
            curMap = map,
            status = MapDownloadStatus(status = MapDownloadStatusType.IN_PROGRESS),
        )

        deleteMap(map)

        downloadRepository.downloadMap(
            map = map,
            onStatusUpdated = ::setDownloadStatus,
        )
    }

    fun cancelDownloadMap(map: MapModel) {
        downloadRepository.cancelDownloadMap(map)
        deleteMap(map)
    }

    fun getMapUrlResponse(map: MapModel): Int {
        return try {
            val url = URL(map.url)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            connection.responseCode
        } catch (e: Exception) {
            -1
        }
    }

    fun setDownloadStatus(curMap: MapModel, status: MapDownloadStatus) {
        val curMapDownloadStatus = uiState.value.mapDownloadStatus.toMutableMap()
        curMapDownloadStatus[curMap.mapId] = status

        if (
            status.status == MapDownloadStatusType.FAILED ||
            status.status == MapDownloadStatusType.NOT_DOWNLOADED
        ) {
            deleteFileFromExternalFilesDir(curMap.downloadFileName)
        }

        _uiState.update { uiState.value.copy(mapDownloadStatus = curMapDownloadStatus) }
    }


    fun loadMapAllowlist() {
        _uiState.update {
            uiState.value.copy(loadingMapAllowlist = true, loadingMapAllowlistError = "")
        }

        viewModelScope.launch(IO) {
            try {
                var mapAllowlist: MapAllowlist?

                mapAllowlist = remoteDataSource.fetchMapAllowlist(ALLOWLIST_URL)

                if (mapAllowlist == null) {
                    mapAllowlist = readMapAllowlistFromDisk()
                } else {
                    saveMapAllowlistToDisk(mapAllowlist)
                }

                if (mapAllowlist == null) {
                    mapAllowlist = readMapAllowlistFromAssets()
                }

                if (mapAllowlist == null) {
                    _uiState.update {
                        uiState.value.copy(loadingMapAllowlistError = "Failed to load map list")
                    }
                    return@launch
                }

                val maps = mapAllowlist.maps
                    .map { it.toDomain() }

                val mapDownloadStatus = mutableMapOf<String, MapDownloadStatus>()
                for (map in maps) {
                    mapDownloadStatus[map.mapId] = getMapDownloadStatus(map)
                }

                _uiState.update {
                    uiState.value.copy(
                        loadingMapAllowlist = false,
                        maps = maps,
                        mapDownloadStatus = mapDownloadStatus,
                    )
                }

                processPendingDownloads(maps)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMapAllowlistToDisk(allowlist: MapAllowlist) {
        try {
            val file = File(externalFilesDir, ALLOWLIST_FILENAME)
            file.writeText(Gson().toJson(allowlist))
        } catch (e: Exception) {e.printStackTrace()}
    }

    private fun readMapAllowlistFromDisk(): MapAllowlist? {
        return try {
            val file = File(externalFilesDir, ALLOWLIST_FILENAME)
            if (file.exists()) {
                Gson().fromJson(file.readText(), MapAllowlist::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun readMapAllowlistFromAssets(): MapAllowlist? {
        return try {
            val content = context.assets.open("map_allowlist.json").bufferedReader().readText()
            Gson().fromJson(content, MapAllowlist::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearLoadMapAllowlistError() {
        TODO("nothing")
    }

    private fun processPendingDownloads(maps: List<MapModel>) {
        downloadRepository.cancelAll {
            viewModelScope.launch(Main) {
                for (map in maps) {
                    val downloadStatus = uiState.value.mapDownloadStatus[map.mapId]?.status
                    if (downloadStatus == MapDownloadStatusType.PARTIALLY_DOWNLOADED) {
                        downloadRepository.downloadMap(
                            map = map,
                            onStatusUpdated = ::setDownloadStatus,
                        )
                    }
                }
            }
        }
    }

    private fun getMapDownloadStatus(map: MapModel): MapDownloadStatus {

        var status = MapDownloadStatusType.NOT_DOWNLOADED
        var receivedBytes = 0L
        var totalBytes = 0L

        if (isMapPartiallyDownloaded(map)) {
            status = MapDownloadStatusType.PARTIALLY_DOWNLOADED
            val tmpFile = File(
                externalFilesDir,
                "${map.normalizedName}/${map.downloadFileName}.$TMP_FILE_EXT"
            )
            receivedBytes = tmpFile.length()
            totalBytes = map.totalBytes
        } else if (isMapDownloaded(map)) {
            status = MapDownloadStatusType.SUCCEEDED
        }

        return MapDownloadStatus(
            status = status,
            receivedBytes = receivedBytes,
            totalBytes = totalBytes,
        )
    }

    private fun isMapDownloaded(map: MapModel): Boolean {
        val pmtilesFile = File(externalFilesDir, "${map.normalizedName}/${map.pmtilesName}")
        return pmtilesFile.exists()
    }

    private fun isMapPartiallyDownloaded(map: MapModel): Boolean {
        val tmpFile = File(
            externalFilesDir,
            "${map.normalizedName}/${map.downloadFileName}.$TMP_FILE_EXT"
        )
        return tmpFile.exists()
    }

    private fun createEmptyUiState(): MapManagerUiState {
        return MapManagerUiState(
            maps = listOf(),
            mapDownloadStatus = mapOf(),
        )
    }

    private fun isFileInExternalFilesDir(fileName: String): Boolean {
        return externalFilesDir?.let { File(it, fileName).exists() } ?: false
    }

    private fun deleteFileFromExternalFilesDir(fileName: String) {
        if (isFileInExternalFilesDir(fileName)) {
            File(externalFilesDir, fileName).delete()
        }
    }

    private fun deleteDirFromExternalFilesDir(dir: String) {
        if (isFileInExternalFilesDir(dir)) {
            File(externalFilesDir, dir).deleteRecursively()
        }
    }

    fun getStyleJsonPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/style_runtime.json")
        return if (file.exists()) file.absolutePath else null
    }
    fun getGraphPath(map: MapModel): String? {
        val file = File(externalFilesDir, "${map.normalizedName}/graph-cache")
        return if (file.exists()) file.absolutePath else null
    }
}