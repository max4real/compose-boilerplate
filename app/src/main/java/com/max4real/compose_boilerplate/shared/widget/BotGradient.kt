package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.max4real.compose_boilerplate.ui.theme.theme

@Composable
fun BottomGradient(
    color : androidx.compose.ui.graphics.Color = theme.gradientBase,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = if (isDarkMode) {
                    listOf(
                        color.copy(alpha = 0f),
                        color.copy(alpha = 0.44f),
                        color.copy(alpha = 0.73f),
                        color.copy(alpha = 0.89f),
                        color.copy(alpha = 0.97f)
                    )
                } else {
                    listOf(
                        color.copy(alpha = 0f),
                        color.copy(alpha = 0.58f),
                        color.copy(alpha = 0.88f),
                        color.copy(alpha = 0.98f),
                        color
                    )
                }
            )
        )
    )
}
