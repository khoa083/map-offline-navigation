package com.kblack.offlinemap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kblack.offlinemap.BuildConfig
import com.kblack.offlinemap.data.repository.GhRepository
import com.kblack.offlinemap.data.repository.MapDownloadRepository
import com.kblack.offlinemap.data.repository.PlaceSearchRepository
import com.kblack.offlinemap.models.MapDownloadStatus
import com.kblack.offlinemap.models.MapDownloadStatusType
import com.kblack.offlinemap.models.MapModel
import com.kblack.offlinemap.utils.UpdateChecker
import com.kblack.offlinemap.utils.toVersionName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapManagerUiState(
    val maps: List<MapModel> = emptyList(),
    val mapDownloadStatus: Map<String, MapDownloadStatus> = emptyMap(),
    val loadingMapAllowlist: Boolean = true,
    val loadingMapAllowlistError: String? = null,

    val isShowDialogUpdate: Boolean = false,
    val versionUpdate: String? = null,
    val isShowDialogError: String? = null,
)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mapDownloadRepository: MapDownloadRepository,
    private val ghRepository: GhRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapManagerUiState())
    val uiState = _uiState.asStateFlow()

    fun deleteMap(map: MapModel) {
        mapDownloadRepository.deleteMap(map)

        val curMapDownloadStatus = uiState.value.mapDownloadStatus.toMutableMap()
        curMapDownloadStatus[map.mapId] =
            MapDownloadStatus(status = MapDownloadStatusType.NOT_DOWNLOADED)

        _uiState.update {
            it.copy(mapDownloadStatus = curMapDownloadStatus)
        }
    }

    fun downloadMap(map: MapModel) {
        setDownloadStatus(
            map = map,
            status = MapDownloadStatus(status = MapDownloadStatusType.IN_PROGRESS),
        )

        deleteMap(map)

        mapDownloadRepository.downloadMap(
            map = map,
            onStatusUpdated = ::setDownloadStatus,
        )
    }

    fun cancelDownloadMap(map: MapModel) {
        mapDownloadRepository.cancelDownloadMap(map)
        deleteMap(map)
    }

    fun getMapUrlResponse(map: MapModel): Int {
        return ghRepository.getMapUrlResponse(map.url)
    }

    private fun setDownloadStatus(map: MapModel, status: MapDownloadStatus) {
        val curMapDownloadStatus = uiState.value.mapDownloadStatus.toMutableMap()
        curMapDownloadStatus[map.mapId] = status

        if (
            status.status == MapDownloadStatusType.FAILED ||
            status.status == MapDownloadStatusType.NOT_DOWNLOADED
        ) {
            mapDownloadRepository.deleteMap(map)
        }

        _uiState.update { it.copy(mapDownloadStatus = curMapDownloadStatus) }
    }

    fun loadMapAllowlist() {
        _uiState.update {
            it.copy(loadingMapAllowlist = true, loadingMapAllowlistError = null)
        }

        viewModelScope.launch(IO) {
            try {
                val maps = ghRepository.loadMapAllowlist()
                if (maps == null) {
                    _uiState.update {
                       it.copy(
                            loadingMapAllowlist = false,
                            loadingMapAllowlistError = "Failed to load map list")
                    }
                    return@launch
                }

                val mapDownloadStatus = mutableMapOf<String, MapDownloadStatus>()
                for (map in maps) {
                    mapDownloadStatus[map.mapId] = mapDownloadRepository.getLocalMapStatus(map)
                }

                _uiState.update {
                    it.copy(
                        loadingMapAllowlist = false,
                        maps = maps,
                        mapDownloadStatus = mapDownloadStatus,
                    )
                }

                processPendingDownloads(maps)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loadingMapAllowlist = false,
                        loadingMapAllowlistError = e.message ?: "Failed to load map list"
                    )
                }
            }
        }
    }

    fun checkUpdate() {
        viewModelScope.launch(IO) {
            try {
                val config = ghRepository.getConfig()
                _uiState.update {
                    it.copy(
                        isShowDialogUpdate = UpdateChecker().isUpdateApp(config),
                        versionUpdate = "v${(config?.version)?.toVersionName()}-${config?.version}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isShowDialogError = "Failed to check for updates")
                }
            }
        }
    }

    fun closeUpdate(){
        _uiState.update {
            it.copy(isShowDialogUpdate = false, versionUpdate = null)
        }
    }

    fun clearLoadMapAllowlistError() {
        _uiState.update {
            it.copy(loadingMapAllowlistError = null)
        }
    }

    private fun processPendingDownloads(maps: List<MapModel>) {
        mapDownloadRepository.cancelAll {
            viewModelScope.launch(Main) {
                for (map in maps) {
                    val downloadStatus = uiState.value.mapDownloadStatus[map.mapId]?.status
                    if (downloadStatus == MapDownloadStatusType.PARTIALLY_DOWNLOADED) {
                        mapDownloadRepository.downloadMap(
                            map = map,
                            onStatusUpdated = ::setDownloadStatus,
                        )
                    }
                }
            }
        }
    }

}