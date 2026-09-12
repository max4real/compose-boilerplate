package com.max4real.compose_boilerplate.shared.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.shared.util.mylog
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlin.math.absoluteValue

@Composable
fun CustomAvatar(
    imageUrl: String?,
    name: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    decodeSize: Dp = size,
    shape: Shape = CircleShape,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    iconColor: Color = theme.textSecondary,
    contentDescription: String = "Avatar",
    includeShadow: Boolean = false,
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    isClickable: Boolean = true,
    isDeletedAccount: Boolean = false,
    isSavedMessages: Boolean = false,
    onTap: () -> Unit = {},
) {
    // Deleted accounts and the Saved Messages self-chat never show a stale photo or
    // name-derived initials — always their dedicated placeholder icon.
    val effectiveImageUrl = if (isDeletedAccount || isSavedMessages) null else imageUrl
    val effectiveName = if (isDeletedAccount || isSavedMessages) null else name

    val hasName = !effectiveName.isNullOrBlank()

    val avatarColors = remember(effectiveName) {
        avatarColorsFromName(effectiveName)
    }

    val resolvedBackgroundColor = backgroundColor
        ?: if (hasName) avatarColors.background else theme.surface

    val resolvedTextColor = textColor
        ?: if (hasName) avatarColors.text else theme.textSecondary

    Box(
        modifier = modifier
            .then(
                if (includeShadow) {
                    Modifier.dropShadow(
                        shape = shape,
                        shadow = Shadow(
                            radius = 40.dp,
                            spread = 0.dp,
                            offset = DpOffset(x = 0.dp, y = 4.dp),
                            color = theme.iconButtonShadow
                        )
                    )
                } else {
                    Modifier
                }
            )
            .size(size)
            .clip(shape)
            .then(
                if (effectiveImageUrl.isNullOrBlank()) {
                    Modifier.background(resolvedBackgroundColor)
                } else {
                    Modifier
                }
            )
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(
                        width = borderWidth,
                        color = borderColor,
                        shape = shape
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (isClickable) {
                    Modifier.clickable {
                        onTap()
                        mylog("CustomAvatar: tapped url - $effectiveImageUrl")
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (effectiveImageUrl.isNullOrBlank()) {
            AvatarPlaceholder(
                name = effectiveName,
                size = size,
                iconColor = iconColor,
                textColor = resolvedTextColor,
                isDeletedAccount = isDeletedAccount,
                isSavedMessages = isSavedMessages
            )
        } else {
            val context = LocalContext.current
            val density = LocalDensity.current
            val decodeSizePx = with(density) { decodeSize.roundToPx() }
            val imageRequest = remember(effectiveImageUrl, decodeSizePx) {
                ImageRequest.Builder(context)
                    .data(effectiveImageUrl)
                    .size(decodeSizePx, decodeSizePx)
                    .build()
            }

            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(size)
                    .clip(shape),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Shimmer()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF171C22)), // TODO: later remove the bg
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarPlaceholder(
                            name = effectiveName,
                            size = size,
                            iconColor = iconColor,
                            textColor = resolvedTextColor,
                            isDeletedAccount = isDeletedAccount,
                            isSavedMessages = isSavedMessages
                        )
                    }
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )
        }
    }
}

