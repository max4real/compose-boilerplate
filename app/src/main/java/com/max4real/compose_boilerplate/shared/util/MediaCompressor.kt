package com.max4real.compose_boilerplate.shared.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItemType
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerResult
import com.max4real.compose_boilerplate.shared.util.mylog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

enum class MediaCompressionMode {
    COMPRESSED,
    ORIGINAL
}

enum class MediaCompressionPreset {
    /**
     * Very small file.
     * Good for weak network / quick upload.
     */
    VERY_LOW,

    /**
     * Small file, acceptable chat preview quality.
     */
    LOW,

    /**
     * Recommended default for chat.
     */
    BALANCED,

    /**
     * Better visual quality, bigger file.
     */
    HIGH,

    /**
     * Keep more resolution and bitrate.
     * Good when user expects clearer media.
     */
    VERY_HIGH
}

enum class MediaCompressionIntent {
    AUTO,
    VERY_LOW,
    LOW,
    BALANCED,
    HIGH,
    VERY_HIGH,
    ORIGINAL
}

data class MediaCompressionEstimate(
    val originalUri: Uri,
    val type: MediaPickerItemType,

    val originalSize: Long?,
    val estimatedOutputSize: Long?,

    val originalWidth: Int? = null,
    val originalHeight: Int? = null,

    val estimatedWidth: Int? = null,
    val estimatedHeight: Int? = null,

    val durationMillis: Long? = null,
    val targetVideoBitrate: Int? = null,

    val willUseOriginal: Boolean = false
)

data class MediaCompressionConfig(
    val scale: Float,
    val imageQuality: Int,
    val minVideoShortSide: Int,
    val videoBitrateFactor: Float,
    val minVideoBitrate: Int,
    val maxVideoBitrate: Int,
    val estimatedAudioBitrate: Int,
    val skipIfEstimatedBiggerThanOriginal: Boolean = true,
    val fallbackToOriginalIfBigger: Boolean = true
)

fun MediaCompressionPreset.toConfig(): MediaCompressionConfig {
    return when (this) {
        MediaCompressionPreset.VERY_LOW -> MediaCompressionConfig(
            scale = 0.18f,
            imageQuality = 48,
            minVideoShortSide = 216,
            videoBitrateFactor = 0.12f,
            minVideoBitrate = 60_000,
            maxVideoBitrate = 180_000,
            estimatedAudioBitrate = 48_000
        )

        MediaCompressionPreset.LOW -> MediaCompressionConfig(
            scale = 0.25f,
            imageQuality = 58,
            minVideoShortSide = 280,
            videoBitrateFactor = 0.20f,
            minVideoBitrate = 90_000,
            maxVideoBitrate = 320_000,
            estimatedAudioBitrate = 64_000
        )

        MediaCompressionPreset.BALANCED -> MediaCompressionConfig(
            scale = 0.50f,
            imageQuality = 75,
            minVideoShortSide = 480,
            videoBitrateFactor = 0.45f,
            minVideoBitrate = 250_000,
            maxVideoBitrate = 1_200_000,
            estimatedAudioBitrate = 96_000
        )

        MediaCompressionPreset.HIGH -> MediaCompressionConfig(
            scale = 0.70f,
            imageQuality = 82,
            minVideoShortSide = 540,
            videoBitrateFactor = 0.60f,
            minVideoBitrate = 350_000,
            maxVideoBitrate = 1_800_000,
            estimatedAudioBitrate = 128_000
        )

        MediaCompressionPreset.VERY_HIGH -> MediaCompressionConfig(
            scale = 0.85f,
            imageQuality = 88,
            minVideoShortSide = 720,
            videoBitrateFactor = 0.75f,
            minVideoBitrate = 500_000,
            maxVideoBitrate = 2_800_000,
            estimatedAudioBitrate = 128_000
        )
    }
}

