package com.kblack.offlinemap.presentation.screen.home.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.R
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.ui.Constant.MAP_INFO_ICON_SIZE
import com.kblack.offlinemap.presentation.ui.convertBytesToReadable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MapNameAndStatus(
    mapOff: MapModel,
    downloadStatus: MapDownloadStatus?,
    modifier: Modifier = Modifier,
) {
    val inProgress = downloadStatus?.status == MapDownloadStatusType.IN_PROGRESS
    val isPartiallyDownloaded = downloadStatus?.status == MapDownloadStatusType.PARTIALLY_DOWNLOADED
    var curDownloadProgress = 0f

    Column(modifier = modifier) {
        Text(
            mapOff.name,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            StatusIcon(
                mapOff = mapOff,
                downloadStatus = downloadStatus,
                modifier = Modifier.padding(end = 4.dp)
            )
            if (downloadStatus != null && downloadStatus.status == MapDownloadStatusType.FAILED) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        downloadStatus.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = Typography().labelSmall,
                        overflow = TextOverflow.Ellipsis,
                    )

                }
            } else {
                var sizeLabel: String = mapOff.sizeInBytes.convertBytesToReadable()
                if (downloadStatus != null) {
                    if (inProgress || isPartiallyDownloaded) {
                        var totalSize = downloadStatus.totalBytes
                        if (totalSize == 0L) {
                            totalSize = mapOff.sizeInBytes
                        }
                        sizeLabel =
                            "${
                                downloadStatus.receivedBytes.convertBytesToReadable(
                                    extraDecimalForGbAndAbove = true
                                )
                            } of ${totalSize.convertBytesToReadable()}"
                        if (downloadStatus.bytesPerSecond > 0) {
                            sizeLabel =
                                "$sizeLabel · ${downloadStatus.bytesPerSecond.convertBytesToReadable()} / s"
                        }
                        if (isPartiallyDownloaded) {
                            sizeLabel = "$sizeLabel (resuming...)"
                        }
                        curDownloadProgress =
                            downloadStatus.receivedBytes.toFloat() / downloadStatus.totalBytes.toFloat()
                        if (curDownloadProgress.isNaN()) {
                            curDownloadProgress = 0f
                        }
                    } else if (downloadStatus.status == MapDownloadStatusType.UNZIPPING) {
                        sizeLabel = "Unzipping..."
                    }
                }
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    for ((index, line) in sizeLabel.split("\n").withIndex()) {
                        Text(
                            line,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier.offset(y = if (index == 0) 0.dp else (-1).dp),
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun StatusIcon(
    mapOff: MapModel,
    downloadStatus: MapDownloadStatus?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        val color = MaterialTheme.colorScheme.primary
        when (downloadStatus?.status) {
            MapDownloadStatusType.NOT_DOWNLOADED -> {
                Icon(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    tint = Color(0xFFCCCCCC),
                    contentDescription = stringResource(R.string.cd_not_downloaded_icon),
                    modifier = Modifier.size(MAP_INFO_ICON_SIZE),
                )
            }

            MapDownloadStatusType.SUCCEEDED -> {
                Icon(
                    Icons.Filled.DownloadForOffline,
                    tint = color,
                    contentDescription = stringResource(R.string.cd_downloaded_icon),
                    modifier = Modifier.size(MAP_INFO_ICON_SIZE),
                )
            }

            MapDownloadStatusType.IN_PROGRESS -> {
                Icon(
                    Icons.Rounded.Downloading,
                    contentDescription = stringResource(R.string.cd_downloading_icon),
                    modifier = Modifier.size(MAP_INFO_ICON_SIZE),
                )
            }

            MapDownloadStatusType.FAILED -> {
                Icon(
                    Icons.Rounded.Error,
                    tint = Color(0xFFAA0000),
                    contentDescription = stringResource(R.string.cd_download_failed_icon),
                    modifier = Modifier.size(MAP_INFO_ICON_SIZE),
                )
            }

            else -> {
                null
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MapNameAndStatusPreview() {
    MapNameAndStatus(
        mapOff = MapModel(
            mapId = "1",
            name = "Map of Europe",
            time = "2024-06-01",
            description = "A detailed map of Europe with major cities and landmarks.",
            sizeInBytes = 150_000_000L,
            continent = "Europe",
            allow = true
        ),
        downloadStatus = MapDownloadStatus(
            status = MapDownloadStatusType.NOT_DOWNLOADED,
            totalBytes = 150_000_000L,
            receivedBytes = 75_000_000L,
            errorMessage = "",
            bytesPerSecond = 500_000L,
            remainingMs = 150_000L
        )
    )
}