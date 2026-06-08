package com.kblack.offlinemap.data.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.kblack.offlinemap.models.NavigationSnapshot
import com.kblack.offlinemap.ui.utils.NavigationInstructionFormat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NavigationServiceHelper {

    private var totalDistanceMeters: Double = 0.0

    private var isStopFromService = false

    private val _stopRequestedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val stopRequestedFlow: SharedFlow<Unit> = _stopRequestedFlow.asSharedFlow()

    fun start(context: Context, snapshot: NavigationSnapshot) {
        totalDistanceMeters = snapshot.remainingDistanceMeters
        isStopFromService = false
        ContextCompat.startForegroundService(
            context,
            buildIntent(context, snapshot, NavigationForegroundService.ACTION_START)
        )
    }

    fun update(context: Context, snapshot: NavigationSnapshot) {
        context.startService(
            buildIntent(context, snapshot, NavigationForegroundService.ACTION_UPDATE)
        )
    }

    fun stop(context: Context) {
        if (isStopFromService) {
            isStopFromService = false
            return
        }
        totalDistanceMeters = 0.0
        context.startService(
            Intent(context, NavigationForegroundService::class.java).apply {
                action = NavigationForegroundService.ACTION_STOP
            }
        )
    }

    fun notifyStop() {
        isStopFromService = true
        totalDistanceMeters = 0.0
        _stopRequestedFlow.tryEmit(Unit)
    }

    fun hide(context: Context) {
        context.startService(
            Intent(context, NavigationForegroundService::class.java).apply {
                action = NavigationForegroundService.ACTION_HIDE
            }
        )
    }

    fun show(context: Context, snapshot: NavigationSnapshot?) {
        snapshot ?: return
        context.startService(
            buildIntent(context, snapshot, NavigationForegroundService.ACTION_SHOW)
        )
    }

    private fun buildIntent(
        context: Context,
        snapshot: NavigationSnapshot,
        action: String
    ): Intent {
        val instr = snapshot.nextInstruction

        val instructionText = if (instr != null)
            NavigationInstructionFormat.title(instr.sign, instr.name)
        else ""

        val rotation = if (instr != null)
            NavigationInstructionFormat.rotationDegrees(instr.sign)
        else 0f

        val progress = if (totalDistanceMeters > 0)
            ((1.0 - snapshot.remainingDistanceMeters / totalDistanceMeters) * 100)
                .toInt().coerceIn(0, 100)
        else 0

        return Intent(context, NavigationForegroundService::class.java).apply {
            this.action = action
            putExtra(NavigationForegroundService.EXTRA_INSTRUCTION, instructionText)
            putExtra(NavigationForegroundService.EXTRA_ROTATION, rotation)
            putExtra(NavigationForegroundService.EXTRA_PROGRESS, progress)
        }
    }
}