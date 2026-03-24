package com.kblack.offlinemap.presentation.screen.overview

import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.config.Profile
import com.graphhopper.routing.ev.MaxSpeed
import com.graphhopper.util.Parameters
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.base.BaseContainer
import com.kblack.offlinemap.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import timber.log.Timber
import java.io.File
import java.util.Locale
import kotlin.div
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapViewScreen(map: MapModel, homeViewModel: HomeViewModel,) {

    val uiState by homeViewModel.uiState.collectAsState()
    val map = uiState.maps.find { it.mapId == map.mapId } ?: return

    val styleJsonPath = homeViewModel.getStyleJsonPath(map)

    var hopper: GraphHopper? = null

    var showLoading by remember { mutableStateOf(false) }

    Timber.d("CAPTURE_PATH_MAP_tiles ${homeViewModel.getStyleJsonPath(map)}")
    Timber.d("CAPTURE_PATH_MAP_graph ${homeViewModel.getGraphPath(map)}")

    suspend fun initialize(graphDirectoryPath: String) {
        withContext(IO) {
            val graphDir = File(graphDirectoryPath)
            require(graphDir.exists() && graphDir.isDirectory) {
                "Graph directory not found: $graphDirectoryPath"
            }

            if (hopper != null) return@withContext

            val localHopper = GraphHopper().forMobile()
            localHopper.setProfiles(Profile("car").setVehicle("car").setWeighting("fastest"))
            val loaded = localHopper.load(graphDir.absolutePath)
            if (!loaded) {
                throw IllegalStateException("Cannot load GraphHopper graph from ${graphDir.absolutePath}")
            }
            hopper = localHopper
            Timber.d("[CAPTURE] Load graph success")
            Timber.d("[CAPTURE] Available profiles: ${localHopper.profiles}")

        }
    }

    suspend fun calculateRoute() {
        return withContext(IO) {
            showLoading = true
            val h = hopper ?: throw IllegalStateException("GraphHopper engine is not initialized")

            val request = GHRequest(21.0285, 105.8542, 10.7626, 106.6602)
                .setProfile("car")
                .setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI)
            request.getHints().put(Parameters.Routing.INSTRUCTIONS, "true")

            request.getPathDetails().add(MaxSpeed.KEY)
            request.getPathDetails().add(Parameters.Details.AVERAGE_SPEED)

            val response = h.route(request)
            if (response == null || response.hasErrors()) {
                val firstError = if (response != null && response.getErrors().isNotEmpty()) {
                    response.getErrors()[0].message
                } else {
                    "Unknown route error"
                }
                throw IllegalStateException(firstError)
            }

            val best = response.best
            val points = best.points
            val last = points.size() - 1

            Timber.d(
                "[ROUTE] distance=%.1f km | time=%d min | points=%d",
                best.distance / 1000.0,
                best.time / 60000,
                points.size()
            )
            Timber.d(
                "[ROUTE] start=(%.5f, %.5f) -> end=(%.5f, %.5f)",
                points.getLat(0), points.getLon(0),
                points.getLat(last), points.getLon(last)
            )

            val tr = h.translationMap.getWithFallBack(Locale("vi", "VN"))

            best.instructions.forEachIndexed { i, ins ->
                val text = ins.getTurnDescription(tr)
                val distKm = ins.distance / 1000.0
                val timeSec = ins.time / 1000
                Timber.d("[STEP ${i + 1}] $text | ${"%.2f".format(distKm)} km | ${timeSec}s | lat: ${points.getLat(i)} lon: ${points.getLon(i)}")
            }

            showLoading = false


        }
    }

    LaunchedEffect(Unit) {
        Timber.d("[CAPTURE] initialize: ${initialize(homeViewModel.getGraphPath(map)!!)}")
        calculateRoute()
    }




    val camera =
        rememberCameraState(
            firstPosition =
                CameraPosition(
                    target = Position(latitude = 10.7626, longitude = 106.6602),
                    zoom = 4.0
                )
        )
    LaunchedEffect(Unit) {
        camera.animateTo(
            finalPosition =
                camera.position.copy(target = Position(latitude = 21.0285, longitude = 105.8542), zoom = 10.0),
            duration = 5.seconds,
        )
    }

    BaseContainer {
        MaplibreMap(
            cameraState = camera,
            options = MapOptions(
                ornamentOptions = OrnamentOptions(
                    isLogoEnabled = false,
                    isAttributionEnabled = false,
                    isScaleBarEnabled = false
                )
            ),
            baseStyle = BaseStyle.Uri("file://${styleJsonPath!!}")
        )
        if (showLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).background(Color.Red))
        }
    }


}