package com.max4real.compose_boilerplate.shared.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import java.io.File

fun Context.createLocalVideoThumbnailUri(
    videoUri: Uri,
    fileName: String
): Uri? {
    val bitmap = runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(this, videoUri)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                retriever.getScaledFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                    LOCAL_VIDEO_THUMBNAIL_SIZE_PX,
                    LOCAL_VIDEO_THUMBNAIL_SIZE_PX
                )
            } else {
                retriever.getFrameAtTime(
                    0,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
            }
        } finally {
            retriever.release()
        }
    }.getOrNull() ?: return null

    return saveVideoThumbnail(bitmap, fileName)
}

private fun Context.saveVideoThumbnail(
    bitmap: Bitmap,
    fileName: String
): Uri? {
    return runCatching {
        val dir = File(cacheDir, LOCAL_VIDEO_THUMBNAIL_DIR).apply { mkdirs() }
        val cleanFileName = fileName
            .substringBeforeLast('.', missingDelimiterValue = fileName)
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "video" }
        val file = File(dir, "${cleanFileName}_${System.currentTimeMillis()}.jpg")

        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, LOCAL_VIDEO_THUMBNAIL_QUALITY, output)
        }

        Uri.fromFile(file)
    }.getOrNull()
}

private const val LOCAL_VIDEO_THUMBNAIL_DIR = "message_video_thumbnails"
private const val LOCAL_VIDEO_THUMBNAIL_SIZE_PX = 320
private const val LOCAL_VIDEO_THUMBNAIL_QUALITY = 82
