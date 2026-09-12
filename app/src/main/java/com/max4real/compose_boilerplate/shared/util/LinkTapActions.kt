package com.max4real.compose_boilerplate.shared.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens [url] in the user's browser. Bare domains matched without a scheme
 * (e.g. "example.com") are prefixed with "https://" so [Intent.ACTION_VIEW]
 * can resolve them. */
fun openUrlInBrowser(context: Context, url: String) {
    val target = if (url.contains("://")) url else "https://$url"
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(target))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        showToast(context, "No app found to open this link.")
    }
}

/** Opens the dialer pre-filled with [phoneNumber] (ACTION_DIAL, not ACTION_CALL —
 * the user still has to press call themselves). */
fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        showToast(context, "No dialer app found.")
    }
}

/** Opens the user's email app with a new message addressed to [email],
 * optionally pre-filled with [subject] and [body]. Subject/body are encoded as
 * mailto query params rather than Intent extras — Gmail's ACTION_SENDTO handler
 * ignores EXTRA_SUBJECT/EXTRA_TEXT and only reads them from the URI. */
fun composeEmail(context: Context, email: String, subject: String? = null, body: String? = null) {
    val query = buildList {
        subject?.let { add("subject=${Uri.encode(it)}") }
        body?.let { add("body=${Uri.encode(it)}") }
    }.joinToString("&")
    val mailto = "mailto:${Uri.encode(email)}" + if (query.isNotEmpty()) "?$query" else ""
    try {
        context.startActivity(
            Intent(Intent.ACTION_SENDTO, Uri.parse(mailto))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        showToast(context, "No email app found.")
    }
}
