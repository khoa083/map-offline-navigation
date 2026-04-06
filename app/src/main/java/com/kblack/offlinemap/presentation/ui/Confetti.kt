package com.kblack.offlinemap.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Shape
import java.util.concurrent.TimeUnit
import kotlin.collections.copy

@Stable
class SimpleConfettiController {
    private var eventId by mutableIntStateOf(0)
    internal val id: Int get() = eventId

    fun launch() {
        eventId++
    }
}

@Composable
fun rememberSimpleConfettiController(): SimpleConfettiController {
    return remember { SimpleConfettiController() }
}

@Composable
fun SimpleConfettiHost(
    controller: SimpleConfettiController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val colors = remember(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary
    ) {
        listOf(
            colorScheme.primary.toArgb(),
            colorScheme.secondary.toArgb(),
            colorScheme.tertiary.toArgb()
        )
    }

    val parties = remember(colors) {
        val base = Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            timeToLive = 4200L,
            shapes = listOf(Shape.Square, Shape.Circle, Shape.Rectangle(0.2f)),
            colors = colors,
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(100),
        )

        listOf(
            base.copy(
                angle = 45,
                position = Position.Relative(0.0, 0.0),
                spread = 90,
            ),
            base.copy(
                angle = 90,
                position = Position.Relative(0.5, 0.0),
                spread = 360,
            ),
            base.copy(
                angle = 135,
                position = Position.Relative(1.0, 0.0),
                spread = 90,
            )
        )
    }

    LaunchedEffect(controller.id) {
        if (controller.id > 0) {
            visible = true
            delay(4200L)
            visible = false
        }
    }

    Box(modifier = modifier) {
        content()

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = parties
            )
        }
    }
}