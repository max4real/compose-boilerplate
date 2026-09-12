package com.max4real.compose_boilerplate.shared.widget.mediapicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.HeightBox
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.ui.theme.CustomColor
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme

@Composable
internal fun MediaPickerAlbumScrim(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onDismiss
            )
    )
}

@Composable
internal fun AlbumPickerDropdown(
    albums: List<MediaPickerAlbum>,
    selectedAlbum: MediaPickerAlbum?,
    onAlbumSelected: (MediaPickerAlbum) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(230.dp)
            .heightIn(max = 310.dp),
        shape = RoundedCornerShape(22.dp),
        color = theme.surface,
        tonalElevation = 10.dp,
        shadowElevation = 12.dp
    ) {
        LazyColumn {
            items(
                items = albums,
                key = { album -> album.id ?: "all_media" }
            ) { album ->
                AlbumPickerRow(
                    album = album,
                    selected = album.id == selectedAlbum?.id,
                    onClick = { onAlbumSelected(album) }
                )
            }
        }
    }
}

@Composable
private fun AlbumPickerRow(
    album: MediaPickerAlbum,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (selected) {
                    CustomColor.accentBlue.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.background2),
            contentAlignment = Alignment.Center
        ) {
            if (album.coverUri != null) {
                AsyncImage(
                    model = album.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.gallery_2),
                    contentDescription = null,
                    tint = theme.cardRowIconsColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        8.WidthBox()

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = album.displayName,
                color = theme.mainText,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W600,
                fontFamily = OpenAISans,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                letterSpacing = (-0.4f).sp
            )

            3.HeightBox()

            Text(
                text = "${album.itemCount} items",
                color = theme.textSecondary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.W500,
                fontFamily = OpenAISans,
                maxLines = 1,
                letterSpacing = (-0.4f).sp
            )
        }

        if (selected) {
            Icon(
                painter = painterResource(R.drawable.tick_circle),
                contentDescription = null,
                tint = CustomColor.accentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
