package com.max4real.compose_boilerplate.shared.widget.mediapicker

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import java.util.Locale

internal data class MediaPickerPermissionState(
    val hasAccess: Boolean,
    val isLimited: Boolean
)

internal fun requiredMediaPermissions(mode: MediaPickerMode): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            buildList {
                if (mode.allowsPhotos) add(Manifest.permission.READ_MEDIA_IMAGES)
                if (mode.allowsVideos) add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            }.toTypedArray()
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            buildList {
                if (mode.allowsPhotos) add(Manifest.permission.READ_MEDIA_IMAGES)
                if (mode.allowsVideos) add(Manifest.permission.READ_MEDIA_VIDEO)
            }.toTypedArray()
        }

        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

internal fun Context.mediaPermissionState(mode: MediaPickerMode): MediaPickerPermissionState {
    val hasPermission: (String) -> Boolean = { permission ->
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val hasPhotoAccess = !mode.allowsPhotos || hasPermission(Manifest.permission.READ_MEDIA_IMAGES)
        val hasVideoAccess = !mode.allowsVideos || hasPermission(Manifest.permission.READ_MEDIA_VIDEO)
        val hasFullAccess = hasPhotoAccess && hasVideoAccess
        val hasLimitedAccess = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            hasPermission(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)

        return MediaPickerPermissionState(
            hasAccess = hasFullAccess || hasLimitedAccess,
            isLimited = hasLimitedAccess && !hasFullAccess
        )
    }

    return MediaPickerPermissionState(
        hasAccess = hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE),
        isLimited = false
    )
}

internal fun Context.hasCameraPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

internal const val MEDIA_PICKER_PAGE_SIZE = 120

internal data class MediaPickerPage(
    val items: List<MediaPickerItem>,
    val hasMore: Boolean
)

/**
 * Loads one page of gallery items sorted newest-first. Photos and videos are queried together
 * through [MediaStore.Files] (rather than as two separate collections) so a single LIMIT/OFFSET
 * pair produces a globally-correct page across both media types.
 */
internal fun queryMediaPickerItemsPage(
    context: Context,
    mode: MediaPickerMode,
    albumId: String? = null,
    offset: Int,
    pageSize: Int = MEDIA_PICKER_PAGE_SIZE
): MediaPickerPage {
    val mediaTypes = buildList {
        if (mode.allowsPhotos) add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        if (mode.allowsVideos) add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
    }
    if (mediaTypes.isEmpty()) return MediaPickerPage(items = emptyList(), hasMore = false)

    val selection = StringBuilder(
        "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (${mediaTypes.joinToString(",")})"
    )
    val selectionArgs = mutableListOf<String>()
    if (albumId != null) {
        selection.append(" AND ${MediaStore.MediaColumns.BUCKET_ID} = ?")
        selectionArgs.add(albumId)
    }

    // Fetch one extra row past the page size so we know whether another page remains.
    val items = context.queryFilesPaged(
        selection = selection.toString(),
        selectionArgs = selectionArgs.toTypedArray(),
        offset = offset,
        limit = pageSize + 1
    )

    return MediaPickerPage(
        items = items.take(pageSize),
        hasMore = items.size > pageSize
    )
}

internal fun queryMediaPickerAlbums(
    context: Context,
    mode: MediaPickerMode
): List<MediaPickerAlbum> {
    val bucketAlbums = context.queryMediaAlbumBuckets(mode)
        .sortedByDescending { it.latestDateAddedMillis }

    val allAlbum = MediaPickerAlbum(
        id = null,
        displayName = "Gallery",
        coverUri = bucketAlbums.firstOrNull()?.coverUri,
        itemCount = bucketAlbums.sumOf { it.itemCount },
        isAllMedia = true
    )

    val namedAlbums = bucketAlbums.map { bucket ->
        MediaPickerAlbum(
            id = bucket.id,
            displayName = bucket.displayName,
            coverUri = bucket.coverUri,
            itemCount = bucket.itemCount,
            isAllMedia = false
        )
    }

    return listOf(allAlbum) + namedAlbums
}

