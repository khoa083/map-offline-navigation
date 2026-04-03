package com.kblack.offlinemap.presentation.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.kblack.offlinemap.domain.models.MapModel
import com.kblack.offlinemap.presentation.screen.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun rememberDelayedAnimationProgress(
    initialDelay: Long = 0,
    animationDurationMs: Int,
    animationLabel: String,
    easing: Easing = FastOutSlowInEasing,
): Float {
    var startAnim by remember { mutableStateOf(false) }
    val progress: Float by
    animateFloatAsState(
        if (startAnim) 1f else 0f,
        label = animationLabel,
        animationSpec = tween(durationMillis = animationDurationMs, easing = easing),
    )
    LaunchedEffect(Unit) {
        delay(initialDelay)
        startAnim = true
    }
    return progress
}
/** Format the bytes .
 * SI (1000): kB, MB, GB...
 *
 * (1024): KiB, MiB, GiB...
 *
 * https://stackoverflow.com/questions/59234916/how-to-convert-byte-size-into-human-readable-format-in-kotlin
 * */
fun Long.convertBytesToReadable(si: Boolean = true, extraDecimalForGbAndAbove: Boolean = false): String {
    val bytes = this
    val unit = if (si) 1000 else 1024
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
    val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
    var formatString = "%.1f %sB"
    if (extraDecimalForGbAndAbove && pre.lowercase() != "k" && pre != "M") {
        formatString = "%.2f %sB"
    }
    return formatString.format(bytes / unit.toDouble().pow(exp.toDouble()), pre)
}

fun checkNotificationPermissionAndStartDownload(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    homeViewModel: HomeViewModel,
    mapOff: MapModel,
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) -> {
            homeViewModel.downloadMap(mapOff)
        }

        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

fun ensureValidFileName(fileName: String): String {
    return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
}