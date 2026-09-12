package com.max4real.compose_boilerplate.ui.theme

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    fun isDark(systemDark: Boolean): Boolean {
        return when (this) {
            SYSTEM -> systemDark
            LIGHT -> false
            DARK -> true
        }
    }

    companion object {
        fun fromName(name: String?): ThemeMode {
            return entries.firstOrNull { it.name == name } ?: SYSTEM // return System as Defalut
        }
    }
}
