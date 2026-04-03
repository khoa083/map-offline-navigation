package com.kblack.offlinemap.presentation.screen.overview.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.domain.models.Route
import com.kblack.offlinemap.domain.models.RouteInstruction
import com.kblack.offlinemap.domain.utils.RouteTextFormatter
import com.kblack.offlinemap.presentation.ui.NavigationInstructionFormatter
import com.kblack.offlinemap.presentation.ui.theme.customColors

@Composable
fun RouteInstructionsSheetContent(
    route: Route?,
    isRouting: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        RouteSummaryHeader(route = route)

        when {
            isRouting -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            route == null || route.instructions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Chua co chi duong",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(route.instructions) { instruction ->
                        RouteInstructionRow(instruction = instruction)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSummaryHeader(route: Route?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.customColors.taskCardBgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryMetric(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Straighten,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            },
            label = "Distance",
            value = RouteTextFormatter.formatDistanceMeters(route?.distanceMeters ?: 0.0)
        )

        SummaryMetric(
            icon = {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            },
            label = "Time",
            value = RouteTextFormatter.formatDurationMillis(route?.durationMillis ?: 0L)
        )
    }
}

@Composable
private fun SummaryMetric(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            Text(text = value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RouteInstructionRow(instruction: RouteInstruction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        RouteSignIcon(sign = instruction.sign)
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = NavigationInstructionFormatter.title(instruction.sign, instruction.name),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = RouteTextFormatter.formatDistanceMeters(instruction.distanceMeters),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RouteSignIcon(sign: Int) {
    val rotation = NavigationInstructionFormatter.rotationDegrees(sign)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.customColors.taskCardBgColor),
        contentAlignment = Alignment.Center
    ) {
        // todo: FIXME Created by AI, I will revise it later. The icon should depend on the sign type, not just an arrow.
        Icon(
            imageVector = Icons.Filled.ArrowUpward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation)
        )
    }
}

