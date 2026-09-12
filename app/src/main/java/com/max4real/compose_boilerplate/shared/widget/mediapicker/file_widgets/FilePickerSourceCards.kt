package com.max4real.compose_boilerplate.shared.widget.mediapicker.file_widgets

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.extensions.HeightBox
import com.max4real.compose_boilerplate.extensions.WidthBox
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme


@Composable
internal fun FilePickerSourceCard(
    onPickLocalFiles: () -> Unit,
    onPickGalleryFiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(theme.bottomSheetInputContainer)
    ) {
        FilePickerSourceRow(
            icon = R.drawable.folder_add,
            title = "Local Storage",
            subtitle = "Browse files from this device",
            showDivider = true,
            onClick = onPickLocalFiles
        )

        FilePickerSourceRow(
            icon = R.drawable.gallery_2,
            title = "Gallery",
            subtitle = "Send photos and videos as files",
            showDivider = false,
            onClick = onPickGalleryFiles
        )
    }
}

@Composable
private fun FilePickerSourceRow(
    icon: Int,
    title: String,
    subtitle: String,
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
                .padding(start = 18.dp, end = 15.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = theme.cardRowIconsColor,
                modifier = Modifier.size(24.dp)
            )

            15.WidthBox()

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = theme.mainText,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = OpenAISans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.4).sp
                )

                3.HeightBox()

                Text(
                    text = subtitle,
                    color = theme.mainText.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.W400,
                    fontFamily = OpenAISans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.4).sp
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
                    .padding(start = 57.dp, end = 15.dp)
                    .height(1.dp)
                    .background(theme.bottomSheetInputDivider.copy(alpha = 0.55f))
            )
        }
    }
}
