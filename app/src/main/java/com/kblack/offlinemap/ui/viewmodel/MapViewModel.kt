package com.kblack.offlinemap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kblack.offlinemap.data.repository.IOFileRepository
import com.kblack.offlinemap.data.repository.LocationRepository
import com.kblack.offlinemap.data.repository.PlaceSearchRepository
import com.kblack.offlinemap.data.repository.RoutingRepository
import com.kblack.offlinemap.models.GeoCoordinate
import com.kblack.offlinemap.models.MapModel
import com.kblack.offlinemap.models.NavigationSnapshot
import com.kblack.offlinemap.models.PlaceSearch
import com.kblack.offlinemap.models.Route
import com.kblack.offlinemap.models.RoutingOptions
import com.kblack.offlinemap.usecase.routing.BuildNavigationUseCase
import com.kblack.offlinemap.usecase.routing.InitializeRouterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
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

    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<PlaceSearch> = emptyList(),

    val errorMessage: String? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val routingRepository: RoutingRepository,
    private val ioFileRepository: IOFileRepository,
    private val locationRepository: LocationRepository,
    private val initializeRouterUseCase: InitializeRouterUseCase,
    private val buildNavigationUseCase: BuildNavigationUseCase,
    private val placeSearchRepository: PlaceSearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var routeJob: Job? = null

    private val _centerOnCurrentLocation = MutableSharedFlow<GeoCoordinate>(extraBufferCapacity = 1)
    val centerOnCurrentLocation: SharedFlow<GeoCoordinate> = _centerOnCurrentLocation.asSharedFlow()

    private val _place = MutableSharedFlow<PlaceSearch>(extraBufferCapacity = 1)
    val place: SharedFlow<PlaceSearch> = _place.asSharedFlow()

    fun getStyleJsonPath(map: MapModel): String? = ioFileRepository.getStyleJsonPath(map)
    fun loadDefaultLocations(): Map<String, GeoCoordinate> = ioFileRepository.loadDefaultLocations().mapValues {
        (_, location) -> GeoCoordinate(location.lat, location.lng)
    }

    private var initializedGraphPath: String? = null

    private val _searchQueryFlow = MutableStateFlow("")

    init {
        observeSearchQuery()
    }

    fun initializeMap(map: MapModel) {
        val graphPath = ioFileRepository.getGraphPath(map) ?: run {
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
                    it.copy(
                        routingReady = false,
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to initialize routing"
                    )
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

        val fresh = locationRepository.getCurrentLocation()
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
                routingRepository.calculateRoute(start, end, _uiState.value.routingOptions)
            }.onSuccess { route ->
                _uiState.update { it.copy(isRouting = false, route = route, errorMessage = null) }
                updateNavigationSnapshot(_uiState.value.currentLocation)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRouting = false,
                        errorMessage = error.message ?: "Failed to calculate route"
                    )
                }
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
        useCurrentLocation()
    }

    fun stopNavigation() {
        locationJob?.cancel()
        locationJob = null
        _uiState.update { it.copy(isNavigating = false, navigationSnapshot = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeLocation() {
        if (locationJob != null) return
        locationJob = viewModelScope.launch {
            locationRepository.observeCurrentLocation(1000L).collect { location ->
                _uiState.update { it.copy(currentLocation = location) }
                updateNavigationSnapshot(location)
            }
        }
    }

    private fun updateNavigationSnapshot(currentLocation: GeoCoordinate?) {
        val route = _uiState.value.route ?: return
        val current = currentLocation ?: return
        if (!_uiState.value.isNavigating) return

        val snapshot = buildNavigationUseCase(route, current)
        _uiState.update { it.copy(navigationSnapshot = snapshot) }

        if (snapshot.isOffTrack && !_uiState.value.isRouting) {
            recalculateRoute()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        _searchQueryFlow
            .debounce(500)
            .distinctUntilChanged()
            .transformLatest { query ->
                val trimmed = query.trim()
                if (trimmed.length < 2) {
                    emit(emptyList())
                    return@transformLatest
                }

                _uiState.update { it.copy(isSearching = true, errorMessage = null) }
                try {
                    val results = placeSearchRepository.searchPlaces(trimmed, limit = 5)
                    emit(results ?: emptyList())
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = e.message ?: "Search error") }
                    emit(emptyList())
                }
            }
            .onEach { results ->
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            }
            .launchIn(viewModelScope)
    }

    fun selectPlace(place: PlaceSearch) = viewModelScope.launch {
        _place.emit(place)
        _searchQueryFlow.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
        routeJob?.cancel()
        routingRepository.close()
    }

}