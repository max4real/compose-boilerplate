package com.max4real.compose_boilerplate.shared.widget

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.max4real.compose_boilerplate.ui.theme.LocalAppDarkMode
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme

private const val DraggableSheetMaxHeightFraction = 0.9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetPlate(
    onDismissRequest: () -> Unit,
    sheetHeightFraction: Float? = null,
    modifier: Modifier = Modifier,
    enableDraggable: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        start = 15.dp,
        end = 15.dp,
        bottom = 24.dp,
        top = 15.dp
    ),
    containerColor: Color = theme.background2,
    contentColor: Color = theme.textPrimary,
    includeDragHandle: Boolean = false,
    applyNavigationBarPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val boundedSheetHeightFraction = sheetHeightFraction?.coerceIn(0f, 1f)
    val sheetGesturesEnabled = enableDraggable &&
            (boundedSheetHeightFraction == null || boundedSheetHeightFraction < DraggableSheetMaxHeightFraction)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        sheetGesturesEnabled = sheetGesturesEnabled,
        shape = RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = {
            if (includeDragHandle) {
                BottomSheetDefaults.DragHandle(
                    color = theme.bottomSheetInputDivider.copy(alpha = 0.7f),
                    width = 53.dp,
                    height = 5.dp,
                    modifier = Modifier.offset(y = (-12).dp)
                )
            }
        }
    ) {
        ConfigureTransparentNavigationBar()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (boundedSheetHeightFraction != null) {
                        Modifier.fillMaxHeight(boundedSheetHeightFraction)
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (applyNavigationBarPadding) {
                        Modifier.navigationBarsPadding()
                    } else {
                        Modifier
                    }
                )
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
private fun ConfigureTransparentNavigationBar() {
    val view = LocalView.current
    val window = (view.parent as? DialogWindowProvider)?.window
    val isDarkMode = LocalAppDarkMode.current

    SideEffect {
        window?.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                @Suppress("DEPRECATION")
                navigationBarColor = Color.Transparent.toArgb()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }

            WindowCompat.getInsetsController(this, view)
                .isAppearanceLightNavigationBars = !isDarkMode
        }
    }
}

@Composable
fun CustomBottomSheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    firstButtonIcon: Painter? = null,
    firstButtonIconSize: Int = 30,
    firstButtonContentDescription: String = "First action",
    onFirstButtonClick: (() -> Unit)? = null,
    lastButtonText: String? = null,
    lastButtonIsLoading: Boolean = false,
    onLastButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (firstButtonIcon != null && onFirstButtonClick != null) {
            CustomBottomSheetHeaderIconButton(
                icon = firstButtonIcon,
                contentDescription = firstButtonContentDescription,
                onClick = onFirstButtonClick,
                modifier = Modifier.align(Alignment.CenterStart),
                iconSize = firstButtonIconSize
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 76.dp),
            text = title,
            color = theme.appBarTitle,
            fontSize = 17.sp,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        if (lastButtonText != null && onLastButtonClick != null) {
            CustomTextButton(
                text = lastButtonText,
                onClick = onLastButtonClick,
                isLoading = lastButtonIsLoading,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun CustomBottomSheetHeaderIconButton(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "BottomSheetHeaderIconScale"
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .dropShadow(
                shape = CircleShape,
                shadow = Shadow(
                    radius = 40.dp,
                    spread = 0.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = theme.iconButtonShadow
                )
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(theme.appBarIconButton)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = theme.appBarIcon,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Composable
fun CustomTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        label = "BottomSheetHeaderTextScale"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .dropShadow(
                shape = RoundedCornerShape(22.dp),
                shadow = Shadow(
                    radius = 40.dp,
                    spread = 0.dp,
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    color = theme.iconButtonShadow
                )
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(theme.appBarIconButton)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = !isLoading,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = theme.appBarTitle,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text,
                color = theme.appBarTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                fontFamily = OpenAISans,
                maxLines = 1,
                lineHeight = 7.sp
            )
        }
    }
}

@Composable
private fun AppBottomSheetDragHandle(
    color: Color
) {
    Spacer(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 8.dp)
            .size(width = 42.dp, height = 4.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(color)
    )
}
