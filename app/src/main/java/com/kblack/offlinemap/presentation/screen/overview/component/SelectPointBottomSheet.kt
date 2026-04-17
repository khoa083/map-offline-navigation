package com.kblack.offlinemap.presentation.screen.overview.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kblack.offlinemap.R
import com.kblack.offlinemap.domain.models.GeoCoordinate
import com.kblack.offlinemap.presentation.viewmodel.MapUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPointBottomSheet(
    point: GeoCoordinate?,
    uiState: MapUiState,
    onDismissRequest: () -> Unit,
    onSelectStart: (GeoCoordinate) -> Unit,
    onSelectEnd: (GeoCoordinate) -> Unit,

    ) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.8f),
        ) {
            RoutePointAction(
                pointPreview = uiState.startPoint,
                rotateAngle = -90f,
                label = "ROUTE FROM",
                onClick = {
                    val selectedPoint = point ?: return@RoutePointAction
                    scope.launch {
                        delay(200)
                        onSelectStart(selectedPoint)
                        onDismissRequest()
                    }
                }
            )

            RoutePointAction(
                pointPreview = uiState.endPoint,
                rotateAngle = 90f,
                label = "ROUTE TO",
                onClick = {
                    val selectedPoint = point ?: return@RoutePointAction
                    scope.launch {
                        delay(200)
                        onSelectEnd(selectedPoint)
                        onDismissRequest()
                    }
                }
            )
        }
    }

}

@Composable
private fun RoutePointAction(
    pointPreview: GeoCoordinate?,
    rotateAngle: Float,
    label: String,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (pointPreview != null) {
                Text(
                    "lat: ${String.format(Locale.US, "%.2f", pointPreview.latitude)} , " +
                            "lon: ${String.format(Locale.US, "%.2f", pointPreview.longitude)}",
                    modifier = Modifier.clearAndSetSemantics {},
                    fontSize = 12.sp,
                    maxLines = 1,
                    textDecoration = TextDecoration.Underline
                )
            }
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.outline_line_start_circle_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(32.dp)
                    .rotate(rotateAngle)
                    .padding(bottom = 8.dp)
            )
            Text(label, modifier = Modifier.clearAndSetSemantics {})
        }
    }
}