package com.kblack.offlinemap.presentation.screen.overview.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Motorcycle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.domain.models.TravelMode
import com.kblack.offlinemap.presentation.ui.theme.customColors

@Composable
fun UpdateRoutingVehicle(
    selectedTravelMode: TravelMode,
    onBackClick: () -> Unit,
    onTravelModeChange: (TravelMode) -> Unit,
    onStartNavigation: () -> Unit,
    canStartNavigation: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.customColors.taskCardBgColor,
                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
            )
            .height(68.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .alpha(0.8f)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.customColors.taskCardBgColor)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TravelModeButton(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                isSelected = selectedTravelMode == TravelMode.Foot,
                onClick = { onTravelModeChange(TravelMode.Foot) }
            )
            TravelModeButton(
                icon = Icons.Filled.Motorcycle,
                isSelected = selectedTravelMode == TravelMode.Motorcycle,
                onClick = { onTravelModeChange(TravelMode.Motorcycle) }
            )
            TravelModeButton(
                icon = Icons.Filled.DirectionsCar,
                isSelected = selectedTravelMode == TravelMode.Car,
                onClick = { onTravelModeChange(TravelMode.Car) }
            )
        }

        Button(
            onClick = onStartNavigation,
            enabled = canStartNavigation,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .alpha(if (canStartNavigation) 1f else 0.6f)
                .padding(end = 14.dp)
        ) {
            Text(text = "Start", color = MaterialTheme.customColors.taskCardBgColor)
        }
    }
}

@Composable
private fun TravelModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .alpha(0.8f)
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.customColors.taskCardBgColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
    ) {
        Icon(
            icon,
            contentDescription = "Travel mode",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}
