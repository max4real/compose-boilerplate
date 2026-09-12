package com.max4real.compose_boilerplate.extensions

fun Int.toReactionCountText(): String {
    return when {
        this >= 1_000_000 -> {
            val value = this / 1_000_000
            "${value}M"
        }

        this >= 1_000 -> {
            val value = this / 1_000
            "${value}K"
        }

        else -> this.toString()
    }
}