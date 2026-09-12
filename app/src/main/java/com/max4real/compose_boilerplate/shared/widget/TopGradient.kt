package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.max4real.compose_boilerplate.ui.theme.theme

@Composable
fun TopGradient(
    modifier: Modifier = Modifier,
    color: Color = theme.gradientBase,
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
//                        color,
//                        color,
//                        color.copy(alpha = 0.9f),
//                        color.copy(alpha = 0.7f),
//                        color.copy(alpha = 0.4f),
//                        Color.Transparent

                        color,
                        color.copy(alpha = 0.95f),
                        color.copy(alpha = 0.87f),
                        color.copy(alpha = 0.50f),
                        color.copy(alpha = 0f)

                    )
                )
            )
            .animateContentSize()
    )
}
