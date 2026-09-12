package com.max4real.compose_boilerplate.shared.widget.mediapicker.file_widgets

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.HeightBox
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.extensions.fileIconPainter
import com.max4real.compose_boilerplate.extensions.fileSubtitle
import com.max4real.compose_boilerplate.extensions.isImageFile
import com.max4real.compose_boilerplate.extensions.isPdfFile
import com.max4real.compose_boilerplate.extensions.isVideoFile
import com.max4real.compose_boilerplate.extensions.loadPdfThumbnail
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.shared.widget.mediapicker.loadVideoThumbnail
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
internal fun RecentFilesCard(
    files: List<MediaPickerItem>,
    onFileClick: (MediaPickerItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(theme.bottomSheetInputContainer)
    ) {
        files.forEachIndexed { index, file ->
            RecentFileRow(
                file = file,
                showDivider = index != files.lastIndex,
                onClick = {
                    onFileClick(file)
                }
            )
        }
    }
}

@Composable
private fun RecentFileRow(
    file: MediaPickerItem,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(color = theme.rippleColor),
                    onClick = onClick
                )
                .padding(start = 14.dp, end = 15.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecentFilePreview(file = file)

            12.WidthBox()

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.displayName ?: "File",
                    color = theme.mainText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = OpenAISans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.4f).sp
                )

                3.HeightBox()

                Text(
                    text = file.fileSubtitle(),
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.W400,
                    fontFamily = OpenAISans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.4f).sp
                )
            }

            5.WidthBox()

            Icon(
                painter = painterResource(R.drawable.arrowdown2),
                contentDescription = null,
                tint = theme.rowChevron,
                modifier = Modifier.size(16.dp)
            )
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 68.dp, end = 15.dp)
                    .height(1.dp)
                    .background(theme.bottomSheetInputDivider.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun RecentFilePreview(
    file: MediaPickerItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(theme.background2),
        contentAlignment = Alignment.Center
    ) {
        when {
            file.isImageFile() -> {
                SubcomposeAsyncImage(
                    model = file.uri,
                    contentDescription = file.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            FileIconPreview(file = file)
                        }

                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }

            file.isVideoFile() -> {
                VideoFilePreview(
                    file = file,
                    modifier = Modifier.matchParentSize()
                )
            }

            file.isPdfFile() -> {
                PdfFilePreview(
                    file = file,
                    modifier = Modifier.matchParentSize()
                )
            }

            else -> {
                FileIconPreview(file = file)
            }
        }
    }
}

@Composable
private fun VideoFilePreview(
    file: MediaPickerItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, file.uri) {
        value = withContext(Dispatchers.IO) {
            context.loadVideoThumbnail(file.uri)
        }
    }

    Box(modifier = modifier) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = file.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                FileIconPreview(file = file)
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.play),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PdfFilePreview(
    file: MediaPickerItem,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, file.uri) {
        value = withContext(Dispatchers.IO) {
            context.loadPdfThumbnail(file.uri)
        }
    }

    if (bitmap != null) {
        Box(modifier = modifier) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = file.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            FileTypeBadge(
                file = file,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    } else {
        FileIconPreview(file = file)
    }
}

@Composable
private fun FileIconPreview(
    file: MediaPickerItem
) {
    Image(
        painter = file.fileIconPainter(),
        contentDescription = null,
        modifier = Modifier.size(28.dp)
    )
}

@Composable
private fun FileTypeBadge(
    file: MediaPickerItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(RoundedCornerShape(topStart = 6.dp))
            .background(theme.bottomSheetInputContainer.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = file.fileIconPainter(),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
    }
}
