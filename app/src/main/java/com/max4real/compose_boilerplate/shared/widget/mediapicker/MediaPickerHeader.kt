package com.max4real.compose_boilerplate.shared.widget.mediapicker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.ui.theme.LocalAppDarkMode
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme

private data class MediaPickerHeaderColors(
    val segmentTrack: Color,
    val segmentIndicator: Color,
    val selectedTabText: Color,
    val unselectedTabText: Color,
    val counterBackground: Color,
    val counterBorder: Color,
    val sendBackground: Color
)

@Composable
private fun mediaPickerHeaderColors(): MediaPickerHeaderColors {
    val isDarkMode = LocalAppDarkMode.current

    return if (isDarkMode) {
        MediaPickerHeaderColors(
            segmentTrack = Color(0xFF2D2D2D),
            segmentIndicator = Color(0xFFEDEDED),
            selectedTabText = Color.Black,
            unselectedTabText = Color.White.copy(alpha = 0.82f),
            counterBackground = theme.background,
            counterBorder = Color(0xFF3A3A3A),
            sendBackground = Color(0xFF2277FF)
        )
    } else {
        MediaPickerHeaderColors(
            segmentTrack = Color(0xFFD9D9D9),
            segmentIndicator = Color.White,
            selectedTabText = Color.Black,
            unselectedTabText = Color.White,
            counterBackground = Color.White,
            counterBorder = Color(0xFFE6E6E6),
            sendBackground = Color(0xFF2277FF)
        )
    }
}

@Composable
internal fun MediaPickerHeader(
    selectionCount: Int,
    mode: MediaPickerMode,
    selectedTab: MediaPickerTab,
    onTabSelected: (MediaPickerTab) -> Unit,
    onSend: () -> Unit,
    galleryLabel: String = "Gallery",
    showGalleryAlbumTrigger: Boolean = true,
    onGalleryAlbumClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = mediaPickerHeaderColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 67.dp)
            .padding(top = 12.dp, bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selectionCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                SelectionCountBadge(
                    count = selectionCount,
                    colors = colors
                )
            }
        }

        if (mode == MediaPickerMode.ALL) {
            MediaPickerSegmentedTabs(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                galleryLabel = galleryLabel,
                showGalleryAlbumTrigger = showGalleryAlbumTrigger,
                onGalleryAlbumClick = onGalleryAlbumClick,
                colors = colors
            )
        } else {
            GalleryAlbumTitleButton(
                label = galleryLabel,
                enabled = showGalleryAlbumTrigger,
                onClick = onGalleryAlbumClick
            )
        }

        if (selectionCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                PickerSendButton(
                    colors = colors,
                    onClick = onSend,
                    mode = selectedTab
                )
            }
        }
    }
}

@Composable
private fun SelectionCountBadge(
    count: Int,
    colors: MediaPickerHeaderColors
) {
    Box(
        modifier = Modifier
            .width(42.dp)
            .height(33.dp)
            .clip(CircleShape)
            .background(colors.counterBackground)
            .border(
                width = 2.dp,
                color = colors.counterBorder,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            color = theme.mainText,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.W700,
            fontFamily = OpenAISans,
            maxLines = 1,
            letterSpacing = (-0.4f).sp
        )
    }
}

@Composable
private fun MediaPickerSegmentedTabs(
    selectedTab: MediaPickerTab,
    onTabSelected: (MediaPickerTab) -> Unit,
    galleryLabel: String,
    showGalleryAlbumTrigger: Boolean,
    onGalleryAlbumClick: () -> Unit,
    colors: MediaPickerHeaderColors,
    modifier: Modifier = Modifier
) {
    val animationSpec = tween<Dp>(
        durationMillis = 250,
        easing = FastOutSlowInEasing
    )

    BoxWithConstraints(
        modifier = modifier
            .width(165.dp)
            .height(43.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(colors.segmentTrack)
            .padding(5.dp)
    ) {
        val tabWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTab == MediaPickerTab.GALLERY) 0.dp else tabWidth,
            animationSpec = animationSpec,
            label = "MediaPickerTabIndicatorOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(21.dp))
                .background(colors.segmentIndicator)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaPickerSegmentedTabButton(
                label = galleryLabel,
                selected = selectedTab == MediaPickerTab.GALLERY,
                showArrow = selectedTab == MediaPickerTab.GALLERY && showGalleryAlbumTrigger,
                colors = colors,
                onClick = {
                    if (selectedTab == MediaPickerTab.GALLERY) {
                        onGalleryAlbumClick()
                    } else {
                        onTabSelected(MediaPickerTab.GALLERY)
                    }
                },
                modifier = Modifier.weight(1f)
            )

            MediaPickerSegmentedTabButton(
                label = "File",
                selected = selectedTab == MediaPickerTab.FILE,
                colors = colors,
                onClick = {
                    onTabSelected(MediaPickerTab.FILE)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MediaPickerSegmentedTabButton(
    label: String,
    selected: Boolean,
    showArrow: Boolean = false,
    colors: MediaPickerHeaderColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.selectedTabText else colors.unselectedTabText,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "MediaPickerTabTextColor"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.W600,
                fontFamily = OpenAISans,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.4f).sp,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (showArrow) {
                Icon(
                    painter = painterResource(R.drawable.arrow_down),
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GalleryAlbumTitleButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = theme.mainText,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = (-0.4f).sp
        )

        if (enabled) {
            Icon(
                painter = painterResource(R.drawable.arrowdown2),
                contentDescription = null,
                tint = theme.mainText,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(13.dp)
            )
        }
    }
}

@Composable
private fun PickerSendButton(
    mode: MediaPickerTab,
    colors: MediaPickerHeaderColors,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(colors.sendBackground)
            .clickable(
                onClick = onClick,
                interactionSource = interactionSource,
                indication = ripple(),
            )
            .padding(horizontal = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (mode == MediaPickerTab.GALLERY) "Add" else "Send",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.W600,
            fontFamily = OpenAISans,
            maxLines = 1,
            letterSpacing = (-0.4f).sp
        )
    }
}
