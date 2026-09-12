package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.imageLoader
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.shared.config.AppEnvironment
import com.max4real.compose_boilerplate.ui.theme.CustomColor

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val url = "${AppEnvironment.MEDIA_BASE_URL}/emoji/badge.webp"

    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = url,
        contentDescription = "Verified Account",
        imageLoader = context.imageLoader,
        modifier = modifier.size(size),
        loading = {
            VerifiedBadgeIcon(size)
        },
        error = {
            VerifiedBadgeIcon(size)
        },
        success = {
            SubcomposeAsyncImageContent(modifier = Modifier.verifiedBadgeShimmer(size))
        }
    )
}

@Composable
private fun VerifiedBadgeIcon(size: Dp) {
    Icon(
        painter = painterResource(R.drawable.verified_badge),
        contentDescription = "Verified",
        tint = CustomColor.accentBlue,
        modifier = Modifier
            .size(size)
            .verifiedBadgeShimmer(size)
    )
}

// Diagonal highlight sweeping across the badge, masked to its own alpha shape.
private fun Modifier.verifiedBadgeShimmer(badgeSize: Dp): Modifier = composed {
    val sizePx = with(LocalDensity.current) { badgeSize.toPx() }
    val bandWidth = sizePx * 0.72f
    val bandHeight = sizePx * 2.4f

    val transition = rememberInfiniteTransition(label = "verified_badge_shimmer")
    val translateX by transition.animateFloat(
        initialValue = -bandWidth,
        targetValue = sizePx * 1.15f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3550
                -bandWidth at 0
                -bandWidth at 2200
                (sizePx * 1.15f) at 3050 using FastOutSlowInEasing
                (sizePx * 1.15f) at 3550
            }
        ),
        label = "verified_badge_shimmer_translate"
    )

    graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            rotate(degrees = 22f) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0f),
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0f),
                        ),
                        start = Offset(translateX, 0f),
                        end = Offset(translateX + bandWidth, 0f)
                    ),
                    topLeft = Offset(translateX, (sizePx - bandHeight) / 2f),
                    size = Size(bandWidth, bandHeight),
                    blendMode = BlendMode.SrcAtop
                )
            }
        }
}
