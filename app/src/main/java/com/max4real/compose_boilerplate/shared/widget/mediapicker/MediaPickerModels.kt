package com.max4real.compose_boilerplate.shared.widget.mediapicker

import android.net.Uri

enum class MediaPickerMode {
    PHOTO,
    MEDIA,
    ALL
}

data class MediaPickerResult(
    val type: MediaPickerResultType,
    val items: List<MediaPickerItem>
)

enum class MediaPickerResultType {
    PHOTO, // only Photo - Compress
    VIDEO, // only Video - Compress
    MEDIA, // both photo and video - Compress

    FILE // only file - Uncompress
}

data class MediaPickerItem(
    val uri: Uri,
    val type: MediaPickerItemType,
    val displayName: String? = null,
    val mimeType: String? = null,
    val size: Long? = null,
    val dateAddedMillis: Long? = null,
    val durationMillis: Long? = null,
    val waveForm: List<Float>? = null,
    val width: Int? = null,
    val height: Int? = null
)

internal data class MediaPickerAlbum(
    val id: String?,
    val displayName: String,
    val coverUri: Uri?,
    val itemCount: Int,
    val isAllMedia: Boolean = false
)

enum class MediaPickerItemType {
    PHOTO,
    VIDEO,
    FILE
}

typealias OnMediaPickerSelect = (MediaPickerResult) -> Unit

internal fun List<MediaPickerItem>.toPickerResult(fallbackType: MediaPickerResultType): MediaPickerResult {
    val resolvedType = when {
        any { it.type == MediaPickerItemType.FILE } -> MediaPickerResultType.FILE
        isEmpty() -> fallbackType
        all { it.type == MediaPickerItemType.PHOTO } -> MediaPickerResultType.PHOTO
        all { it.type == MediaPickerItemType.VIDEO } -> MediaPickerResultType.VIDEO
        else -> MediaPickerResultType.MEDIA
    }

    return MediaPickerResult(
        type = resolvedType,
        items = this
    )
}