@Composable
private fun AvatarPlaceholder(
    name: String?,
    size: Dp,
    iconColor: Color,
    textColor: Color,
    isDeletedAccount: Boolean = false,
    isSavedMessages: Boolean = false
) {
    val avatarLabel = remember(name) {
        name.avatarInitials()
    }

    if (isDeletedAccount) {
        Icon(
            painter = painterResource(R.drawable.ghost_1),
            contentDescription = "Deleted Account",
            tint = iconColor,
            modifier = Modifier
                .size(size * 0.7f)
                .padding(5.dp)
        )
    } else if (isSavedMessages) {
        Icon(
            painter = painterResource(R.drawable.archive),
            contentDescription = "Saved Messages",
            tint = iconColor,
            modifier = Modifier
                .size(size * 0.7f)
                .padding(5.dp)
        )
    } else if (avatarLabel.isNotBlank()) {
        val fontSize = (size.value * 0.34f).sp
        Text(
            text = avatarLabel,
            color = textColor,
            fontSize = fontSize,
            lineHeight = fontSize,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            letterSpacing = (-0.4).sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    } else {
        Icon(
            painter = painterResource(R.drawable.user),
            contentDescription = "Avatar Placeholder",
            tint = iconColor,
            modifier = Modifier
                .size(size * 0.7f)
                .padding(5.dp)
        )
    }
}

private fun String?.avatarInitials(): String {
    val cleanName = this
        ?.trim()
        ?.replace(Regex("\\s+"), " ")
        .orEmpty()

    if (cleanName.isBlank()) return ""

    val parts = cleanName.split(" ")

    return when {
        parts.size >= 2 -> {
            val first = parts.firstOrNull()?.firstOrNull()
            val second = parts.getOrNull(1)?.firstOrNull()

            listOfNotNull(first, second)
                .joinToString("")
                .uppercase()
        }

        cleanName.length >= 2 -> {
            cleanName.take(2).uppercase()
        }

        else -> {
            cleanName.uppercase()
        }
    }
}

data class AvatarColors(
    val background: Color,
    val text: Color
)

fun avatarColorsFromName(
    name: String?,
    includeIDontLikeColor: Boolean = true
): AvatarColors {
    val cleanName = name
        ?.trim()
        ?.lowercase()
        .orEmpty()

    val palettes = buildList {
        add(
            AvatarColors(
                background = Color(0xFFE53935),
                text = Color.White
            )
        )

        if (includeIDontLikeColor) {
            add(
                AvatarColors(
                    background = Color(0xFFF57C00),
                    text = Color.White
                )
            )
            add(
                AvatarColors(
                    background = Color(0xFFFFCA28),
                    text = Color(0xFF5D4037)
                )
            )
        }

        add(
            AvatarColors(
                background = Color(0xFF43A047),
                text = Color.White
            )
        )

        add(
            AvatarColors(
                background = Color(0xFF1E88E5),
                text = Color.White
            )
        )

        add(
            AvatarColors(
                background = Color(0xFF8E24AA),
                text = Color.White
            )
        )

        add(
            AvatarColors(
                background = Color(0xFFD81B60),
                text = Color.White
            )
        )
    }

    if (cleanName.isBlank()) {
        return palettes.first()
    }

    val index = cleanName.hashCode().absoluteValue % palettes.size
    return palettes[index]
}

// soft
//private fun avatarColorsFromName(name: String?): AvatarColors {
//    val cleanName = name
//        ?.trim()
//        ?.lowercase()
//        .orEmpty()
//
//    val palettes = listOf(
//        AvatarColors(
//            background = Color(0xFFFFF4D8),
//            text = Color(0xFF8F5A00)
//        ),
//        AvatarColors(
//            background = Color(0xFFDDECFB),
//            text = Color(0xFF315D86)
//        ),
//        AvatarColors(
//            background = Color(0xFFE9E0D8),
//            text = Color(0xFF624C3E)
//        ),
//        AvatarColors(
//            background = Color(0xFFE7E2F8),
//            text = Color(0xFF56488A)
//        ),
//        AvatarColors(
//            background = Color(0xFFDDF5E7),
//            text = Color(0xFF2E6B46)
//        ),
//        AvatarColors(
//            background = Color(0xFFFFE1E8),
//            text = Color(0xFF9A3F55)
//        ),
//        AvatarColors(
//            background = Color(0xFFE2F2F0),
//            text = Color(0xFF2E6962)
//        ),
//        AvatarColors(
//            background = Color(0xFFF1E4D8),
//            text = Color(0xFF76513A)
//        )
//    )
//
//    if (cleanName.isBlank()) {
//        return palettes.first()
//    }
//
//    val index = abs(cleanName.hashCode()) % palettes.size
//    return palettes[index]
//}