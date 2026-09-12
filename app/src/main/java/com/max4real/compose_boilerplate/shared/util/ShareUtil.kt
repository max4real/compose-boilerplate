package com.max4real.compose_boilerplate.shared.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.max4real.compose_boilerplate.BuildConfig


fun mylog(message: String) {
//    if (BuildConfig.FLAVOR == "dev") {
    if (BuildConfig.DEBUG) {
        println("maxDebug: $message")
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

/**
 * Hands a URL to the system Sharesheet. Text rather than a media item, so it is a plain
 * `ACTION_SEND` with no provider or grant behind it.
 */
fun shareText(context: Context, text: String, chooserTitle: String? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }.onFailure { showToast(context, "Couldn't open the share sheet") }
}

/**
 * Copies to the clipboard, and says so only where the system does not.
 *
 * Android 13 shows its own copy confirmation, so a toast on top of it is the same message twice.
 */
fun copyToClipboard(context: Context, text: String, label: String = "Link") {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        showToast(context, "$label copied")
    }
}
