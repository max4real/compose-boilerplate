package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.max4real.compose_boilerplate.ui.theme.theme

@Composable
fun Shimmer(
    forceDark: Boolean = false
) {
    val shimmerTransition = rememberInfiniteTransition(label = "media_bubble_shimmer")
    val shimmerOffset by shimmerTransition.animateFloat(
        initialValue = -450f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "media_bubble_shimmer_offset"
    )
    val baseColor = if (forceDark) Color(0xFF1D232B) else theme.imageShimmerBase
    val highlightColor = if (forceDark) Color(0xFF343D48) else theme.imageShimmerHighlight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        baseColor,
                        highlightColor,
                        baseColor,
                        baseColor
                    ),
                    start = Offset(shimmerOffset, shimmerOffset),
                    end = Offset(shimmerOffset + 450f, shimmerOffset + 450f)
                )
            )
    )
}
