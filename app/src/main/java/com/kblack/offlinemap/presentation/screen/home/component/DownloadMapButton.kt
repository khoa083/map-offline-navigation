package com.kblack.offlinemap.presentation.screen.home.component

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.viewmodel.HomeViewModel
import com.kblack.offlinemap.presentation.ui.checkNotificationPermissionAndStartDownload
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DownloadMapButton(
    mapOff: MapModel,
    downloadStatus: MapDownloadStatus?,
    homeViewModel: HomeViewModel,
    enabled: Boolean = true,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    canShowTryIt: Boolean = true,
) {

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var checkingToken by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var downloadStarted by remember { mutableStateOf(false) }

    val needToDownloadFirst =
        (downloadStatus?.status == MapDownloadStatusType.NOT_DOWNLOADED ||
                downloadStatus?.status == MapDownloadStatusType.FAILED)
    val inProgress = downloadStatus?.status == MapDownloadStatusType.IN_PROGRESS
    val downloadSucceeded = downloadStatus?.status == MapDownloadStatusType.SUCCEEDED
    val isPartiallyDownloaded = downloadStatus?.status == MapDownloadStatusType.PARTIALLY_DOWNLOADED
    val isUnzipping = downloadStatus?.status == MapDownloadStatusType.UNZIPPING

    val showDownloadProgress =
        !downloadSucceeded && (downloadStarted || checkingToken || inProgress || isPartiallyDownloaded || isUnzipping)
    var curDownloadProgress: Float

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            homeViewModel.downloadMap(mapOff)
        }

    LaunchedEffect(downloadStatus?.status) {
        if (
            downloadStatus?.status == MapDownloadStatusType.NOT_DOWNLOADED ||
            downloadStatus?.status == MapDownloadStatusType.SUCCEEDED ||
            downloadStatus?.status == MapDownloadStatusType.FAILED
        ) {
            downloadStarted = false
            checkingToken = false
        }
    }

    val startDownload: (accessToken: String?) -> Unit = {
        checkNotificationPermissionAndStartDownload(
            context = context,
            launcher = permissionLauncher,
            homeViewModel = homeViewModel,
            mapOff = mapOff,
        )
        checkingToken = false
    }


    val handleClickButton = {
        scope.launch(IO) {
            if (needToDownloadFirst) {
                downloadStarted = true
                // For HuggingFace urls
                if (mapOff.url.startsWith("https://huggingface.co")) {

                    // Check if the url needs auth.

                    val firstResponseCode = homeViewModel.getMapUrlResponse(mapOff)
                    if (firstResponseCode == HttpURLConnection.HTTP_OK) {
                        withContext(Main) { startDownload(null) }
                        return@launch
                    } else if (firstResponseCode < 0) {
                        downloadStarted = false
                        showErrorDialog = true
                        return@launch
                    }
                }
                // For other urls, just download the model.
                else {
                    withContext(Main) { startDownload(null) }
                }
            }
            // No need to download. Directly open the model.
            else {
                withContext(Main) {
                    onClicked()
                }
            }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!showDownloadProgress) {
            Button(
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (
                            (!downloadSucceeded || !canShowTryIt)
                        ) {
                            MaterialTheme.colorScheme.surfaceContainer
                        } else {
                            Color(0xFF3174F1)
                        }
                ),
                contentPadding = PaddingValues(horizontal = 12.dp),
                onClick = {
                    if (!enabled || checkingToken) {
                        return@Button
                    }
                    handleClickButton()
                }
            ) {
                val textColor =
                    if (!downloadSucceeded) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        Color.White
                    }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        if (needToDownloadFirst) Icons.Outlined.FileDownload
                        else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = textColor,
                    )

                    if (needToDownloadFirst) {
                        Text(
                            "Download",
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    } else if (canShowTryIt) {
                        Text(
                            "Try it",
                            color = textColor,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            autoSize =
                                TextAutoSize.StepBased(
                                    minFontSize = 8.sp,
                                    maxFontSize = 16.sp,
                                    stepSize = 1.sp
                                ),
                        )
                    }

                }
            }
        } else if (downloadStatus?.status == MapDownloadStatusType.UNZIPPING) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 8.dp)
                    .height(42.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically

            ) {
                CircularProgressIndicator(
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(20.dp).fillMaxWidth(),
                )
            }
        } else {
            curDownloadProgress =
                downloadStatus!!.receivedBytes.toFloat() / downloadStatus.totalBytes.toFloat()
            if (curDownloadProgress.isNaN() || curDownloadProgress.isInfinite()) {
                curDownloadProgress = 0f
            }
            val animatedProgress = remember { Animatable(0f) }

            var downloadProgressModifier: Modifier = Modifier.fillMaxWidth()
            downloadProgressModifier =
                downloadProgressModifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 8.dp)
                    .height(42.dp)
            Row(
                modifier = downloadProgressModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (checkingToken) {
                    Text(
                        "Checking ...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        "${(curDownloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .width(44.dp),
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        progress = { animatedProgress.value },
                        color = Color(0xFF3174F1),
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    IconButton(
                        onClick = {
                            downloadStarted = false
                            homeViewModel.cancelDownloadMap(mapOff)
                        },
                        colors =
                            IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                        modifier = Modifier.semantics { contentDescription = "Stop" },
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            LaunchedEffect(curDownloadProgress) {
                animatedProgress.animateTo(curDownloadProgress, animationSpec = tween(150))
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                icon = {
                    Icon(
                        Icons.Rounded.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = { Text("Unknown network error") },
                text = { Text("Please check your internet connection.") },
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showErrorDialog = false
                    }) { Text("Close") }
                },
            )
        }
    }
}