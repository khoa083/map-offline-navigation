package com.kblack.offlinemap.presentation.screen.overview

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
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
import kotlin.time.Duration.Companion.seconds

@Composable
fun MapViewScreen(map: MapModel, homeViewModel: HomeViewModel,) {

    val uiState by homeViewModel.uiState.collectAsState()
    val map = uiState.maps.find { it.mapId == map.mapId } ?: return

    val styleJsonPath = homeViewModel.getStyleJsonPath(map)

    Timber.d("CAPTURE_PATH_MAP_tiles ${homeViewModel.getStyleJsonPath(map)}")
    Timber.d("CAPTURE_PATH_MAP_graph ${homeViewModel.getGraphPath(map)}")

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
        if (map != null) {
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
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }


}