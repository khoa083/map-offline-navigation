package com.kblack.offlinemap.presentation.screen.overview.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.kblack.offlinemap.R
import com.kblack.offlinemap.presentation.ui.Constant.MAX_ZOOM
import com.kblack.offlinemap.presentation.ui.Constant.MIN_ZOOM
import com.kblack.offlinemap.presentation.ui.theme.customColors

@Composable
fun BoxScope.MapControls(
    zoom: Double,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onClickLocation: () -> Unit,
    compassMode: Boolean = false,
    onClickCompass: () -> Unit,
    mapMode3d: Boolean = false,
    onClickMapMode3d: () -> Unit,
){
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onZoomIn,
                    enabled = zoom < MAX_ZOOM,
                    modifier = Modifier
                        .alpha(0.8f)
                        .width(52.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp, 28.dp, 8.dp, 8.dp))
                        .background(MaterialTheme.customColors.taskCardBgColor)
                        .align(Alignment.End)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increment",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onZoomOut,
                    enabled = zoom > MIN_ZOOM,
                    modifier = Modifier
                        .alpha(0.8f)
                        .width(52.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp, 8.dp, 28.dp, 28.dp))
                        .background(MaterialTheme.customColors.taskCardBgColor)
                        .align(Alignment.End)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrement",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                    ) {

                        IconButton(
                            onClick = onClickMapMode3d,
                            modifier = Modifier
                                .alpha(0.8f)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.customColors.taskCardBgColor)
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(if(mapMode3d) R.drawable.d_on else R.drawable.d_off),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        IconButton(
                            onClick = onClickCompass,
                            modifier = Modifier
                                .alpha(0.8f)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.customColors.taskCardBgColor)
                        ) {
                            Icon(
                                imageVector = if (compassMode) Icons.Filled.Explore else Icons.Filled.ExploreOff, contentDescription = "Explore",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onClickLocation,
                        modifier = Modifier
                            .alpha(0.8f)
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.customColors.taskCardBgColor)
                    ) {
                        Icon(
                            Icons.Filled.MyLocation, contentDescription = "My Location",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }


                }
            }
        }

        Spacer(modifier = Modifier.height(200.dp))
    }
}