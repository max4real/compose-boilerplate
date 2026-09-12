package com.max4real.compose_boilerplate.shared.util

val MentionRegex =
    Regex("@([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})")

/** Reserved sentinel "user id" standing in for every current member of a group conversation
 * — see docs/mentions-everyone-backend.md. UUID-shaped on purpose so it matches [MentionRegex]
 * and needs no changes to the token format, only a resolution/permission special case. */
const val EveryoneMentionUserId = "00000000-0000-0000-0000-000000000000"

/** Loose phone-number matcher for chat text highlighting. Intentionally NOT
 * android.util.Patterns.PHONE (too permissive — matches almost any digit run,
 * including plain years/counts). Requires 7–15 total digits (E.164-ish upper
 * bound), optionally grouped with spaces/dots/dashes and an optional leading
 * '+' or parenthesized area/country code, e.g. "+95 9 1234 5678", "(09) 123-456-789",
 * "09123456789". Excludes matches touching a word character, '.', or '@'
 * immediately before/after, so it doesn't fire inside emails, decimals,
 * version numbers, or ids glued to letters. */
val PhoneNumberRegex = Regex(
    "(?<![\\w.@])" +
        "\\+?" +
        "(?:\\(\\d{1,4}\\)[\\s.-]?)?" +
        "(?:\\d[\\s.-]?){6,14}\\d" +
        "(?!\\w)"
)

/** Plain-text (non-Compose) mention resolution, for surfaces that don't build an
 * AnnotatedString — e.g. a search snippet feeding into another highlighter, or a
 * notification body. Same [liveNames]/[snapshotNames] fallback order as the Compose
 * `withMentionsResolved` in bubble_share/ChatBubbleText.kt. */
fun String.resolveMentionsToPlainText(
    liveNames: Map<String, String>,
    snapshotNames: Map<String, String>
): String {
    if (!contains('@')) return this

    return MentionRegex.replace(this) { match ->
        val userId = match.groupValues[1]
        val name = if (userId == EveryoneMentionUserId) {
            "everyone"
        } else {
            liveNames[userId] ?: snapshotNames[userId] ?: "unknown"
        }
        "@$name"
    }
}