data class CompressedMediaResult(
    val originalUri: Uri,
    val outputUri: Uri,

    val type: MediaPickerItemType,

    val displayName: String?,
    val mimeType: String?,

    val originalSize: Long?,
    val outputSize: Long,

    val originalWidth: Int? = null,
    val originalHeight: Int? = null,

    val outputWidth: Int? = null,
    val outputHeight: Int? = null,

    val durationMillis: Long? = null,

    val estimatedOutputSize: Long? = null,
    val targetVideoBitrate: Int? = null,

    val mode: MediaCompressionMode
)

class MediaCompressor(
    private val context: Context
) {

    suspend fun compressPickerResult(
        result: MediaPickerResult,
        preset: MediaCompressionPreset = MediaCompressionPreset.BALANCED,
        onProgress: (itemIndex: Int, progress: Float) -> Unit = { _, _ -> }
    ): List<CompressedMediaResult> {
        return compressPickerResult(
            result = result,
            config = preset.toConfig(),
            onProgress = onProgress
        )
    }

    suspend fun compressPickerResult(
        result: MediaPickerResult,
        config: MediaCompressionConfig,
        onProgress: (itemIndex: Int, progress: Float) -> Unit = { _, _ -> }
    ): List<CompressedMediaResult> {
        return result.items.mapIndexed { index, item ->
            when (item.type) {
                MediaPickerItemType.PHOTO -> {
                    onProgress(index, 0f)

                    compressImage(
                        item = item,
                        scale = config.scale,
                        quality = config.imageQuality,
                        fallbackToOriginalIfBigger = config.fallbackToOriginalIfBigger
                    ).also {
                        onProgress(index, 1f)
                    }
                }

                MediaPickerItemType.VIDEO -> {
                    compressVideo(
                        item = item,
                        config = config,
                        onProgress = { progress ->
                            onProgress(index, progress)
                        }
                    )
                }

                MediaPickerItemType.FILE -> {
                    onProgress(index, 1f)
                    buildOriginalResult(item)
                }
            }
        }
    }

    suspend fun compressImage(
        item: MediaPickerItem,
        preset: MediaCompressionPreset = MediaCompressionPreset.BALANCED
    ): CompressedMediaResult {
        val config = preset.toConfig()

        return compressImage(
            item = item,
            scale = config.scale,
            quality = config.imageQuality,
            fallbackToOriginalIfBigger = config.fallbackToOriginalIfBigger
        )
    }

    suspend fun compressImage(
        item: MediaPickerItem,
        scale: Float = 0.50f,
        quality: Int = 75,
        fallbackToOriginalIfBigger: Boolean = true
    ): CompressedMediaResult = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(item.uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }

        val originalWidth = boundsOptions.outWidth
        val originalHeight = boundsOptions.outHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            return@withContext buildOriginalResult(item)
        }

        val safeScale = scale.coerceIn(0.1f, 1f)

        val targetWidth = max(1, (originalWidth * safeScale).roundToInt())
        val targetHeight = max(1, (originalHeight * safeScale).roundToInt())

        val sampleSize = calculateInSampleSize(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            targetWidth = targetWidth,
            targetHeight = targetHeight
        )

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = resolver.openInputStream(item.uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: return@withContext buildOriginalResult(item)

        val resizedBitmap = Bitmap.createScaledBitmap(
            decodedBitmap,
            targetWidth,
            targetHeight,
            true
        )

        if (resizedBitmap != decodedBitmap) {
            decodedBitmap.recycle()
        }

        val outputFile = createTempOutputFile(
            prefix = "compressed_image_",
            extension = ".jpg"
        )

        FileOutputStream(outputFile).use { output ->
            resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality.coerceIn(1, 100),
                output
            )
        }

        resizedBitmap.recycle()

        val originalSize = item.size
        val compressedSize = outputFile.length()

        if (
            fallbackToOriginalIfBigger &&
            originalSize != null &&
            compressedSize >= originalSize
        ) {
            outputFile.delete()
            return@withContext buildOriginalResult(item)
        }

        CompressedMediaResult(
            originalUri = item.uri,
            outputUri = outputFile.toUri(),
            type = MediaPickerItemType.PHOTO,
            displayName = item.displayName ?: outputFile.name,
            mimeType = "image/jpeg",
            originalSize = originalSize,
            outputSize = compressedSize,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            outputWidth = targetWidth,
            outputHeight = targetHeight,
            durationMillis = null,
            estimatedOutputSize = null,
            targetVideoBitrate = null,
            mode = MediaCompressionMode.COMPRESSED
        )
    }

    @OptIn(UnstableApi::class)
    suspend fun compressVideo(
        item: MediaPickerItem,
        preset: MediaCompressionPreset = MediaCompressionPreset.BALANCED,
        onProgress: (Float) -> Unit = {}
    ): CompressedMediaResult {
        return compressVideo(
            item = item,
            config = preset.toConfig(),
            onProgress = onProgress
        )
    }

    @OptIn(UnstableApi::class)
    suspend fun compressVideo(
        item: MediaPickerItem,
        config: MediaCompressionConfig,
        onProgress: (Float) -> Unit = {}
    ): CompressedMediaResult {
        val metadata = readVideoMetadata(item.uri)

        val originalWidth = metadata.displayWidth
        val originalHeight = metadata.displayHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            return buildOriginalResult(item)
        }

        val targetSize = calculateVideoTargetSize(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            scale = config.scale,
            minShortSide = config.minVideoShortSide
        )

        val durationMillis = item.durationMillis ?: metadata.durationMillis

        val targetVideoBitrate = calculateTargetVideoBitrate(
            originalBitrate = metadata.bitrate,
            targetWidth = targetSize.width,
            targetHeight = targetSize.height,
            durationMillis = durationMillis,
            originalSize = item.size,
            videoBitrateFactor = config.videoBitrateFactor,
            minVideoBitrate = config.minVideoBitrate,
            maxVideoBitrate = config.maxVideoBitrate
        )

        val rawEstimatedOutputSize = estimateVideoOutputSizeBytes(
            durationMillis = durationMillis,
            videoBitrate = targetVideoBitrate,
            audioBitrate = metadata.audioBitrate ?: config.estimatedAudioBitrate
        )

        val estimatedOutputSize = rawEstimatedOutputSize
            ?.let { (it * 3.5f).toLong() }

        val originalSize = item.size

        if (
            config.skipIfEstimatedBiggerThanOriginal &&
            originalSize != null &&
            estimatedOutputSize != null &&
            estimatedOutputSize >= originalSize
        ) {
            onProgress(1f)

            return buildOriginalResult(
                item = item,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                durationMillis = durationMillis,
                estimatedOutputSize = estimatedOutputSize,
                targetVideoBitrate = targetVideoBitrate
            )
        }

        val outputFile = createTempOutputFile(
            prefix = "compressed_video_",
            extension = ".mp4"
        )

        onProgress(0f)

        runCatching {
            transcodeVideoWithMedia3(
                inputUri = item.uri,
                outputFile = outputFile,
                targetWidth = targetSize.width,
                targetHeight = targetSize.height,
                targetVideoBitrate = targetVideoBitrate,
                onProgress = onProgress
            )
        }.onFailure { error ->
            outputFile.delete()
            mylog("MediaCompressor: video compression failed, using original. uri=${item.uri}, error=${error.localizedMessage}")
            onProgress(1f)

            return buildOriginalResult(
                item = item,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                durationMillis = durationMillis,
                estimatedOutputSize = estimatedOutputSize,
                targetVideoBitrate = targetVideoBitrate
            )
        }

        val compressedSize = outputFile.length()

        if (
            config.fallbackToOriginalIfBigger &&
            originalSize != null &&
            compressedSize >= originalSize
        ) {
            outputFile.delete()
            onProgress(1f)

            return buildOriginalResult(
                item = item,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                durationMillis = durationMillis,
                estimatedOutputSize = estimatedOutputSize,
                targetVideoBitrate = targetVideoBitrate
            )
        }

        onProgress(1f)

        return CompressedMediaResult(
            originalUri = item.uri,
            outputUri = outputFile.toUri(),
            type = MediaPickerItemType.VIDEO,
            displayName = item.displayName ?: outputFile.name,
            mimeType = "video/mp4",
            originalSize = originalSize,
            outputSize = compressedSize,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            outputWidth = targetSize.width,
            outputHeight = targetSize.height,
            durationMillis = durationMillis,
            estimatedOutputSize = estimatedOutputSize,
            targetVideoBitrate = targetVideoBitrate,
            mode = MediaCompressionMode.COMPRESSED
        )
    }

    private fun buildOriginalResult(
        item: MediaPickerItem,
        originalWidth: Int? = null,
        originalHeight: Int? = null,
        durationMillis: Long? = item.durationMillis,
        estimatedOutputSize: Long? = null,
        targetVideoBitrate: Int? = null
    ): CompressedMediaResult {
        return CompressedMediaResult(
            originalUri = item.uri,
            outputUri = item.uri,
            type = item.type,
            displayName = item.displayName,
            mimeType = item.mimeType,
            originalSize = item.size,
            outputSize = item.size ?: 0L,
            originalWidth = originalWidth ?: item.width,
            originalHeight = originalHeight ?: item.height,
            outputWidth = originalWidth ?: item.width,
            outputHeight = originalHeight ?: item.height,
            durationMillis = durationMillis,
            estimatedOutputSize = estimatedOutputSize,
            targetVideoBitrate = targetVideoBitrate,
            mode = MediaCompressionMode.ORIGINAL
        )
    }

    private fun calculateInSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var inSampleSize = 1

        if (originalHeight > targetHeight || originalWidth > targetWidth) {
            val halfHeight = originalHeight / 2
            val halfWidth = originalWidth / 2

            while (
                halfHeight / inSampleSize >= targetHeight &&
                halfWidth / inSampleSize >= targetWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private data class VideoSize(
        val width: Int,
        val height: Int
    )

    private fun calculateVideoTargetSize(
        originalWidth: Int,
        originalHeight: Int,
        scale: Float,
        minShortSide: Int
    ): VideoSize {
        val safeScale = scale.coerceIn(0.1f, 1f)

        val rawWidth = (originalWidth * safeScale).roundToInt()
        val rawHeight = (originalHeight * safeScale).roundToInt()

        val isLandscape = originalWidth >= originalHeight

        val width: Int
        val height: Int

        if (isLandscape) {
            height = max(rawHeight, minShortSide)
            width = ((height.toFloat() / originalHeight.toFloat()) * originalWidth).roundToInt()
        } else {
            width = max(rawWidth, minShortSide)
            height = ((width.toFloat() / originalWidth.toFloat()) * originalHeight).roundToInt()
        }

        return VideoSize(
            width = makeEven(width),
            height = makeEven(height)
        )
    }

    private fun makeEven(value: Int): Int {
        return if (value % 2 == 0) value else value + 1
    }

    private fun calculateTargetVideoBitrate(
        originalBitrate: Int?,
        targetWidth: Int,
        targetHeight: Int,
        durationMillis: Long?,
        originalSize: Long?,
        videoBitrateFactor: Float,
        minVideoBitrate: Int,
        maxVideoBitrate: Int
    ): Int {
        val pixelCount = targetWidth * targetHeight

        val defaultByResolution = when {
            pixelCount <= 426 * 240 -> 280_000
            pixelCount <= 640 * 360 -> 450_000
            pixelCount <= 854 * 480 -> 750_000
            pixelCount <= 1280 * 720 -> 1_200_000
            pixelCount <= 1920 * 1080 -> 2_000_000
            else -> 2_800_000
        }

        val metadataBased = originalBitrate
            ?.takeIf { it > 0 }
            ?.let { (it * videoBitrateFactor).roundToInt() }

        val sizeDurationBased = if (
            originalSize != null &&
            originalSize > 0L &&
            durationMillis != null &&
            durationMillis > 0L
        ) {
            val originalTotalBitrate = ((originalSize * 8_000L) / durationMillis).toInt()
            (originalTotalBitrate * videoBitrateFactor).roundToInt()
        } else {
            null
        }

        return listOfNotNull(
            defaultByResolution,
            metadataBased,
            sizeDurationBased
        )
            .minOrNull()
            ?.coerceIn(minVideoBitrate, maxVideoBitrate)
            ?: defaultByResolution.coerceIn(minVideoBitrate, maxVideoBitrate)
    }

    fun estimateVideoOutputSizeBytes(
        durationMillis: Long?,
        videoBitrate: Int,
        audioBitrate: Int
    ): Long? {
        if (durationMillis == null || durationMillis <= 0L) return null

        val durationSeconds = durationMillis / 1000.0
        val totalBitrate = videoBitrate + audioBitrate

        return ((durationSeconds * totalBitrate) / 8.0).toLong()
    }

    private data class VideoMetadata(
        val rawWidth: Int,
        val rawHeight: Int,
        val rotation: Int,
        val durationMillis: Long?,
        val bitrate: Int?, // usually total bitrate
        val audioBitrate: Int?
    ) {
        val displayWidth: Int
            get() = if (rotation == 90 || rotation == 270) rawHeight else rawWidth

        val displayHeight: Int
            get() = if (rotation == 90 || rotation == 270) rawWidth else rawHeight
    }

    private fun readVideoMetadata(uri: Uri): VideoMetadata {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, uri)

            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: 0

            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: 0

            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0

            val duration = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()

            val bitrate = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()

            val audioBitrate = readAudioBitrate(uri)

            VideoMetadata(
                rawWidth = width,
                rawHeight = height,
                rotation = rotation,
                durationMillis = duration,
                bitrate = bitrate,
                audioBitrate = audioBitrate
            )
        } finally {
            retriever.release()
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun transcodeVideoWithMedia3(
        inputUri: Uri,
        outputFile: File,
        targetWidth: Int,
        targetHeight: Int,
        targetVideoBitrate: Int,
        onProgress: (Float) -> Unit
    ) = suspendCancellableCoroutine<Unit> { continuation ->
        val mainHandler = Handler(Looper.getMainLooper())
        var transformer: Transformer? = null

        val mediaItem = MediaItem.fromUri(inputUri)

        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(
                Effects(
                    emptyList(),
                    listOf(
                        Presentation.createForWidthAndHeight(
                            targetWidth,
                            targetHeight,
                            Presentation.LAYOUT_SCALE_TO_FIT
                        )
                    )
                )
            )
            .build()

        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder()
                    .setBitrate(targetVideoBitrate)
                    .build()
            )
            .build()

        val progressHolder = ProgressHolder()

        val progressRunnable = object : Runnable {
            override fun run() {
                if (!continuation.isActive) return

                runCatching {
                    transformer?.getProgress(progressHolder)

                    val progress = progressHolder.progress / 100f

                    onProgress(progress.coerceIn(0f, 0.99f))
                }

                mainHandler.postDelayed(this, 300L)
            }
        }

        continuation.invokeOnCancellation {
            mainHandler.post {
                mainHandler.removeCallbacks(progressRunnable)

                runCatching {
                    transformer?.cancel()
                }
            }

            runCatching {
                outputFile.delete()
            }
        }

        mainHandler.post {
            if (!continuation.isActive) return@post

            transformer = Transformer.Builder(context)
                .setEncoderFactory(encoderFactory)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(
                    object : Transformer.Listener {
                        override fun onCompleted(
                            composition: androidx.media3.transformer.Composition,
                            exportResult: ExportResult
                        ) {
                            mainHandler.removeCallbacks(progressRunnable)
                            if (continuation.isActive) {
                                onProgress(1f)
                                continuation.resume(Unit)
                            }
                        }

                        override fun onError(
                            composition: androidx.media3.transformer.Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            mainHandler.removeCallbacks(progressRunnable)
                            if (continuation.isActive) {
                                continuation.resumeWithException(exportException)
                            }
                        }
                    }
                )
                .build()

            transformer?.start(editedMediaItem, outputFile.absolutePath)
            mainHandler.post(progressRunnable)
        }
    }

    private fun createTempOutputFile(
        prefix: String,
        extension: String
    ): File {
        val dir = File(context.cacheDir, "media_compression")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return File(
            dir,
            prefix + System.currentTimeMillis() + "_" + randomSuffix() + extension
        )
    }

    private fun randomSuffix(): String {
        return (1000..9999).random().toString()
    }

    // ------------------------------- GUESS Size

    suspend fun estimatePickerResult(
        result: MediaPickerResult,
        preset: MediaCompressionPreset = MediaCompressionPreset.BALANCED
    ): List<MediaCompressionEstimate> {
        return estimatePickerResult(
            result = result,
            config = preset.toConfig()
        )
    }

    suspend fun estimatePickerResult(
        result: MediaPickerResult,
        config: MediaCompressionConfig
    ): List<MediaCompressionEstimate> = withContext(Dispatchers.IO) {
        result.items.map { item ->
            when (item.type) {
                MediaPickerItemType.PHOTO -> estimateImage(
                    item = item,
                    config = config
                )

                MediaPickerItemType.VIDEO -> estimateVideo(
                    item = item,
                    config = config
                )

                MediaPickerItemType.FILE -> MediaCompressionEstimate(
                    originalUri = item.uri,
                    type = item.type,
                    originalSize = item.size,
                    estimatedOutputSize = item.size,
                    willUseOriginal = true
                )
            }
        }
    }

    private fun estimateImage(
        item: MediaPickerItem,
        config: MediaCompressionConfig
    ): MediaCompressionEstimate {
        val resolver = context.contentResolver

        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(item.uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        }

        val originalWidth = boundsOptions.outWidth
        val originalHeight = boundsOptions.outHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            return MediaCompressionEstimate(
                originalUri = item.uri,
                type = item.type,
                originalSize = item.size,
                estimatedOutputSize = item.size,
                willUseOriginal = true
            )
        }

        val safeScale = config.scale.coerceIn(0.1f, 1f)

        val targetWidth = max(1, (originalWidth * safeScale).roundToInt())
        val targetHeight = max(1, (originalHeight * safeScale).roundToInt())

        val estimatedSize = estimateImageOutputSizeBytes(
            originalSize = item.size,
            scale = safeScale,
            quality = config.imageQuality
        )

        val willUseOriginal = item.size != null &&
                estimatedSize != null &&
                estimatedSize >= item.size

        return MediaCompressionEstimate(
            originalUri = item.uri,
            type = item.type,
            originalSize = item.size,
            estimatedOutputSize = if (willUseOriginal) item.size else estimatedSize,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            estimatedWidth = if (willUseOriginal) originalWidth else targetWidth,
            estimatedHeight = if (willUseOriginal) originalHeight else targetHeight,
            durationMillis = null,
            targetVideoBitrate = null,
            willUseOriginal = willUseOriginal
        )
    }

    private fun estimateVideo(
        item: MediaPickerItem,
        config: MediaCompressionConfig
    ): MediaCompressionEstimate {
        val metadata = readVideoMetadata(item.uri)

        val originalWidth = metadata.displayWidth
        val originalHeight = metadata.displayHeight

        if (originalWidth <= 0 || originalHeight <= 0) {
            return MediaCompressionEstimate(
                originalUri = item.uri,
                type = item.type,
                originalSize = item.size,
                estimatedOutputSize = item.size,
                durationMillis = item.durationMillis ?: metadata.durationMillis,
                willUseOriginal = true
            )
        }

        val targetSize = calculateVideoTargetSize(
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            scale = config.scale,
            minShortSide = config.minVideoShortSide
        )

        val durationMillis = item.durationMillis ?: metadata.durationMillis

        val targetVideoBitrate = calculateTargetVideoBitrate(
            originalBitrate = metadata.bitrate,
            targetWidth = targetSize.width,
            targetHeight = targetSize.height,
            durationMillis = durationMillis,
            originalSize = item.size,
            videoBitrateFactor = config.videoBitrateFactor,
            minVideoBitrate = config.minVideoBitrate,
            maxVideoBitrate = config.maxVideoBitrate
        )

        val rawEstimatedOutputSize = estimateVideoOutputSizeBytes(
            durationMillis = durationMillis,
            videoBitrate = targetVideoBitrate,
            audioBitrate = metadata.audioBitrate ?: config.estimatedAudioBitrate
        )

        val estimatedOutputSize = rawEstimatedOutputSize
            ?.let { (it * 3.5f).toLong() }

        val willUseOriginal =
            config.skipIfEstimatedBiggerThanOriginal &&
                    item.size != null &&
                    estimatedOutputSize != null &&
                    estimatedOutputSize >= item.size

        return MediaCompressionEstimate(
            originalUri = item.uri,
            type = item.type,
            originalSize = item.size,
            estimatedOutputSize = if (willUseOriginal) item.size else estimatedOutputSize,
            originalWidth = originalWidth,
            originalHeight = originalHeight,
            estimatedWidth = if (willUseOriginal) originalWidth else targetSize.width,
            estimatedHeight = if (willUseOriginal) originalHeight else targetSize.height,
            durationMillis = durationMillis,
            targetVideoBitrate = targetVideoBitrate,
            willUseOriginal = willUseOriginal
        )
    }

    private fun estimateImageOutputSizeBytes(
        originalSize: Long?,
        scale: Float,
        quality: Int
    ): Long? {
        if (originalSize == null || originalSize <= 0L) return null

        val pixelFactor = scale * scale

        val qualityFactor = when {
            quality >= 90 -> 0.90f
            quality >= 80 -> 0.70f
            quality >= 70 -> 0.55f
            quality >= 60 -> 0.42f
            else -> 0.35f
        }

        return (originalSize * pixelFactor * qualityFactor)
            .roundToInt()
            .toLong()
            .coerceAtLeast(20_000L)
    }

    private fun readAudioBitrate(uri: Uri): Int? {
        val extractor = MediaExtractor()

        return try {
            val fd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return null

            fd.use {
                extractor.setDataSource(it.fileDescriptor)

                for (index in 0 until extractor.trackCount) {
                    val format = extractor.getTrackFormat(index)
                    val mime = format.getString(MediaFormat.KEY_MIME) ?: continue

                    if (mime.startsWith("audio/")) {
                        if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                            val bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
                            if (bitrate > 0) return bitrate
                        }
                    }
                }
            }

            null
        } catch (_: Throwable) {
            null
        } finally {
            runCatching {
                extractor.release()
            }
        }
    }

    suspend fun decidePresetForPickerResult(
        result: MediaPickerResult,
        preferred: MediaCompressionPreset = MediaCompressionPreset.LOW
    ): MediaCompressionPreset = withContext(Dispatchers.IO) {
        val items = result.items

        if (items.isEmpty()) return@withContext preferred

        val decidedPresets = items.map { item ->
            when (item.type) {
                MediaPickerItemType.PHOTO -> decideImagePreset(
                    item = item,
                    preferred = preferred
                )

                MediaPickerItemType.VIDEO -> decideVideoPreset(
                    item = item,
                    preferred = preferred
                )

                MediaPickerItemType.FILE -> preferred
            }
        }

        // Pick the least aggressive among selected items.
        // This avoids ruining quality when one video is already tiny.
        decidedPresets.maxBy { it.qualityRank() }
    }

    private fun MediaCompressionPreset.qualityRank(): Int {
        return when (this) {
            MediaCompressionPreset.VERY_LOW -> 1
            MediaCompressionPreset.LOW -> 2
            MediaCompressionPreset.BALANCED -> 3
            MediaCompressionPreset.HIGH -> 4
            MediaCompressionPreset.VERY_HIGH -> 5
        }
    }

    private fun decideImagePreset(
        item: MediaPickerItem,
        preferred: MediaCompressionPreset
    ): MediaCompressionPreset {
        val size = item.size ?: return preferred

        return when {
            // Already tiny image, do not destroy quality.
            size <= 350_000L -> MediaCompressionPreset.BALANCED

            // Small image.
            size <= 1_000_000L -> maxQualityPreset(
                preferred,
                MediaCompressionPreset.LOW
            )

            // Normal image.
            size <= 5_000_000L -> preferred

            // Large image can use preferred aggressive setting.
            else -> preferred
        }
    }

    private fun maxQualityPreset(
        a: MediaCompressionPreset,
        b: MediaCompressionPreset
    ): MediaCompressionPreset {
        return if (a.qualityRank() >= b.qualityRank()) a else b
    }

    private fun decideVideoPreset(
        item: MediaPickerItem,
        preferred: MediaCompressionPreset
    ): MediaCompressionPreset {
        val size = item.size ?: return preferred
        val metadata = readVideoMetadata(item.uri)
        val durationMillis = item.durationMillis ?: metadata.durationMillis

        if (durationMillis == null || durationMillis <= 0L) {
            return preferred
        }

        val durationSeconds = durationMillis / 1000.0
        val sizeMb = size / 1024.0 / 1024.0
        val totalBitrate = ((size * 8_000L) / durationMillis).toInt()

        return when {
            // Very small for its duration.
            // Example: 3min 8MB ≈ 355kbps. Already compressed.
            totalBitrate <= 450_000 && durationSeconds >= 60 -> {
                MediaCompressionPreset.BALANCED
            }

            // Short but already small.
            sizeMb <= 5.0 && durationSeconds <= 30 -> {
                maxQualityPreset(preferred, MediaCompressionPreset.BALANCED)
            }

            // Long and small-ish.
            sizeMb <= 12.0 && durationSeconds >= 90 -> {
                MediaCompressionPreset.BALANCED
            }

            // Big video, aggressive preset is useful.
            sizeMb >= 50.0 -> {
                preferred
            }

            // Medium normal case.
            else -> {
                preferred
            }
        }
    }

    // -------------------- Helpers ------------------------

    fun saveImageToGallery(
        context: Context,
        result: CompressedMediaResult
    ): Uri {
        val fileName = buildCompressedFileName(
            displayName = result.displayName,
            fallbackExtension = ".jpg"
        )

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, result.mimeType ?: "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ComposeBoilerplateCompressTest")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val savedUri = resolver.insert(collection, values)
            ?: error("Cannot create image in MediaStore.")

        resolver.openOutputStream(savedUri)?.use { output ->
            resolver.openInputStream(result.outputUri)?.use { input ->
                input.copyTo(output)
            } ?: error("Cannot open compressed image.")
        } ?: error("Cannot open output stream.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(savedUri, values, null, null)
        }

        return savedUri
    }

    fun saveVideoToMovies(
        context: Context,
        result: CompressedMediaResult
    ): Uri {
        val fileName = buildCompressedFileName(
            displayName = result.displayName,
            fallbackExtension = ".mp4"
        )

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, result.mimeType ?: "video/mp4")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ComposeBoilerplateCompressTest")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val savedUri = resolver.insert(collection, values)
            ?: error("Cannot create video in MediaStore.")

        resolver.openOutputStream(savedUri)?.use { output ->
            resolver.openInputStream(result.outputUri)?.use { input ->
                input.copyTo(output)
            } ?: error("Cannot open compressed video.")
        } ?: error("Cannot open output stream.")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(savedUri, values, null, null)
        }

        return savedUri
    }

    private fun buildCompressedFileName(
        displayName: String?,
        fallbackExtension: String
    ): String {
        val cleanName = displayName
            ?.substringBeforeLast(".")
            ?.takeIf { it.isNotBlank() }
            ?: "room_chat_media"

        return "${cleanName}_compressed_${System.currentTimeMillis()}$fallbackExtension"
    }
    // -------------------- Helpers ------------------------
}
