package com.max4real.compose_boilerplate.shared.util

// Inline spoiler markup `>!hidden!<`, injected by the backend around OTP codes.
val SpoilerRegex = Regex("""(?<!\\)>!(.+?)(?<!\\)!<""", RegexOption.DOT_MATCHES_ALL)

private const val SpoilerOpen = ">!"

data class ParsedSpoilerText(
    val text: String,
    val ranges: List<IntRange>
)

// Strips the markers and reports what to hide, in offsets into the stripped text.
fun String.parseSpoilerMarkup(): ParsedSpoilerText {
    if (!contains(SpoilerOpen)) return ParsedSpoilerText(this, emptyList())

    val builder = StringBuilder(length)
    val ranges = mutableListOf<IntRange>()
    var cursor = 0

    SpoilerRegex.findAll(this).forEach { match ->
        if (match.range.first < cursor) return@forEach

        builder.appendUnescaped(this, cursor, match.range.first)
        val start = builder.length
        val hidden = match.groupValues[1]
        builder.appendUnescaped(hidden, 0, hidden.length)
        if (builder.length > start) ranges += start until builder.length
        cursor = match.range.last + 1
    }
    builder.appendUnescaped(this, cursor, length)

    return ParsedSpoilerText(builder.toString(), ranges)
}

// Drops the backslash from an escaped `\>!` / `\!<` so it renders as plain text.
private fun StringBuilder.appendUnescaped(source: CharSequence, start: Int, end: Int) {
    var index = start
    while (index < end) {
        val char = source[index]
        if (char == '\\' && isMarkerAt(source, index + 1, end)) {
            index++
            continue
        }
        append(char)
        index++
    }
}

private fun isMarkerAt(source: CharSequence, index: Int, end: Int): Boolean {
    if (index + 1 >= end) return false
    val first = source[index]
    val second = source[index + 1]
    return (first == '>' && second == '!') || (first == '!' && second == '<')
}
