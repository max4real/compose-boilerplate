package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.max4real.compose_boilerplate.shared.util.CustomHaptic
import com.max4real.compose_boilerplate.ui.theme.CustomColor
import com.max4real.compose_boilerplate.ui.theme.OpenAISans

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun test() {
    AuthButton(
        text = "Continue",
        isFilled = true,
        isEnabled = true,
        isLoading = false,
        containerColor = CustomColor.White,
        disabledContainerColor = CustomColor.White20,
        contentColor = CustomColor.Black,
        disabledContentColor = CustomColor.White50,
        onTap = {

        }
    )
}

@Composable
fun AuthButton(
    text: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    isFilled: Boolean = true,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    icon: Painter? = null,
    containerColor: Color = Color.White,
    disabledContainerColor: Color = CustomColor.Gray,
    brandColor: Color = CustomColor.BrandColor,
    contentColor: Color = CustomColor.White,
    disabledContentColor: Color = CustomColor.LightGray,
    loadingColor: Color = Color.White
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonEnabled = isEnabled && !isLoading
    val enabledContentColor = if (isFilled) contentColor else containerColor
    val currentContentColor = if (buttonEnabled) enabledContentColor else disabledContentColor
    val backgroundColor = when {
        buttonEnabled && isFilled -> containerColor
        !buttonEnabled && isFilled -> disabledContainerColor
        else -> CustomColor.Transparent
    }
    val shape = RoundedCornerShape(25.dp)
    val scale = if (isPressed && !isLoading) 0.99f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isFilled) {
                    Modifier
                } else {
                    Modifier.border(
                        width = 2.dp,
                        color = if (buttonEnabled) brandColor else brandColor.copy(alpha = 0.5f),
                        shape = shape
                    )
                }
            )
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                enabled = buttonEnabled,
                interactionSource = interactionSource,
                indication = ripple(
                    color = if (isFilled) Color.Black.copy(alpha = 0.08f) else Color.White.copy(
                        alpha = 0.10f
                    ),
                ),
                onClick = {
                    CustomHaptic.lightImpact(context)
                    onTap()
                }
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = loadingColor
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = currentContentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontFamily = OpenAISans,
                    fontWeight = FontWeight.W600,
                    color = currentContentColor,
                    letterSpacing = (-0.4).sp,
                )
            }
        }
    }
}
