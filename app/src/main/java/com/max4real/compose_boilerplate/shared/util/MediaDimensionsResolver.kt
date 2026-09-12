package com.max4real.compose_boilerplate.shared.util

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItemType

data class MediaDimensions(
    val width: Int?,
    val height: Int?
) {
    val hasValidSize: Boolean
        get() = width != null && width > 0 && height != null && height > 0
}

fun Context.resolveMediaDimensions(item: MediaPickerItem): MediaDimensions {
    val existing = MediaDimensions(width = item.width, height = item.height)

    return when (item.type) {
        MediaPickerItemType.PHOTO -> resolveImageDimensions(item.uri).takeIf { it.hasValidSize }
            ?: existing
        MediaPickerItemType.VIDEO -> resolveVideoDimensions(item.uri).takeIf { it.hasValidSize }
            ?: existing
        MediaPickerItemType.FILE -> MediaDimensions(width = null, height = null)
    }
}

fun Context.resolveImageDimensions(uri: Uri): MediaDimensions {
    return runCatching {
        contentResolver.openInputStream(uri)?.use { input ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            val rawDimensions = MediaDimensions(
                width = options.outWidth.takeIf { it > 0 },
                height = options.outHeight.takeIf { it > 0 }
            )
            val rotation = resolveImageRotationDegrees(uri)

            if (rotation == 90 || rotation == 270) {
                MediaDimensions(width = rawDimensions.height, height = rawDimensions.width)
            } else {
                rawDimensions
            }
        }
    }.getOrNull() ?: MediaDimensions(width = null, height = null)
}

private fun Context.resolveImageRotationDegrees(uri: Uri): Int? {
    return runCatching {
        contentResolver.openInputStream(uri)?.use { input ->
            when (ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_TRANSPOSE -> 90

                ExifInterface.ORIENTATION_ROTATE_180,
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180

                ExifInterface.ORIENTATION_ROTATE_270,
                ExifInterface.ORIENTATION_TRANSVERSE -> 270

                else -> 0
            }
        }
    }.getOrNull()
}

fun Context.resolveVideoDimensions(uri: Uri): MediaDimensions {
    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, uri)
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull()

            if (rotation == 90 || rotation == 270) {
                MediaDimensions(width = height, height = width)
            } else {
                MediaDimensions(width = width, height = height)
            }
        } finally {
            retriever.release()
        }
    }.getOrNull()?.takeIf { it.hasValidSize }
        ?: MediaDimensions(width = null, height = null)
}
