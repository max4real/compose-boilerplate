package com.max4real.compose_boilerplate.shared.util

import com.max4real.compose_boilerplate.shared.config.AppEnvironment

object MediaUrlResolver {
    fun thumbnailUrlForKey(key: String?): String? {
        val cleanKey = key
            ?.trim()
            ?.trimStart('/')
            ?.takeIf { it.isNotBlank() }
            ?.takeUnless { it.contains(":local:") }
            ?: return null
        val thumbnailKey = cleanKey.substringBeforeLast(
            delimiter = ".",
            missingDelimiterValue = cleanKey
        ) + ".jpg"

        return "${AppEnvironment.MEDIA_BASE_URL.trimEnd('/')}/thumbnails/$thumbnailKey"
    }
}
