package com.kblack.offlinemap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.usecase.mapallowlist.GetMapUrlResponseUseCase
import com.kblack.offlinemap.domain.usecase.mapallowlist.LoadMapAllowlistUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelAllUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.CancelDownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DeleteMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.DownloadMapUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetLocalMapStatusUseCase
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
)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val loadMapAllowlistUseCase: LoadMapAllowlistUseCase,
    private val getMapUrlResponseUseCase: GetMapUrlResponseUseCase,
    private val getLocalMapStatusUseCase: GetLocalMapStatusUseCase,
    private val deleteMapUseCase: DeleteMapUseCase,
    private val downloadMapUseCase: DownloadMapUseCase,
    private val cancelDownloadMapUseCase: CancelDownloadMapUseCase,
    private val cancelAllUseCase: CancelAllUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapManagerUiState())
    val uiState = _uiState.asStateFlow()

    fun deleteMap(map: MapModel) {
        deleteMapUseCase(map)

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

        downloadMapUseCase(
            map = map,
            onStatusUpdated = ::setDownloadStatus,
        )
    }

    fun cancelDownloadMap(map: MapModel) {
        cancelDownloadMapUseCase(map)
        deleteMap(map)
    }

    fun getMapUrlResponse(map: MapModel): Int {
        return getMapUrlResponseUseCase(map.url)
    }

    private fun setDownloadStatus(map: MapModel, status: MapDownloadStatus) {
        val curMapDownloadStatus = uiState.value.mapDownloadStatus.toMutableMap()
        curMapDownloadStatus[map.mapId] = status

        if (
            status.status == MapDownloadStatusType.FAILED ||
            status.status == MapDownloadStatusType.NOT_DOWNLOADED
        ) {
            deleteMapUseCase(map)
        }

        _uiState.update { it.copy(mapDownloadStatus = curMapDownloadStatus) }
    }

    fun loadMapAllowlist() {
        _uiState.update {
            it.copy(loadingMapAllowlist = true, loadingMapAllowlistError = null)
        }

        viewModelScope.launch(IO) {
            try {
                val maps = loadMapAllowlistUseCase()
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
                    mapDownloadStatus[map.mapId] = getLocalMapStatusUseCase(map)
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

    fun clearLoadMapAllowlistError() {
        _uiState.update {
            it.copy(loadingMapAllowlistError = null)
        }
    }

    private fun processPendingDownloads(maps: List<MapModel>) {
        cancelAllUseCase {
            viewModelScope.launch(Main) {
                for (map in maps) {
                    val downloadStatus = uiState.value.mapDownloadStatus[map.mapId]?.status
                    if (downloadStatus == MapDownloadStatusType.PARTIALLY_DOWNLOADED) {
                        downloadMapUseCase(
                            map = map,
                            onStatusUpdated = ::setDownloadStatus,
                        )
                    }
                }
            }
        }
    }

}