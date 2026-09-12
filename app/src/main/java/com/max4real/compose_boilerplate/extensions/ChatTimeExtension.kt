package com.max4real.compose_boilerplate.extensions

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Long?.toChatTimeText(): String {
    val timestampMs = this ?: return ""
    if (timestampMs <= 0L) return ""

    val todayCalendar = Calendar.getInstance()
    val currentTimeMs = todayCalendar.timeInMillis

    // Calculate the difference in milliseconds
    val diffMs = currentTimeMs - timestampMs

    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = timestampMs
    }

    val yesterdayCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        messageCalendar.isSameDay(todayCalendar) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMs)).uppercase()
        }

        messageCalendar.isSameDay(yesterdayCalendar) -> {
            "Yesterday"
        }

//        messageCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) -> {
//            SimpleDateFormat("d/M", Locale.getDefault()).format(Date(timestampMs))
//        }

        else -> {
            SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date(timestampMs))
        }
    }
}

fun Calendar.isSameDay(other: Calendar): Boolean {
    return get(Calendar.YEAR) == other.get(Calendar.YEAR) && get(Calendar.DAY_OF_YEAR) == other.get(
        Calendar.DAY_OF_YEAR
    )
}


fun Long.toDayKey(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@toDayKey
    }

    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
}

fun Long.toMonthKey(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@toMonthKey
    }

    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}"
}

fun Long.toMonthYearLabel(): String {
    if (this <= 0L) return ""

    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(this))
}

fun Long.toMessageDateLabel(): String {
    if (this <= 0L) return ""

    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = this@toMessageDateLabel
    }
    val todayCalendar = Calendar.getInstance()
    val yesterdayCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val timeText = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))

    return when {
        messageCalendar.isSameDay(todayCalendar) -> "Today" //"Today ${timeText.uppercase()}"
        messageCalendar.isSameDay(yesterdayCalendar) -> "Yesterday" //"Yesterday ${timeText.uppercase()}"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(this))
    }
}


fun Long.toMessageReadStatusLabel(): String {
    if (this <= 0L) return ""

    val messageCalendar = Calendar.getInstance().apply {
        timeInMillis = this@toMessageReadStatusLabel
    }
    val todayCalendar = Calendar.getInstance()
    val yesterdayCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val timeText = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))

    return when {
        messageCalendar.isSameDay(todayCalendar) -> "at today ${timeText.uppercase()}"
        messageCalendar.isSameDay(yesterdayCalendar) -> "at yesterday ${timeText.uppercase()}"
        else -> SimpleDateFormat("'at' dd/MM/yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Long?.toLastSeenText(): String {
    val timestampMs = this ?: return "Offline"
    if (timestampMs <= 0L) return "Offline"

    val lastSeenCalendar = Calendar.getInstance().apply {
        timeInMillis = timestampMs
    }

    val todayCalendar = Calendar.getInstance()

    val yesterdayCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val timeText = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(timestampMs))
        .uppercase()

    return when {
        lastSeenCalendar.isSameDay(todayCalendar) -> {
            "Last seen today at $timeText"
        }

        lastSeenCalendar.isSameDay(yesterdayCalendar) -> {
            "Last seen yesterday at $timeText"
        }

        lastSeenCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) -> {
            val dateText = SimpleDateFormat("MMM d", Locale.getDefault())
                .format(Date(timestampMs))

            "Last seen $dateText at $timeText"
        }

        else -> {
            val dateText = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(Date(timestampMs))

            "Last seen $dateText at $timeText"
        }
    }
}

/** Story header line: minutes and hours while it is fresh, then the day it was posted. */
fun Long.toStoryTimeText(): String {
    if (this <= 0L) return ""

    val nowMs = System.currentTimeMillis()
    val elapsedMinutes = (nowMs - this) / 60_000L
    val postedCalendar = Calendar.getInstance().apply { timeInMillis = this@toStoryTimeText }
    val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val timeText = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))

    return when {
        elapsedMinutes < 1L -> "Just now"
        elapsedMinutes < 60L -> "${elapsedMinutes}m ago"
        postedCalendar.isSameDay(Calendar.getInstance()) -> "${elapsedMinutes / 60L}h ago"
        postedCalendar.isSameDay(yesterdayCalendar) -> "yesterday at $timeText"
        else -> SimpleDateFormat("d MMM 'at' h:mm a", Locale.getDefault()).format(Date(this))
    }
}

fun Long?.toReactionReactedAtLabel(): String {
    val timestampMs = this ?: return ""
    if (timestampMs <= 0L) return ""

    val reactedCalendar = Calendar.getInstance().apply {
        timeInMillis = timestampMs
    }
    val nowCalendar = Calendar.getInstance()
    val datePattern = if (
        reactedCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR)
    ) {
        "MMMM d"
    } else {
        "MMMM d, yyyy"
    }
    val dateText = SimpleDateFormat(datePattern, Locale.getDefault())
        .format(Date(timestampMs))
    val timeText = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(timestampMs))

    return "Reacted at $dateText, $timeText"
}

/** "today at 09:14 am" — the story viewers list, which wants the day spelled out next to the clock. */
fun Long.toStoryViewerTimeText(): String {
    if (this <= 0L) return ""

    val viewedCalendar = Calendar.getInstance().apply { timeInMillis = this@toStoryViewerTimeText }
    val todayCalendar = Calendar.getInstance()
    val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val timeText = SimpleDateFormat("hh:mm a", Locale.getDefault())
        .format(Date(this))
        .lowercase(Locale.getDefault())

    return when {
        viewedCalendar.isSameDay(todayCalendar) -> "today at $timeText"
        viewedCalendar.isSameDay(yesterdayCalendar) -> "yesterday at $timeText"
        else -> {
            val dayText = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))
            "$dayText at $timeText"
        }
    }
}
