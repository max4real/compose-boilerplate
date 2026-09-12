package com.max4real.compose_boilerplate.extensions

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.max4real.compose_boilerplate.R
import com.max4real.compose_boilerplate.shared.widget.mediapicker.MediaPickerItem

@DrawableRes
fun String?.fileIconRes(): Int {
    return fileIconResFor(extension = fileExtension(), mimeType = null)
}

@DrawableRes
fun MediaPickerItem.fileIconRes(): Int {
    return fileIconResFor(
        extension = displayName.fileExtension(),
        mimeType = mimeType
    )
}

@Composable
fun MediaPickerItem.fileIconPainter(): Painter {
    return painterResource(id = fileIconRes())
}

@DrawableRes
private fun fileIconResFor(
    extension: String,
    mimeType: String?
): Int {
    return when {
        mimeType?.startsWith("image/") == true ||
                extension in imageExtensions ->
            R.drawable.jpg

        mimeType == "application/pdf" ||
                extension == "pdf" ->
            R.drawable.pdf

        mimeType in wordMimeTypes ||
                extension in wordExtensions ->
            R.drawable.doc

        mimeType in spreadsheetMimeTypes ||
                extension in spreadsheetExtensions ||
                extension == "xml" ->
            R.drawable.xls

        extension in presentationExtensions ->
            R.drawable.ppt

        extension in textExtensions ->
            R.drawable.txt

        extension in archiveExtensions ->
            R.drawable.zip

        extension in appPackageExtensions ->
            R.drawable.apk

        mimeType?.startsWith("audio/") == true ||
                extension in audioExtensions ->
            R.drawable.mp3

        mimeType?.startsWith("video/") == true ||
                extension in videoExtensions ->
            R.drawable.mp4

        extension in fontExtensions ->
            R.drawable.ttf

        extension in designExtensions ->
            R.drawable.svg

        extension in codeExtensions ->
            R.drawable.code

        else ->
            R.drawable.document_text
    }
}

private fun String?.fileExtension(): String {
    return this
        ?.substringBeforeLast("?")
        ?.substringBeforeLast("#")
        ?.substringAfterLast(".", missingDelimiterValue = "")
        ?.lowercase()
        ?.trim()
        .orEmpty()
}

private val imageExtensions = setOf(
    "jpg",
    "jpeg",
    "png",
    "webp",
    "gif",
    "bmp",
    "heic",
    "heif",
    "tiff",
    "tif"
)

private val wordExtensions = setOf("doc", "docx", "rtf", "odt")
private val spreadsheetExtensions = setOf("xls", "xlsx", "csv", "xms", "ods", "tsv", "numbers")
private val presentationExtensions = setOf("ppt", "pptx", "pps", "ppsx", "odp", "key")
private val textExtensions = setOf("txt", "md", "log", "json", "yaml", "yml")
private val codeExtensions = setOf(
    "kt",
    "java",
    "swift",
    "js",
    "ts",
    "html",
    "css",
    "py",
    "go",
    "rs",
    "cpp",
    "c",
    "h",
    "php",
    "rb",
    "dart",
    "sql",
    "sh"
)
private val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
private val audioExtensions = setOf("mp3", "wav", "m4a", "aac", "ogg", "flac", "amr")
private val videoExtensions = setOf("mp4", "mov", "mkv", "avi", "webm", "3gp", "m4v")
private val fontExtensions = setOf("ttf", "otf", "woff", "woff2")
private val appPackageExtensions = setOf("apk", "aab", "ipa", "exe", "dmg", "pkg", "deb", "rpm")
private val designExtensions = setOf("svg", "ai", "psd", "fig", "sketch")

private val wordMimeTypes = setOf(
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/rtf"
)

private val spreadsheetMimeTypes = setOf(
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "text/csv",
    "application/xml",
    "text/xml"
)
