package com.kblack.offlinemap.presentation.screen.home.component

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kblack.offlinemap.presentation.model.TopBarAction
import com.kblack.offlinemap.presentation.model.TopBarType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leftAction: TopBarAction? = null,
    rightAction: TopBarAction? = null,
    subtitle: String = "",
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BasicText(
                        text = title,
                        maxLines = 1,
                        color = { titleColor },
                        style = MaterialTheme.typography.titleMedium,
                        autoSize =
                            TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 16.sp, stepSize = 1.sp),
                    )
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = {
            when (leftAction?.type) {
                TopBarType.SETTING -> {
                    IconButton(onClick = Toast.makeText(LocalContext.current, "App setting clicked", Toast.LENGTH_SHORT)::show) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                else -> {null}
            }
            when (rightAction?.type) {
                TopBarType.NAVIGATE_UP->{
                    IconButton(onClick = Toast.makeText(LocalContext.current, "clicked", Toast.LENGTH_SHORT)::show) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                else -> {null}
            }
        }
    )
}