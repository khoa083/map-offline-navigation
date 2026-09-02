package com.kblack.offlinemap.ui.screen.overview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kblack.offlinemap.models.GeoCoordinate
import com.kblack.offlinemap.models.MapModel
import com.kblack.offlinemap.ui.base.BaseContainer
import com.kblack.offlinemap.ui.screen.overview.component.ExitConfirmationDialog
import com.kblack.offlinemap.ui.screen.overview.component.FloatingSearchBar
import com.kblack.offlinemap.ui.screen.overview.component.KeepScreenOn
import com.kblack.offlinemap.ui.screen.overview.component.MapCameraEffects
import com.kblack.offlinemap.ui.screen.overview.component.MapControls
import com.kblack.offlinemap.ui.screen.overview.component.rememberMapLocationAccessState
import com.kblack.offlinemap.ui.screen.overview.component.rememberMapLocationState
import com.kblack.offlinemap.ui.screen.overview.component.NavigationBottomPanel
import com.kblack.offlinemap.ui.screen.overview.component.NavigationMode
import com.kblack.offlinemap.ui.screen.overview.component.RouteInstructionsBottomSheet
import com.kblack.offlinemap.ui.screen.overview.component.SelectPointBottomSheet
import com.kblack.offlinemap.ui.screen.overview.component.UpdateRoutingVehicle
import com.kblack.offlinemap.ui.screen.overview.component.rememberAnimatedRouteLine
import com.kblack.offlinemap.ui.screen.overview.component.rememberAnimatedPuckLocation
import com.kblack.offlinemap.ui.utils.Constant.INITIAL_ZOOM
import com.kblack.offlinemap.ui.utils.Constant.MAX_ZOOM
import com.kblack.offlinemap.ui.utils.Constant.MIN_ZOOM
import com.kblack.offlinemap.ui.theme.customColors
import com.kblack.offlinemap.ui.viewmodel.MapViewModel
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.nil
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.overlay.ExpandingAttributionButton
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.toJson
import kotlinx.serialization.json.JsonObject
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@SuppressLint("SourceLockedOrientationActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen(
    map: MapModel,
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()

    val styleJsonPath = remember(map.mapId) { mapViewModel.getStyleJsonPath(map) }
    var showSelectPointSheet by remember { mutableStateOf(false) }
    var point by remember { mutableStateOf<GeoCoordinate?>(null) }
    var zoom by remember { mutableDoubleStateOf(INITIAL_ZOOM) }

    val showEndFlagAndTopBar = uiState.startPoint != null && uiState.endPoint != null
    val selectedTravelMode = uiState.routingOptions.travelMode
    val canStartNavigation = uiState.route != null && !uiState.isRouting
    val snackBarHostState = remember { SnackbarHostState() }

    val animatedRoute = rememberAnimatedRouteLine(route = uiState.route, travelMode = selectedTravelMode)

    LaunchedEffect(showEndFlagAndTopBar) {
        if (showEndFlagAndTopBar) {
            showSelectPointSheet = false
        }
    }

    var compassMode by remember { mutableStateOf(false) }
    var mapMode3d by remember { mutableStateOf(false) }
    val currentTilt by rememberUpdatedState(if (mapMode3d) 55.0 else 0.0)

    val defaultLocations = remember { mapViewModel.loadDefaultLocations() }
    val defaultLocation = remember(map.mapId) {
        val l = defaultLocations[map.mapId]
        if (l != null) Position(latitude = l.latitude, longitude = l.longitude) else
            Position(latitude = 21.0285, longitude = 105.8542) //Hanoi(VN)
    }

    val focusManager = LocalFocusManager.current

    val locationAccessState = rememberMapLocationAccessState(
        context = context,
        onLocationReady = { mapViewModel.useCurrentLocation() }
    )
    val canUseMapLibreLocation =
        locationAccessState.hasPermission && locationAccessState.isLocationServiceOn
    val locationStateMaplibre = rememberMapLocationState(canUseMapLibreLocation)
    val hasMapLibreLocation = locationStateMaplibre != null
    val sheetState = rememberBottomSheetScaffoldState()

    val shouldInterceptBack = uiState.isNavigating || activity?.isTaskRoot == true
    BackHandler(enabled = shouldInterceptBack) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                activity?.finish()
            },
            onDismiss = { showExitDialog = false },
        )
    }

    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = defaultLocation,
                    zoom = zoom
                )
        )

    // v0.15.0 dropped MapOptions.ornamentOptions (native-view ornaments) in favor of a Compose
    // overlay. This reproduces the old isLogoEnabled=false / isAttributionEnabled=true /
    // isScaleBarEnabled=false intent: only the attribution button, nothing else.
    val mapOverlay = remember {
        MapOverlay {
            // Read before entering Row: RowScope shadows MapOverlayScope's implicit receiver,
            // so cameraState/styleState must be captured to locals first (same pattern as
            // MapOverlay.Default in the library itself).
            val overlayCamera = cameraState
            val overlayStyle = styleState
            Row(
                Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ExpandingAttributionButton(cameraState = overlayCamera, styleState = overlayStyle)
            }
        }
    }

    LaunchedEffect(Unit) {
        mapViewModel.initializeMap(map)
    }

    MapCameraEffects(
        camera = camera,
        zoom = zoom,
        mapMode3d = mapMode3d,
        onMapMode3dChange = { mapMode3d = it },
        compassMode = compassMode,
        isNavigating = uiState.isNavigating,
        currentTilt = currentTilt,
        locationStateMaplibre = locationStateMaplibre,
        centerOnCurrentLocation = mapViewModel.centerOnCurrentLocation,
        place = mapViewModel.place,
        onPointSelected = { point = it },
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackBarHostState.showSnackbar(
                message = message,
                withDismissAction = true
            )
            mapViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    )
    {
        BaseContainer(modifier = Modifier.padding(it)) {
            MaplibreMap(
                cameraState = camera,
                overlay = mapOverlay,
                baseStyle = BaseStyle.Uri("file://${styleJsonPath}"),
                onMapClick = { p, dp ->
                    Timber.d("[CAPTURE] Map clicked at: $p , $dp")
                    point = GeoCoordinate(latitude = p.latitude, longitude = p.longitude)

                    showSelectPointSheet = !showEndFlagAndTopBar

                    ClickResult.Pass
                },
                onMapLoadFailed = { error ->
                    Timber.e("[CAPTURE] Map failed to load: $error")
                },
                onMapLoadFinished = {
                    Timber.d("[CAPTURE] Map loaded successfully")
                },
            ) {

                val routeJson = animatedRoute.geoJson
                if (routeJson != null) {
                    val routeSource = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(routeJson)
                    )

                    LineLayer(
                        id = "route-layer",
                        source = routeSource,
                        minZoom = 0.0f,
                        maxZoom = 24.0f,
                        color = const(MaterialTheme.customColors.tabHeaderBgColor),
                        width = const(8.dp),
                        opacity = const(0.6f),
                        cap = const(LineCap.Round),
                        join = const(LineJoin.Round),
                        dasharray = if (animatedRoute.isDashLine) const(listOf(1f, 1.5f)) else nil(),
                    )
                }

                if (showEndFlagAndTopBar) {

                    FlagPointLayer(point = uiState.endPoint!!)
                } else if (uiState.endPoint != null) {
                    // Memoized on the point itself: without this, every recomposition (e.g. the
                    // current-location update every second while navigating) re-serialized this
                    // GeoJSON and pushed it to the native source even though the point hadn't moved.
                    val endPointJson = remember(uiState.endPoint) {
                        singlePointFeatureJson(uiState.endPoint!!)
                    }
                    val endPointSource = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(endPointJson)
                    )
                    CircleLayer(
                        id = "end-point-layer",
                        source = endPointSource,
                        color = const(MaterialTheme.customColors.tabHeaderBgColor),
                        radius = const(8.dp)

                    )
                }
                if (uiState.startPoint != null && uiState.startPoint != uiState.currentLocation) {
                    val startPointJson = remember(uiState.startPoint) {
                        singlePointFeatureJson(uiState.startPoint!!)
                    }
                    val startPointSource = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(startPointJson)
                    )
                    CircleLayer(
                        id = "start-point-layer",
                        source = startPointSource,
                        color = const(MaterialTheme.customColors.tabHeaderBgColor),
                        radius = const(8.dp)

                    )
                }

                if (locationAccessState.hasPermission &&
                    locationAccessState.isLocationServiceOn &&
                    hasMapLibreLocation
                ) {
                    // https://maplibre.org/maplibre-compose/api/lib/maplibre-compose/org.maplibre.compose.location/-location-puck.html
                    // new: https://github.com/maplibre/maplibre-compose/issues/707 (0.13.0)
                    LocationPuck(
                        idPrefix = "location-accuracy",
                        location = rememberAnimatedPuckLocation(locationStateMaplibre.location),
                        cameraState = camera,
                        oldLocationThreshold = 10.seconds,
                        accuracyThreshold = 0f,
                        colors = LocationPuckColors(
                            bearingColor = Color(0xFF0B57D0),
                        ),
                        sizes = LocationPuckSizes(),
                    )
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }


            if (showEndFlagAndTopBar && !uiState.isNavigating) {
                UpdateRoutingVehicle(
                    selectedTravelMode = selectedTravelMode,
                    onBackClick = {
                        showSelectPointSheet = false
                        point = null
                        mapViewModel.clearPoints()
                    },
                    onTravelModeChange = { mode ->
                        mapViewModel.updateRoutingOptions(
                            uiState.routingOptions.copy(travelMode = mode)
                        )
                    },
                    onStartNavigation = { mapViewModel.startNavigation() },
                    canStartNavigation = canStartNavigation
                )
            }

            if (!uiState.isNavigating && !showEndFlagAndTopBar) {
                FloatingSearchBar(
                    searchQuery = uiState.searchQuery,
                    searchResults = uiState.searchResults,
                    isSearching = uiState.isSearching,
                    onSearchQueryChanged = { query -> mapViewModel.onSearchQueryChanged(query) },
                    onLocationSelected = { location -> mapViewModel.selectPlace(location) },
                    focusManager = focusManager,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter)
                )
            }

            // https://stackoverflow.com/questions/69039723/is-there-a-jetpack-compose-equivalent-for-androidkeepscreenon-to-keep-screen-al
            if (uiState.isNavigating) {
                KeepScreenOn()
            }

            MapControls(
                zoom,
                onZoomIn = { zoom = (camera.position.zoom + 1.0).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                onZoomOut = { zoom = (camera.position.zoom - 1.0).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                onClickLocation = locationAccessState.onLocationClick,
                compassMode = compassMode,
                onClickCompass = { compassMode = !compassMode },
                mapMode3d = mapMode3d,
                onClickMapMode3d = { mapMode3d = !mapMode3d }
            )

            if (showSelectPointSheet && !showEndFlagAndTopBar) {
                focusManager.clearFocus()
                SelectPointBottomSheet(
                    point = point,
                    uiState = uiState,
                    onDismissRequest = { showSelectPointSheet = false },
                    onSelectStart = { startP ->
                        mapViewModel.selectStartPoint(startP)
                        showSelectPointSheet = false
                    },
                    onSelectEnd = { endP ->
                        mapViewModel.selectEndPoint(endP)
                        showSelectPointSheet = false
                    }
                )
            }

            if (showEndFlagAndTopBar && !uiState.isNavigating) {
                BottomSheetScaffold(
                    sheetPeekHeight = 148.dp,
                    scaffoldState = sheetState,
                    sheetSwipeEnabled = sheetState.bottomSheetState.currentValue != SheetValue.Expanded,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.customColors.taskCardBgColor,
                    sheetContainerColor = MaterialTheme.customColors.taskCardBgColor,
                    sheetContent = {
                        RouteInstructionsBottomSheet(
                            route = uiState.route,
                            isRouting = uiState.isRouting,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                ) { _ -> }
            }

            if (uiState.isNavigating) {
                NavigationMode(
                    snapshot = uiState.navigationSnapshot,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                NavigationBottomPanel(
                    snapshot = uiState.navigationSnapshot,
                    onStopNavigation = { mapViewModel.stopNavigation() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun FlagPointLayer(
    point: GeoCoordinate,
) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.JsonString(singlePointFeatureJson(point))
    )

    SymbolLayer(
        id = "end-point-layer-flag",
        source = source,
        iconImage =
            image(
                value = rememberVectorPainter(Icons.Default.Flag),
                size = DpSize(24.dp, 24.dp),
            ),
        iconAllowOverlap = const(true),

        )
}

private fun singlePointFeatureJson(point: GeoCoordinate): String {
    val markerPoint = Point(Position(longitude = point.longitude, latitude = point.latitude))
    return Feature<Point, JsonObject?>(geometry = markerPoint, properties = null).toJson()
}
