package com.kblack.offlinemap.ui.screen.overview.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(currentView) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}