package com.max4real.compose_boilerplate.extensions

import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import java.util.Locale

fun MediaPickerItem.fileSubtitle(): String {
    return listOfNotNull(
        size?.formatReadableFileSize(),
        mimeType
    ).joinToString(separator = " - ").ifBlank {
        "Unknown file"
    }
}

fun MediaPickerItem.isImageFile(): Boolean {
    return mimeType?.startsWith("image/") == true ||
        displayName.hasAnyFileExtension(
            "jpg",
            "jpeg",
            "png",
            "webp",
            "gif",
            "bmp",
            "heic",
            "heif"
        )
}

fun MediaPickerItem.isVideoFile(): Boolean {
    return mimeType?.startsWith("video/") == true ||
        displayName.hasAnyFileExtension(
            "mp4",
            "mov",
            "mkv",
            "avi",
            "webm",
            "3gp",
            "m4v"
        )
}

fun MediaPickerItem.isPdfFile(): Boolean {
    return mimeType == "application/pdf" || displayName.hasAnyFileExtension("pdf")
}

fun String?.hasAnyFileExtension(vararg extensions: String): Boolean {
    val extension = this
        ?.substringBeforeLast("?")
        ?.substringBeforeLast("#")
        ?.substringAfterLast(".", missingDelimiterValue = "")
        ?.lowercase()
        ?.trim()
        .orEmpty()

    return extension in extensions
}

fun Long.formatReadableFileSize(): String {
    if (this <= 0L) return "Unknown size"

    val kiloBytes = this / 1024.0
    if (kiloBytes < 1024.0) {
        return String.format(Locale.US, "%.0f KB", kiloBytes)
    }

    val megaBytes = kiloBytes / 1024.0
    if (megaBytes < 1024.0) {
        return String.format(Locale.US, "%.1f MB", megaBytes)
    }

    return String.format(Locale.US, "%.1f GB", megaBytes / 1024.0)
}
