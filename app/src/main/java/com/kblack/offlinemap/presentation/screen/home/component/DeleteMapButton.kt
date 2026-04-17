package com.kblack.offlinemap.presentation.screen.home.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.kblack.offlinemap.R
import com.kblack.offlinemap.domain.models.MapDownloadStatus
import com.kblack.offlinemap.domain.models.MapDownloadStatusType
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.viewmodel.HomeViewModel

@Composable
fun DeleteMapButton(
    mapOff: MapModel,
    homeViewModel: HomeViewModel,
    downloadStatus: MapDownloadStatus?,
    showDeleteBtn: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        when (downloadStatus?.status) {
            MapDownloadStatusType.SUCCEEDED -> {
                if (showDeleteBtn) {
                    IconButton(onClick = { showConfirmDeleteDialog = true }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.alpha(0.6f),
                        )
                    }
                }
            }

            else -> {
                null
            }
        }
    }
    if (showConfirmDeleteDialog) {
        ConfirmDeleteMapDialog(
            mapOff = mapOff,
            onConfirm = {
                homeViewModel.deleteMap(mapOff)
                showConfirmDeleteDialog = false
            },
            onDismiss = { showConfirmDeleteDialog = false },
        )
    }
}

@Composable
fun ConfirmDeleteMapDialog(mapOff: MapModel, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete download") },
        text = {
            Text(stringResource(R.string.confirm_delete_model_dialog_content).format(mapOff.name))
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Ok") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}