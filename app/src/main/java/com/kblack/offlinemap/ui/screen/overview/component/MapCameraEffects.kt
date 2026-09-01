package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import com.kblack.offlinemap.models.GeoCoordinate
import com.kblack.offlinemap.models.PlaceSearch
import kotlinx.coroutines.flow.SharedFlow
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.BearingUpdate
import org.maplibre.compose.location.LocationState
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.updateCamera
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.LengthUnit
import timber.log.Timber
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Bundles every [CameraState]-related side effect for [com.kblack.offlinemap.ui.screen.overview.MapViewScreen]:
 * keeping the camera's zoom/tilt in sync with UI-driven state, flying to a place/current-location
 * pick, following the device's live location while navigating, and slewing the bearing while
 * compass mode is on. Pulling these out of the screen keeps each concern independently readable
 * and testable, and keeps the screen composable itself focused on layout.
 *
 * Emits no UI of its own.
 */
@Composable
fun MapCameraEffects(
    camera: CameraState,
    zoom: Double,
    mapMode3d: Boolean,
    onMapMode3dChange: (Boolean) -> Unit,
    compassMode: Boolean,
    isNavigating: Boolean,
    currentTilt: Double,
    locationStateMaplibre: LocationState?,
    centerOnCurrentLocation: SharedFlow<GeoCoordinate>,
    place: SharedFlow<PlaceSearch>,
    onPointSelected: (GeoCoordinate) -> Unit,
) {
    LaunchedEffect(zoom) {
        if (abs(camera.position.zoom - zoom) < 0.01) return@LaunchedEffect
        camera.animateTo(
            finalPosition = camera.position.copy(zoom = zoom),
        )
    }

    LaunchedEffect(mapMode3d) {
        camera.animateTo(
            finalPosition = camera.position.copy(tilt = if (mapMode3d) 55.0 else 0.0),
            duration = 700.milliseconds,
        )
    }

    LaunchedEffect(Unit) {
        centerOnCurrentLocation.collect { p ->
            camera.animateTo(
                CameraPosition(
                    target = Position(latitude = p.latitude, longitude = p.longitude),
                    zoom = 16.5,
                    tilt = currentTilt,
                ),
                duration = 3.seconds,
            )
        }
    }

    LaunchedEffect(Unit) {
        place.collect { p ->
            onPointSelected(GeoCoordinate(latitude = p.lat, longitude = p.lng))
            camera.animateTo(
                CameraPosition(
                    target = Position(latitude = p.lat, longitude = p.lng),
                    zoom = 12.5,
                    tilt = currentTilt,
                ),
                duration = 2.seconds,
            )
        }
    }

    LaunchedEffect(isNavigating) {
        if (isNavigating && !mapMode3d) onMapMode3dChange(true)
    }

    if (isNavigating) {
        locationStateMaplibre?.let { safeLocationState ->
            LocationTrackingEffect(
                trackBearing = true,
                locationState = safeLocationState,
                enabled = true,
            ) {
                Timber.d("[CAPTURE] update: $currentLocation")
                val speed = currentLocation.speed?.distancePerSecond?.toDouble(
                    LengthUnit(1.0, "m")
                ) ?: 0.0
                val speedThreshold = 1f  // 2 m/s (~7.2 km/h)
                Timber.d("[CAPTURE] update speed: $speed")
                val updateMode = if (speed >= speedThreshold) {
                    BearingUpdate.TRACK_COURSE
                } else {
                    BearingUpdate.ALWAYS_NORTH
                }

                // Location fixes arrive at a 1s default cadence (LocationRequest.minimumInterval,
                // see rememberLocationState), but updateCamera's own default animationDuration is
                // only 300ms. That mismatch is what made camera movement look jittery while moving:
                // it glided for 300ms, then sat still for the remaining ~700ms until the next fix
                // arrived, over and over. Stretching the animation to match the fix cadence turns
                // that into one continuous glide between fixes instead of a series of
                // jump-then-pause hops.
                // See: https://github.com/maplibre/maplibre-compose/blob/v0.15.0/lib/maplibre-compose/src/commonMain/kotlin/org/maplibre/compose/location/UpdateCamera.kt
                updateCamera(
                    camera = camera,
                    animationDuration = 1.seconds,
                    updateBearing = updateMode,
                )
            }
        }
    }

    if (compassMode) {
        val targetHeading = remember { mutableStateOf<Float?>(null) }
        val heading by rememberCompassMode()

        DisposableEffect(Unit) {
            onDispose { targetHeading.value = null }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { heading }.collect { h ->
                targetHeading.value = h
            }
        }

        LaunchedEffect(camera, isNavigating) {
            if (isNavigating) return@LaunchedEffect
            var current = camera.position.bearing.toFloat()
            while (true) {
                withFrameMillis {
                    val target = targetHeading.value ?: return@withFrameMillis
                    val delta = shortestAngleDelta(current, target)
                    if (abs(delta) > 0.05f) {
                        current = normalizeDegree(current + delta * 0.2f)
                        camera.position = camera.position.copy(bearing = current.toDouble())
                    }
                }
            }
        }
    }
}
