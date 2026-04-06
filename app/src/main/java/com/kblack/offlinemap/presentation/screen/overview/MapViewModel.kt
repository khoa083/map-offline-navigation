package com.kblack.offlinemap.presentation.screen.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.domain.models.NavigationSnapshot
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.models.RoutingOptions
import com.kblack.offlinemap.domain.usecase.location.GetCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.location.ObserveCurrentLocationUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetGraphPathUseCase
import com.kblack.offlinemap.domain.usecase.mapdownload.GetStyleJsonPathUseCase
import com.kblack.offlinemap.domain.usecase.routing.BuildNavigationUseCase
import com.kblack.offlinemap.domain.usecase.routing.CalculateRouteUseCase
import com.kblack.offlinemap.domain.usecase.routing.CloseRouterUseCase
import com.kblack.offlinemap.domain.usecase.routing.InitializeRouterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(

    val routingReady: Boolean = false,
    val isLoading: Boolean = false,
    val isRouting: Boolean = false,
    val isNavigating: Boolean = false,

    val startPoint: GeoCoordinate? = null,
    val endPoint: GeoCoordinate? = null,
    val currentLocation: GeoCoordinate? = null,
    val route: Route? = null,
    val routingOptions: RoutingOptions = RoutingOptions(),
    val navigationSnapshot: NavigationSnapshot? = null,

    val errorMessage: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getStyleJsonPathUseCase: GetStyleJsonPathUseCase,
    private val getGraphPathUseCase: GetGraphPathUseCase,
    private val calculateRouteUseCase: CalculateRouteUseCase,
    private val initializeRouterUseCase: InitializeRouterUseCase,
    private val closeRouterUseCase: CloseRouterUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val observeCurrentLocationUseCase: ObserveCurrentLocationUseCase,
    private val buildNavigationUseCase: BuildNavigationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var routeJob: Job? = null

    private val _centerOnCurrentLocation = MutableSharedFlow<GeoCoordinate>(extraBufferCapacity = 1)
    val centerOnCurrentLocation: SharedFlow<GeoCoordinate> = _centerOnCurrentLocation.asSharedFlow()

    fun getStyleJsonPath(map: MapModel): String? = getStyleJsonPathUseCase(map)
    private var initializedGraphPath: String? = null

    fun initializeMap(map: MapModel) {
        val graphPath = getGraphPathUseCase(map) ?: run {
            _uiState.update { it.copy(errorMessage = "Graph path not found") }
            return
        }

        if (_uiState.value.routingReady && initializedGraphPath == graphPath) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                initializeRouterUseCase(graphPath)
            }.onSuccess {
                initializedGraphPath = graphPath
                _uiState.update {
                    it.copy(routingReady = true, isLoading = false, errorMessage = null)
                }
            }.onFailure { error ->
                initializedGraphPath = null
                _uiState.update {
                    it.copy(routingReady = false, isLoading = false, errorMessage = error.message)
                }
            }
        }
    }

    fun useCurrentLocation() = viewModelScope.launch {
        val cached = _uiState.value.currentLocation
        if (cached != null) {
            _uiState.update { it.copy(startPoint = cached, errorMessage = null) }
            _centerOnCurrentLocation.tryEmit(cached)
        }

        val fresh = getCurrentLocationUseCase()
        when {
            fresh != null -> {
                _uiState.update { it.copy(currentLocation = fresh, startPoint = fresh, errorMessage = null) }
                _centerOnCurrentLocation.emit(fresh)
            }
            cached == null -> {
                _uiState.update { it.copy(errorMessage = "Current location is not available") }
            }
        }
    }

    fun selectStartPoint(point: GeoCoordinate) {
        _uiState.update { it.copy(startPoint = point, errorMessage = null) }
        if (_uiState.value.endPoint != null) recalculateRoute()
    }

    fun selectEndPoint(point: GeoCoordinate) {
        _uiState.update { it.copy(endPoint = point, errorMessage = null) }
        if (_uiState.value.startPoint != null) recalculateRoute()
    }

    fun updateRoutingOptions(options: RoutingOptions) {
        _uiState.update { it.copy(routingOptions = options) }
        val state = _uiState.value
        if (state.startPoint != null && state.endPoint != null) recalculateRoute()
    }

    fun clearPoints() {

        _uiState.update {
            it.copy(
                startPoint = it.currentLocation,
                endPoint = null,
                route = null,
                errorMessage = null
            )
        }
    }

    fun recalculateRoute() {
        val state = _uiState.value
        if (!state.routingReady) {
            _uiState.update { it.copy(errorMessage = "Routing engine is not initialized") }
            return
        }
        val start = state.startPoint ?: return
        val end = state.endPoint ?: return

        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            _uiState.update { it.copy(isRouting = true, errorMessage = null) }
            runCatching {
                calculateRouteUseCase(start, end, _uiState.value.routingOptions)
            }.onSuccess { route ->
                _uiState.update { it.copy(isRouting = false, route = route, errorMessage = null) }
                updateNavigationSnapshot(_uiState.value.currentLocation)
            }.onFailure { error ->
                _uiState.update { it.copy(isRouting = false, errorMessage = error.message) }
            }
        }
    }


    fun startNavigation() {
        if (_uiState.value.route == null) {
            _uiState.update { it.copy(errorMessage = "Route is not available") }
            return
        }
        _uiState.update { it.copy(isNavigating = true) }
        observeLocation()
    }

    fun stopNavigation() {
        locationJob?.cancel()
        locationJob = null
        _uiState.update { it.copy(isNavigating = false, navigationSnapshot = null) }
    }

    private fun observeLocation() {
        if (locationJob != null) return
        locationJob = viewModelScope.launch {
            observeCurrentLocationUseCase(1000L).collect { location ->
                _uiState.update { it.copy(currentLocation = location) }
                updateNavigationSnapshot(location)
            }
        }
    }

    private fun updateNavigationSnapshot(currentLocation: GeoCoordinate?) {
        val route = _uiState.value.route ?: return
        val current = currentLocation ?: return
        if (!_uiState.value.isNavigating) return
        _uiState.update { it.copy(navigationSnapshot = buildNavigationUseCase(route, current)) }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        routeJob?.cancel()
        closeRouterUseCase()
    }

}