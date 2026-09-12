package com.max4real.compose_boilerplate.extensions

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import kotlin.use

internal fun Context.loadPdfThumbnail(uri: android.net.Uri): Bitmap? {
    return runCatching {
        contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                if (renderer.pageCount <= 0) return@use null

                val page = renderer.openPage(0)
                try {
                    val width = 160
                    val height = (width * page.height / page.width).coerceAtLeast(1)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(
                        bitmap,
                        null,
                        null,
                        PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    bitmap
                } finally {
                    page.close()
                }
            }
        }
    }.getOrNull()
}


// this one extract the available phone memory.
internal fun Context.supportsReactionAnimation(): Boolean {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    if (am.isLowRamDevice) return false
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    return memInfo.totalMem >= 3L * 1024 * 1024 * 1024
}
