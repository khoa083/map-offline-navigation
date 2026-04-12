package com.kblack.offlinemap.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.R
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.screen.home.component.MapTopBar
import com.kblack.offlinemap.presentation.model.TopBarAction
import com.kblack.offlinemap.presentation.model.TopBarType
import com.kblack.offlinemap.presentation.screen.home.component.MapList
import com.kblack.offlinemap.presentation.ui.SimpleConfettiHost
import com.kblack.offlinemap.presentation.ui.rememberDelayedAnimationProgress
import com.kblack.offlinemap.presentation.ui.rememberSimpleConfettiController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClickMapView: (MapModel) -> Unit = {},
    homeViewModel: HomeViewModel
) {
    val uiState by homeViewModel.uiState.collectAsState()
    var loadingMapAllowlistDelayed by remember { mutableStateOf(false) }

    val confetti = rememberSimpleConfettiController()

    LaunchedEffect(uiState.loadingMapAllowlist) {
        if (uiState.loadingMapAllowlist) {
            delay(200)
            if (uiState.loadingMapAllowlist) {
                loadingMapAllowlistDelayed = true
            }
        } else {
            loadingMapAllowlistDelayed = false
        }
    }

    if (uiState.loadingMapAllowlistError != null) {
        val errorMessage = uiState.loadingMapAllowlistError
        AlertDialog(
            icon = {
                Icon(
                    Icons.Rounded.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = { errorMessage?.let { Text(it) } },
            text = { Text("Please check your internet connection and try again later.") },
            onDismissRequest = { homeViewModel.loadMapAllowlist() },
            confirmButton = {
                TextButton(onClick = { homeViewModel.loadMapAllowlist() }) { Text("Retry") }
            },
            dismissButton = {
                TextButton(onClick = { homeViewModel.clearLoadMapAllowlistError() }) {
                    Text("Cancel")
                }
            },
        )
    }

    SimpleConfettiHost(
        controller = confetti,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                val progress =
                    rememberDelayedAnimationProgress(
                        animationDurationMs = 800,
                        animationLabel = "map top bar",
                    )
                Box(
                    modifier =
                        Modifier.graphicsLayer {
                            alpha = progress
                            translationY = ((-16).dp * (1 - progress)).toPx()
                        }
                ) {
                    MapTopBar(
                        title = stringResource(R.string.app_name),
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        confetti.launch()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary,
                ) {
                    Icon(Icons.Filled.Celebration, contentDescription = null)
                }
            }
        ) { paddingValues ->
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                ) {
                    MapList(
                        contentPadding = paddingValues,
                        maps = uiState.maps,
                        mapDownloadStatus = uiState.mapDownloadStatus,
                        homeViewModel = homeViewModel,
                        onModelClicked = { map -> onClickMapView(map) }
                    )
                }
            }

            if (loadingMapAllowlistDelayed) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 3.dp,
                        modifier = Modifier.padding(end = 8.dp).size(20.dp),
                    )
                    Text(
                        "Loading map list...",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

