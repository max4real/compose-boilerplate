package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.shared.util.CustomHaptic
import com.max4real.compose_boilerplate.ui.theme.LocalAppDarkMode
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.ComposeboilerplateTheme
import com.max4real.compose_boilerplate.ui.theme.theme

data class CustomPopUpItem(
    val title: String,
    val icon: Painter,
    val iconSize : Dp = 20.dp,
    val color: Color? = null,
    val onTap: () -> Unit
)

@Composable
fun CustomPopUp(
    visible: Boolean,
    width: Dp = 200.dp,
    items: List<CustomPopUpItem>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    forceDark: Boolean = false,
    alignment: Alignment = Alignment.TopEnd,
    offset: DpOffset = DpOffset.Zero,
    transformOrigin: TransformOrigin = TransformOrigin(1f, 0f)
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible

    if (!transitionState.currentState && !transitionState.targetState) return

    val density = LocalDensity.current
    val popupOffset = with(density) {
        IntOffset(
            x = offset.x.roundToPx(),
            y = offset.y.roundToPx()
        )
    }

    Popup(
        alignment = alignment,
        offset = popupOffset,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(animationSpec = tween(120)) + scaleIn(
                animationSpec = tween(180),
                initialScale = 0.82f,
                transformOrigin = transformOrigin
            ),
            exit = fadeOut(animationSpec = tween(90)) + scaleOut(
                animationSpec = tween(120),
                targetScale = 0.82f,
                transformOrigin = transformOrigin
            )
        ) {
            CompositionLocalProvider(
                LocalAppDarkMode provides (forceDark || LocalAppDarkMode.current)
            ) {
                Surface(
                    modifier = modifier
                        .width(width),
                    shadowElevation = 6.dp,
                    color = theme.bottomSheetInputContainer,
                    contentColor = theme.bottomSheetInputContainer,
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                    ) {
                        items.forEach { item ->
                            CustomPopUpRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomPopUpRow(
    item: CustomPopUpItem
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = theme.rippleColor),
                onClick = {
                    CustomHaptic.lightImpact(context)
                    item.onTap()
                }
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = item.icon,
            contentDescription = null,
            tint = item.color ?: theme.cardRowIconsColor,
            modifier = Modifier.size(item.iconSize)
        )

        Text(
            text = item.title,
            color = item.color ?: theme.textPrimary,
            fontSize = 15.sp,
            letterSpacing = (-0.4).sp,
            fontFamily = OpenAISans,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CustomPopUpPreview() {
    ComposeboilerplateTheme(darkTheme = false) {
        CustomPopUp(
            visible = true,
            items = listOf(
                CustomPopUpItem(
                    title = "New chat",
                    icon = painterResource(R.drawable.user),
                    onTap = {}
                ),
                CustomPopUpItem(
                    title = "Create group",
                    icon = painterResource(R.drawable.profile_circle),
                    onTap = {}
                )
            ),
            onDismissRequest = {}
        )
    }
}
