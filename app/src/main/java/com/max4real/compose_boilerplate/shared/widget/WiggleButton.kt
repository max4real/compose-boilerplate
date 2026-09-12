package com.max4real.compose_boilerplate.shared.widget

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WiggleButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minScale: Float = 0.85f,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) minScale else 1f,
        animationSpec = tween(100),
        label = "wiggle_scale"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(enabled) {
                if (!enabled || onClick == null) return@pointerInput

                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()

                        view.performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY
                        )

                        onClick()

                        kotlinx.coroutines.delay(100.milliseconds)

                        isPressed = false
                    }
                )
            }
    ) {
        content()
    }
}