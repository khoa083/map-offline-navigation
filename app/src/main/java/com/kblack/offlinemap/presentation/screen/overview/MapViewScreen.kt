package com.kblack.offlinemap.presentation.screen.overview

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.base.BaseContainer
import com.kblack.offlinemap.presentation.screen.overview.component.MapControls
import com.kblack.offlinemap.presentation.screen.overview.component.NavigationBottomPanel
import com.kblack.offlinemap.presentation.screen.overview.component.NavigationMode
import com.kblack.offlinemap.presentation.screen.overview.component.RouteInstructionsBottomSheet
import com.kblack.offlinemap.presentation.screen.overview.component.SelectPointBottomSheet
import com.kblack.offlinemap.presentation.screen.overview.component.UpdateRoutingVehicle
import com.kblack.offlinemap.presentation.screen.overview.component.normalizeDegree
import com.kblack.offlinemap.presentation.screen.overview.component.rememberCompassMode
import com.kblack.offlinemap.presentation.screen.overview.component.shortestAngleDelta
import com.kblack.offlinemap.presentation.ui.Constant.INITIAL_ZOOM
import com.kblack.offlinemap.presentation.ui.Constant.MAX_ZOOM
import com.kblack.offlinemap.presentation.ui.Constant.MIN_ZOOM
import com.kblack.offlinemap.presentation.ui.theme.customColors
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.BearingUpdate
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.spatialk.geojson.Position
import timber.log.Timber
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

//todo: FIXME
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

    val uiState by mapViewModel.uiState.collectAsStateWithLifecycle()
    val styleJsonPath = remember(map.mapId) { mapViewModel.getStyleJsonPath(map) }
    var showSelectPointSheet by remember { mutableStateOf(false) }
    var point by remember { mutableStateOf<GeoCoordinate?>(null) }
    var zoom by remember { mutableDoubleStateOf(INITIAL_ZOOM) }

    val routePoints = remember(uiState.route) {
        mutableStateListOf<GeoCoordinate>().apply { addAll(uiState.route?.points.orEmpty()) }
    }

    val showEndFlagAndTopBar = uiState.startPoint != null && uiState.endPoint != null
    val selectedTravelMode = uiState.routingOptions.travelMode
    val canStartNavigation = uiState.route != null && !uiState.isRouting

    //fix show picker point
    LaunchedEffect(showEndFlagAndTopBar) {
        if (showEndFlagAndTopBar) {
            showSelectPointSheet = false
        }
    }

    val locationProvider = rememberDefaultLocationProvider()
    val locationState = rememberUserLocationState(locationProvider)

    var compassMode by remember { mutableStateOf(false) }
    var mapMode3d by remember { mutableStateOf(false) }
    val currentTilt by rememberUpdatedState(if (mapMode3d) 55.0 else 0.0)

    val routeCoords = remember(routePoints) {
        routePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
    }

    val progress = remember(routeCoords) { Animatable(0f) }

    LaunchedEffect(routeCoords) {
        progress.snapTo(0f)
        if (routeCoords.size >= 2) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                )
            )
        }
    }


    fun hasLocationPermission(ctx: Context): Boolean {
        val fine = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Timber.d("[CAPTURE] permission result = $result")
        hasPermission = hasLocationPermission(context)
    }

    val sheetState = rememberBottomSheetScaffoldState()

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            mapViewModel.useCurrentLocation()
        }
    }

    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 21.0285, longitude = 105.8542), //Hanoi
                    zoom = INITIAL_ZOOM
                )
        )

    LaunchedEffect(zoom) {
        if (abs(camera.position.zoom - zoom) < 0.01) return@LaunchedEffect
         camera.animateTo(
             finalPosition =
                 camera.position.copy(
                    zoom = zoom
                ),
        )
    }

    //todo: FIXME: hard code tilt
    // control 2D/3D mode by changing tilt.
    LaunchedEffect(mapMode3d) {
        camera.animateTo(
            finalPosition = camera.position.copy(tilt = if (mapMode3d) 55.0 else 0.0),
            duration = 700.milliseconds
        )
    }

    LaunchedEffect(Unit) {
        mapViewModel.initializeMap(map)
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        mapViewModel.centerOnCurrentLocation.collect { p ->
            camera.animateTo(
                CameraPosition(
                    target = Position(latitude = p.latitude, longitude = p.longitude),
                    zoom = 16.5,
                    tilt = currentTilt
                ),
                duration = 3.seconds
            )
        }
    }

    LaunchedEffect(uiState.isNavigating) {
        if (uiState.isNavigating && !mapMode3d) mapMode3d = true
    }

    if (hasPermission && uiState.isNavigating) {

        LocationTrackingEffect(
            trackBearing = true,
            locationState = locationState,
            enabled = true,
        ) {
            Timber.d("[CAPTURE] update: $currentLocation")
            val speed = currentLocation.speed?.toFloat() ?: 0f
            val speedThreshold = 1f  // 2 m/s (~7.2 km/h)

            val updateMode = if (speed >= speedThreshold) {
                BearingUpdate.TRACK_LOCATION
            } else {
                BearingUpdate.ALWAYS_NORTH
            }
            
            camera.updateFromLocation(updateBearing = updateMode)
        }
    }


    if(compassMode) {
        val heading by rememberCompassMode()

        LaunchedEffect(uiState.isNavigating) {
            if (!compassMode || uiState.isNavigating) return@LaunchedEffect
            snapshotFlow { heading }.collect { target ->
                val targetBearing = target ?: return@collect
                val current = camera.position.bearing.toFloat()
                val delta = shortestAngleDelta(current, targetBearing)
                if (abs(delta) > 1f) {
                    val next = normalizeDegree(current + delta * 0.2f)
                    camera.position = camera.position.copy(bearing = next.toDouble())
                }
            }
        }
    }
