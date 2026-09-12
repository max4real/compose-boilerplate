package com.max4real.compose_boilerplate.extensions

import java.text.BreakIterator

fun String.isSingleEmojiOnly(): Boolean {
    val text = trim()
    if (text.isEmpty()) return false

    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(text)

    var clusterCount = 0
    iterator.first()
    while (iterator.next() != BreakIterator.DONE) {
        if (++clusterCount > 1) return false
    }

    if (clusterCount != 1) return false

    return text.codePoints().anyMatch(::isEmojiCodePoint)
}

private fun isEmojiCodePoint(cp: Int): Boolean =
    cp in 0x1F600..0x1F64F ||
    cp in 0x1F300..0x1F5FF ||
    cp in 0x1F680..0x1F6FF ||
    cp in 0x1F700..0x1FAFF ||
    cp in 0x2600..0x27BF   ||
    cp in 0xFE00..0xFE0F   ||
    cp in 0x1F1E0..0x1F1FF ||
    cp == 0x200D
