package com.max4real.compose_boilerplate.shared.widget.ohteepee

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.max4real.compose_boilerplate.ui.theme.CustomColor
import com.max4real.compose_boilerplate.ui.theme.OpenAISans

@Composable
fun OtpInput(
    modifier: Modifier = Modifier,
    otpState: OhTeePeeState,
    onOtpChange: (OhTeePeeState) -> Unit,
    onComplete: (String) -> Unit
) {
    val otpText = otpState.asString()
    var isTextFieldFocused by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Design Tokens
    val cursorColor = CustomColor.White80
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            CustomColor.White,                    // Top Left: Full White
            CustomColor.White20                   // Bottom Right: 20% Opacity
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    // Blinking animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 900 },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursorAlpha"
    )

    BasicTextField(
        value = otpText,
        onValueChange = { newValue ->
            val filteredText = newValue.filter { it.isDigit() }.take(6)
            val newDigits = List(6) { index ->
                filteredText.getOrNull(index)?.toString() ?: ""
            }
            val newState = OhTeePeeState(digits = newDigits)
            onOtpChange(newState)

            if (newState.isComplete()) {
                onComplete(newState.asString())
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
        modifier = modifier
            .widthIn(max = 360.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isTextFieldFocused = it.isFocused },
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(6) { index ->
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    val isCurrentBox = index == otpText.length

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            // Apply the Linear Gradient Brush here
                            .border(
                                width = if (isCurrentBox && isTextFieldFocused) 2.dp else 1.dp,
                                brush = if (char.isNotEmpty() || (isCurrentBox && isTextFieldFocused)) {
                                    gradientBrush
                                } else {
                                    SolidColor(CustomColor.Gray40)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            fontSize = 24.sp,
                            color = CustomColor.White,
                            fontWeight = FontWeight.W700,
                            fontFamily = OpenAISans
                        )

                        if (isTextFieldFocused && isCurrentBox) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .alpha(cursorAlpha)
                                    .background(cursorColor)
                            )
                        }
                    }
                }
            }
        }
    )
}