//    val targetHeading = remember { mutableStateOf<Float?>(null) }
//    if(compassMode) {
//        val heading by rememberCompassHeading()
//
//        LaunchedEffect(Unit) {
//            snapshotFlow { heading }.collect { h ->
//                targetHeading.value = h
//            }
//        }
//
//        LaunchedEffect(camera) {
//            var current = camera.position.bearing.toFloat()
//            while (true) {
//                withFrameMillis {
//                    val target = targetHeading.value ?: return@withFrameMillis
//                    val delta = shortestAngleDelta(current, target)
//                    if (abs(delta) > 0.05f) {
//                        current = normalizeDegree(current + delta * 0.2f)
//                        camera.position = camera.position.copy(bearing = current.toDouble())
//                    }
//                }
//            }
//        }
//    }

    val routeGeoJson by remember(routeCoords) {
        derivedStateOf {
            if (routeCoords.size < 2) return@derivedStateOf null
            val count = ((routeCoords.size - 1) * progress.value).toInt().coerceAtLeast(1) + 1
            Feature.fromGeometry(LineString.fromLngLats(routeCoords.take(count))).toJson()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.customColors.taskCardBgColor,
    )
    {
        BaseContainer(modifier = Modifier.padding(it)) {
            MaplibreMap(
                cameraState = camera,
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        isLogoEnabled = false,
                        isAttributionEnabled = true,
                        isScaleBarEnabled = false,
                        padding = PaddingValues(top = 84.dp)
                    )
                ),
                baseStyle = BaseStyle.Uri("file://${styleJsonPath}"),
                onMapClick = { p, dp ->
                    Timber.d("[CAPTURE] Map clicked at: $p , $dp")
                    point = GeoCoordinate(latitude = p.latitude, longitude = p.longitude)

                    //todo test: if both points are already selected, clicking on the map should clear them and start new selection
                    if (!showEndFlagAndTopBar) {
                        showSelectPointSheet = true
                    } else {
                        showSelectPointSheet = false
                    }

                    ClickResult.Pass
                },
                onMapLoadFailed = { error ->
                    Timber.e("Map failed to load: $error")
                },
                onMapLoadFinished = {
                    Timber.d("[CAPTURE] Map loaded successfully")
                },
            ) {

                if (routeCoords.size >= 2) {
                    val json = routeGeoJson
                    if (json != null) {
                        val routeSource = rememberGeoJsonSource(
                            data = GeoJsonData.JsonString(json)
                        )

                        LineLayer(
                            id = "route-layer",
                            source = routeSource,
                            minZoom = 0.0f,
                            maxZoom = 24.0f,
                            color = const(Color(0xFF0B57D0)),
                            width = const(8.dp),
                            opacity = const(0.6f),
                            cap = const(LineCap.Round),
                            join = const(LineJoin.Round),
                        )
                    }
                }

                if (showEndFlagAndTopBar) {

                    FlagPointLayer(
                        id = "end-point-layer-flag",
                        point = uiState.endPoint!!
                    )
                } else if (uiState.endPoint != null) {
                    val endPointSource = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(singlePointFeatureJson(uiState.endPoint!!))
                    )
                    CircleLayer(
                        id = "end-point-layer",
                        source = endPointSource,
                        color = const(Color(0xFF0B57D0)),
                        radius = const(8.dp)

                    )
                }
                if (uiState.startPoint != null && uiState.startPoint != uiState.currentLocation) {
                    val startPointSource = rememberGeoJsonSource(
                        data = GeoJsonData.JsonString(singlePointFeatureJson(uiState.startPoint!!))
                    )
                    CircleLayer(
                        id = "start-point-layer",
                        source = startPointSource,
                        color = const(Color(0xFF0B57D0)),
                        radius = const(8.dp)

                    )
                }
                // https://maplibre.org/maplibre-compose/api/lib/maplibre-compose/org.maplibre.compose.location/-location-puck.html
                LocationPuck(
                    idPrefix = "location-accuracy",
                    locationState = locationState,
                    cameraState = camera,
                    oldLocationThreshold = 3.seconds,
                    accuracyThreshold = 0f,
                    colors = LocationPuckColors(
                        bearingColor = Color(0xFF0B57D0),
                    ),
                    sizes = LocationPuckSizes(

                    ),
                )
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


            MapControls(
                zoom,
                onZoomIn = { zoom = (zoom + 1.0).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                onZoomOut = { zoom = (zoom - 1.0).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                onClickLocation = {
                    if (hasPermission) {
                        mapViewModel.useCurrentLocation()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                compassMode = compassMode,
                onClickCompass = {compassMode = !compassMode},
                mapMode3d = mapMode3d,
                onClickMapMode3d = { mapMode3d = !mapMode3d }
            )

            if (showSelectPointSheet && !showEndFlagAndTopBar) {
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
    id: String,
    point: GeoCoordinate,
) {
    val source = rememberGeoJsonSource(
        data = GeoJsonData.JsonString(singlePointFeatureJson(point))
    )

    SymbolLayer(
        id = id,
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
    val markerPoint = Point.fromLngLat(point.longitude, point.latitude)
    return Feature.fromGeometry(markerPoint).toJson()
}