internal fun Context.fileItemsFromUris(uris: List<Uri>): List<MediaPickerItem> {
    return uris.distinct().map { uri ->
        runCatching {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val metadata = readOpenableMetadata(uri)

        MediaPickerItem(
            uri = uri,
            type = MediaPickerItemType.FILE,
            displayName = metadata.displayName ?: uri.lastPathSegment,
            mimeType = contentResolver.getType(uri),
            size = metadata.size
        )
    }
}

internal fun Long.formatPickerFileSize(): String {
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

internal fun Long.formatPickerDuration(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

internal val MediaPickerMode.allowsPhotos: Boolean
    get() = this == MediaPickerMode.PHOTO || this == MediaPickerMode.MEDIA || this == MediaPickerMode.ALL

internal val MediaPickerMode.allowsVideos: Boolean
    get() = this == MediaPickerMode.MEDIA || this == MediaPickerMode.ALL

private val MEDIA_FILE_PROJECTION = arrayOf(
    MediaStore.Files.FileColumns._ID,
    MediaStore.Files.FileColumns.MEDIA_TYPE,
    MediaStore.MediaColumns.DISPLAY_NAME,
    MediaStore.MediaColumns.MIME_TYPE,
    MediaStore.MediaColumns.SIZE,
    MediaStore.MediaColumns.DATE_ADDED,
    MediaStore.MediaColumns.WIDTH,
    MediaStore.MediaColumns.HEIGHT,
    MediaStore.Video.Media.DURATION
)

private fun perTypeContentUri(isVideo: Boolean): Uri = if (isVideo) {
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
} else {
    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
}

/**
 * Queries [MediaStore.Files] (the union of images + videos) with real LIMIT/OFFSET paging, so a
 * page boundary reflects a true position in the newest-first ordering rather than a client-side
 * cutoff. Uses the API 30+ query-args Bundle where available and falls back to LIMIT/OFFSET
 * embedded in the sort order for older API levels (both are honored by MediaStore's SQLite-backed
 * provider).
 */
private fun Context.queryFilesPaged(
    selection: String,
    selectionArgs: Array<String>,
    offset: Int,
    limit: Int
): List<MediaPickerItem> {
    val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

    return runCatching {
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
            }
            contentResolver.query(collectionUri, MEDIA_FILE_PROJECTION, queryArgs, null)
        } else {
            contentResolver.query(
                collectionUri,
                MEDIA_FILE_PROJECTION,
                selection,
                selectionArgs,
                "$sortOrder LIMIT $limit OFFSET $offset"
            )
        }

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val nameColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val mimeColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val dateColumn = c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val widthColumn = c.getColumnIndex(MediaStore.MediaColumns.WIDTH)
            val heightColumn = c.getColumnIndex(MediaStore.MediaColumns.HEIGHT)
            val durationColumn = c.getColumnIndex(MediaStore.Video.Media.DURATION)

            buildList {
                while (c.moveToNext()) {
                    val isVideo = c.getInt(mediaTypeColumn) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    val type = if (isVideo) MediaPickerItemType.VIDEO else MediaPickerItemType.PHOTO
                    val id = c.getLong(idColumn)
                    val dateAddedSeconds = c.getLong(dateColumn)

                    add(
                        MediaPickerItem(
                            uri = ContentUris.withAppendedId(perTypeContentUri(isVideo), id),
                            type = type,
                            displayName = c.getString(nameColumn),
                            mimeType = c.getString(mimeColumn),
                            size = c.getLong(sizeColumn),
                            dateAddedMillis = dateAddedSeconds * 1000L,
                            durationMillis = if (
                                isVideo && durationColumn >= 0 && !c.isNull(durationColumn)
                            ) {
                                c.getLong(durationColumn)
                            } else {
                                null
                            },
                            width = if (
                                !isVideo && widthColumn >= 0 && !c.isNull(widthColumn)
                            ) {
                                c.getInt(widthColumn).takeIf { it > 0 }
                            } else {
                                null
                            },
                            height = if (
                                !isVideo && heightColumn >= 0 && !c.isNull(heightColumn)
                            ) {
                                c.getInt(heightColumn).takeIf { it > 0 }
                            } else {
                                null
                            }
                        )
                    )
                }
            }
        }.orEmpty()
    }.getOrDefault(emptyList())
}

private data class MediaAlbumBucket(
    val id: String,
    val displayName: String,
    val coverUri: Uri,
    val itemCount: Int,
    val latestDateAddedMillis: Long
)

/**
 * Enumerates albums (buckets) in a single unbounded pass over [MediaStore.Files], grouping
 * photo + video rows that share a bucket id as they're read instead of running two separate
 * per-type queries and merging them afterwards.
 */
private fun Context.queryMediaAlbumBuckets(mode: MediaPickerMode): List<MediaAlbumBucket> {
    val mediaTypes = buildList {
        if (mode.allowsPhotos) add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
        if (mode.allowsVideos) add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
    }
    if (mediaTypes.isEmpty()) return emptyList()

    val collectionUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.MediaColumns.BUCKET_ID,
        MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_ADDED
    )
    val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (${mediaTypes.joinToString(",")})"

    return runCatching {
        val albums = linkedMapOf<String, MediaAlbumBucket>()
        contentResolver.query(
            collectionUri,
            projection,
            selection,
            null,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val bucketId = cursor.getString(bucketIdColumn) ?: continue
                val dateAddedMillis = cursor.getLong(dateColumn) * 1000L
                val current = albums[bucketId]
                val isVideo = cursor.getInt(mediaTypeColumn) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

                albums[bucketId] = MediaAlbumBucket(
                    id = bucketId,
                    displayName = cursor.getString(bucketNameColumn)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Album",
                    coverUri = current?.coverUri
                        ?: ContentUris.withAppendedId(perTypeContentUri(isVideo), cursor.getLong(idColumn)),
                    itemCount = (current?.itemCount ?: 0) + 1,
                    latestDateAddedMillis = maxOf(current?.latestDateAddedMillis ?: 0L, dateAddedMillis)
                )
            }
        }
        albums.values.toList()
    }.getOrDefault(emptyList())
}

private data class OpenableMetadata(
    val displayName: String?,
    val size: Long?
)

private fun Context.readOpenableMetadata(uri: Uri): OpenableMetadata {
    return runCatching {
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return@use OpenableMetadata(null, null)

            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)

            OpenableMetadata(
                displayName = if (nameColumn >= 0) cursor.getString(nameColumn) else null,
                size = if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) {
                    cursor.getLong(sizeColumn)
                } else {
                    null
                }
            )
        } ?: OpenableMetadata(null, null)
    }.getOrDefault(OpenableMetadata(null, null))
}
