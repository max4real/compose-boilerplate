package com.max4real.compose_boilerplate.shared.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberSaveGraphicsLayerToGalleryAction(
    username: String?,
    graphicsLayer: GraphicsLayer,
): (onDone: () -> Unit) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingSave by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun saveNow(onDone: () -> Unit) {
        scope.launch {
            val bitmap = graphicsLayer
                .toImageBitmap()
                .asAndroidBitmap()
                .copy(Bitmap.Config.ARGB_8888, false)

            val isSaved = saveBitmapToGallery(
                context = context,
                bitmap = bitmap,
                fileName = "qr_code_${safeFilePart(username.orEmpty())}_${System.currentTimeMillis()}.png"
            )

            showToast(
                context = context,
                message = if (isSaved) {
                    "Profile QR image saved to gallery"
                } else {
                    "Unable to save profile QR image"
                }
            )

            onDone()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val action = pendingSave
        pendingSave = null

        if (isGranted && action != null) {
            action()
        } else {
            showToast(
                context = context,
                message = "Storage permission is required"
            )
        }
    }

    return remember(username, graphicsLayer) {
        { onDone ->
            val needsLegacyStoragePermission =
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

            val hasPermission =
                !needsLegacyStoragePermission ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                saveNow(onDone)
            } else {
                pendingSave = {
                    saveNow(onDone)
                }
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}

private fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
): Boolean {
    return runCatching {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/ComposeBoilerplate"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )

                val appMediaDir = File(picturesDir, "ComposeBoilerplate").apply {
                    mkdirs()
                }

                put(
                    MediaStore.Images.Media.DATA,
                    File(appMediaDir, fileName).absolutePath
                )
            }
        }

        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: return@runCatching false

        resolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                outputStream
            )
        } ?: return@runCatching false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        true
    }.getOrDefault(false)
}

private fun safeFilePart(value: String): String {
    return value
        .lowercase()
        .replace(Regex("[^a-z0-9_-]+"), "_")
        .trim('_')
        .ifBlank { "profile" }
}