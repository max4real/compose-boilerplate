package com.max4real.compose_boilerplate.ui.theme

import androidx.compose.ui.graphics.Color

object CustomColor {
    // Base Colors
    val Black = Color.Black
    val White = Color.White
    val Transparent = Color.Transparent
    val Gray = Color.Gray
    val LightGray = Color.LightGray

    // Custom Hex Colors
    val TextGray = Color(0xFFA1A1A1)
    val BrandColor = Color(0xFF292929)
    val DarkGray = Color(0xFF2C2C2C)

    // Alpha Variations
    val White80 = White.copy(alpha = 0.8f)
    val White50 = White.copy(alpha = 0.5f)
    val White30 = White.copy(alpha = 0.3f)
    val White20 = White.copy(alpha = 0.2f)
    val Gray40 = Gray.copy(alpha = 0.4f)

    // Accent
    val accentBlue = Color(0XFF276BFF)
}
