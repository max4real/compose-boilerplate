package com.max4real.compose_boilerplate.shared.widget.mediapicker.file_widgets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.max4real.compose_boilerplate.extensions.HeightBox
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.ui.theme.OpenAISans
import com.max4real.compose_boilerplate.ui.theme.theme

@Composable
internal fun FilePickerContent(
    contentPadding: PaddingValues = PaddingValues(bottom = 16.dp),
    recentFiles: List<MediaPickerItem>,
    onPickLocalFiles: () -> Unit,
    onPickGalleryFiles: () -> Unit,
    onRecentFileClick: (MediaPickerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        contentPadding = contentPadding
    ) {
        item {
            FilePickerSourceCard(
                onPickLocalFiles = onPickLocalFiles,
                onPickGalleryFiles = onPickGalleryFiles
            )
        }

        if (recentFiles.isNotEmpty()) {
            item {
                24.HeightBox()

                Text(
                    text = "Recent Files",
                    color = theme.mainText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W600,
                    fontFamily = OpenAISans,
                    letterSpacing = (-0.4).sp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 10.dp)
                )

                RecentFilesCard(
                    files = recentFiles,
                    onFileClick = onRecentFileClick
                )
            }
        }
    }
}
