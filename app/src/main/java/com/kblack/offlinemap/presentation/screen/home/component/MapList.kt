package com.kblack.offlinemap.presentation.screen.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.screen.home.HomeViewModel
import timber.log.Timber

@Composable
fun MapList(
    contentPadding: PaddingValues,
    maps: List<MapModel>,
    mapDownloadStatus: Map<String, MapDownloadStatus>,
    homeViewModel: HomeViewModel,
    onModelClicked: (MapModel) -> Unit,
    modifier: Modifier = Modifier,
) {

    Box(
        contentAlignment = Alignment.BottomEnd,
    ) {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = 16.dp),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = maps,
                key = { it.mapId }
            ) { map ->
                MapItem(
                    mapOff = map,
                    downloadStatus = mapDownloadStatus[map.mapId],
                    homeViewModel = homeViewModel,
                    onModelClicked = onModelClicked,
                )
            }
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(contentPadding.calculateBottomPadding())
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun MapItem(
    mapOff: MapModel,
    downloadStatus: MapDownloadStatus?,
    homeViewModel: HomeViewModel,
    onModelClicked: (MapModel) -> Unit,
    showDeleteButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 12.dp))
            .background(color = Color(0xFF282A2C))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.semantics {
                    isTraversalGroup = true
                }
            ) {
                MapNameAndStatus(
                    mapOff = mapOff,
                    downloadStatus = downloadStatus,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.Top) {
                    if (mapOff.localFileRelativeDirPathOverride.isEmpty()) {
                        DeleteMapButton(
                            mapOff = mapOff,
                            homeViewModel = homeViewModel,
                            downloadStatus = downloadStatus,
                            showDeleteBtn = showDeleteButton,
                            modifier = Modifier.offset(y = (-12).dp, x = 0.dp)
                        )
                    }
                }
            }
            MarkdownText(
                mapOff.description,
                smallFontSize = true,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )

            DownloadMapButton(
                mapOff = mapOff,
                downloadStatus = downloadStatus,
                homeViewModel = homeViewModel,
                onClicked = { onModelClicked(mapOff) },
            )
        }

    }
}