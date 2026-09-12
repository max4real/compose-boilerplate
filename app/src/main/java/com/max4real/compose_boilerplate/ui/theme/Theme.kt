package com.max4real.compose_boilerplate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CustomColor.White,
    onPrimary = CustomColor.Black,

    background = CustomColor.Black,
    onBackground = CustomColor.White,

    surface = CustomColor.DarkGray,
    onSurface = CustomColor.White,

    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = CustomColor.White80,

    outline = CustomColor.White20,

    secondary = CustomColor.TextGray,
    onSecondary = CustomColor.Black
)

private val LightColorScheme = lightColorScheme(
    primary = CustomColor.BrandColor,
    onPrimary = CustomColor.White,

    background = CustomColor.White,
    onBackground = CustomColor.BrandColor,

    surface = Color(0xFFF7F7F7),
    onSurface = CustomColor.BrandColor,

    surfaceVariant = Color(0xFFEFEFEF),
    onSurfaceVariant = CustomColor.TextGray,

    outline = Color(0xFFE0E0E0),

    secondary = CustomColor.TextGray,
    onSecondary = CustomColor.White
)

@Composable
fun ComposeboilerplateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // important for custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}