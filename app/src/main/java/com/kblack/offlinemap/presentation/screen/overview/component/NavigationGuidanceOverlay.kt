package com.kblack.offlinemap.presentation.screen.overview.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.domain.models.NavigationSnapshot
import com.kblack.offlinemap.domain.utils.RouteTextFormatter
import com.kblack.offlinemap.presentation.ui.NavigationInstructionFormatter
import com.kblack.offlinemap.presentation.ui.theme.customColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NavigationTopInstructionCard(
    snapshot: NavigationSnapshot?,
    modifier: Modifier = Modifier,
) {
    val instruction = snapshot?.nextInstruction ?: return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.customColors.taskCardBgColor,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(0.28f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(NavigationInstructionFormatter.rotationDegrees(instruction.sign))
                )
                Text(
                    text = RouteTextFormatter.formatDistanceMeters(instruction.distanceMeters),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Surface(
            color = MaterialTheme.customColors.taskCardBgColor,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(0.72f)
        ) {
            Text(
                text = NavigationInstructionFormatter.title(instruction.sign, instruction.name),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

@Composable
fun NavigationBottomPanel(
    snapshot: NavigationSnapshot?,
    onStopNavigation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remainingDuration = snapshot?.remainingDurationMillis ?: 0L
    val remainingDistance = snapshot?.remainingDistanceMeters ?: 0.0
    val currentTime = remember { SimpleDateFormat("h:mma", Locale.getDefault()) }.format(Date())

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
//            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                HeaderMetric(value = currentTime, label = "Destination")
                HeaderMetric(
                    value = RouteTextFormatter.formatDurationMillis(remainingDuration),
                    label = "Remaining"
                )
                HeaderMetric(
                    value = RouteTextFormatter.formatDistanceMeters(remainingDistance),
                    label = "Left"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(14.dp))

//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = {}) {
//                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Voice")
//                }
//                IconButton(onClick = {}) {
//                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
//                }
//                Button(
//                    onClick = onStopNavigation,
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
//                    modifier = Modifier.padding(end = 14.dp)
//                ) {
//                    Text(text = "STOP", color = Color.White)
//                }
//            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onStopNavigation,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "STOP",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}



