package com.max4real.compose_boilerplate.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun String.highlightMatches(query: String, highlightColor: Color): AnnotatedString {
    val source = this
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return AnnotatedString(source)

    return buildAnnotatedString {
        var startIndex = 0
        while (startIndex <= source.length) {
            val matchIndex = source.indexOf(normalizedQuery, startIndex, ignoreCase = true)
            if (matchIndex < 0) {
                append(source.substring(startIndex))
                break
            }
            append(source.substring(startIndex, matchIndex))
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.W700)) {
                append(source.substring(matchIndex, matchIndex + normalizedQuery.length))
            }
            startIndex = matchIndex + normalizedQuery.length
        }
    }
}
