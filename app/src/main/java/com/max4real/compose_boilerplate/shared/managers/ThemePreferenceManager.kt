package com.max4real.compose_boilerplate.shared.managers

import android.content.Context
import androidx.core.content.edit
import com.max4real.compose_boilerplate.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemePreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        return ThemeMode.fromName(prefs.getString(KEY_THEME_MODE, null))
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        prefs.edit {
            putString(KEY_THEME_MODE, themeMode.name)
        }
    }

    companion object {
        private const val PREF_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
