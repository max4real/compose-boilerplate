package com.max4real.compose_boilerplate.extensions

val Long.compact: String
    get() {
        fun format(value: Double, suffix: String): String {
            val str = String.format("%.1f", value)
            val clean = str.removeSuffix(".0")
            return if (clean.replace(".", "").length > 3) {
                // Drop decimals if total digits exceed 3
                String.format("%.0f", value).toInt().toString() + suffix
            } else {
                clean + suffix
            }
        }

        return when {
            this >= 1_000_000_000 -> format(this / 1_000_000_000.0, "B")
            this >= 1_000_000 -> format(this / 1_000_000.0, "M")
            this >= 1_000 -> format(this / 1_000.0, "K")
            else -> this.toString()
        }
    }