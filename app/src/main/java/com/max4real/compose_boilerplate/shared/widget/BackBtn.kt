package com.max4real.compose_boilerplate.shared.widget

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.max4real.compose_boilerplate.ui.theme.CustomColor

@Composable
fun BackButton(
    icon: Painter,
    modifier: Modifier = Modifier,
    iconColor: Color = CustomColor.Black, // 👈 new param
    onClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = ""
    )

    val handleClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        if (onClick != null) {
            onClick()
        } else {
            dispatcher?.onBackPressed()
        }
    }

    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = CustomColor.Black.copy(alpha = 0.1f),
                spotColor = CustomColor.Black.copy(alpha = 0.1f)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(CustomColor.White, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { handleClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            painter = icon,
            contentDescription = "Back",
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}