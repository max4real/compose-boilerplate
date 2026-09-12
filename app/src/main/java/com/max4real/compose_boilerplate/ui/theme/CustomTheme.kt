package com.max4real.compose_boilerplate.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * App-level design tokens, on top of Material3's [androidx.compose.material3.ColorScheme].
 * Add tokens here as new screens need them — this starter set only covers what the
 * bundled shared widgets use.
 */
data class AppColors(
    val background: Color,
    val background2: Color,
    val surface: Color,
    val surfaceSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val brand: Color,
    val icon: Color,
    val inputBackground: Color,
    val mainText: Color,
    val opposite: Color,

    // App bar
    val appBarBackground: Color,
    val appBarTitle: Color,
    val appBarIcon: Color,
    val appBarIconButton: Color,
    val iconButtonShadow: Color,
    val iconButtonShadow2: Color,
    val appBarSearchHintText: Color,

    // Gradients (TopGradient/BotGradient fade into this)
    val gradientBase: Color,

    // Bottom sheets
    val bottomSheetInputContainer: Color,
    val bottomSheetInputDivider: Color,
    val bottomSheetInputText: Color,
    val bottomSheetInputIcon: Color,

    // Misc
    val rippleColor: Color,
    val rowChevron: Color,
    val cardRowIconsColor: Color,
    val imageShimmerBase: Color,
    val imageShimmerHighlight: Color,
    val chipSelectedBackground: Color,
)

val LocalAppDarkMode = compositionLocalOf { false }

val theme: AppColors
    @Composable get() = AppTheme.colors(LocalAppDarkMode.current)

object AppTheme {
    private val lightColors = AppColors(
        background = CustomColor.White,
        background2 = Color(0xFFF6F6F6),
        surface = Color(0xFFF7F7F7),
        surfaceSoft = Color(0xFFEFEFEF),
        textPrimary = CustomColor.BrandColor,
        textSecondary = CustomColor.TextGray,
        border = Color(0xFFE0E0E0),
        brand = CustomColor.BrandColor,
        icon = CustomColor.BrandColor,
        inputBackground = Color(0xFFF7F7F7),
        mainText = Color.Black,
        opposite = Color.Black,

        appBarBackground = Color.White,
        appBarTitle = Color(0xFF111827),
        appBarIcon = Color(0xFF292D32),
        appBarIconButton = Color.White,
        iconButtonShadow = Color.Black.copy(alpha = 0.10f),
        iconButtonShadow2 = Color.Black.copy(alpha = 0.10f),
        appBarSearchHintText = Color.Black.copy(alpha = 0.6f),

        gradientBase = Color.White,

        bottomSheetInputContainer = Color.White,
        bottomSheetInputDivider = Color(0xFFD9D9D9),
        bottomSheetInputText = Color.Black.copy(alpha = 0.6f),
        bottomSheetInputIcon = Color(0XFF292D32),

        rippleColor = Color.Black.copy(alpha = 0.08f),
        rowChevron = Color(0XFF878787).copy(alpha = 0.5f),
        cardRowIconsColor = Color(0XFF818181),
        imageShimmerBase = Color(0xFFE4E7EB),
        imageShimmerHighlight = Color(0xFFF7F8FA),
        chipSelectedBackground = Color(0xFFF4F4F4),
    )

    private val darkColors = AppColors(
        background = Color.Black,
        background2 = Color.Black,
        surface = Color(0xFF171C22),
        surfaceSoft = Color(0xFF232A32),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF9CA3AF),
        border = Color(0xFF2D3640),
        brand = Color(0xFFFFFFFF),
        icon = Color(0xFFE5E7EB),
        inputBackground = Color(0xFF171C22),
        mainText = Color.White,
        opposite = Color.White,

        appBarBackground = Color.Black,
        appBarTitle = Color(0xFFF8FAFC),
        appBarIcon = Color(0xFFE5E7EB),
        appBarIconButton = Color(0xFF171C22),
        iconButtonShadow = Color.Black.copy(alpha = 0.35f),
        iconButtonShadow2 = Color.Black.copy(alpha = 0.25f),
        appBarSearchHintText = Color.White.copy(alpha = 0.55f),

        gradientBase = Color.Black,

        bottomSheetInputContainer = Color(0xFF171C22),
        bottomSheetInputDivider = Color(0xFF2D3640),
        bottomSheetInputText = Color.White.copy(alpha = 0.6f),
        bottomSheetInputIcon = Color(0xFF9E9E9E),

        rippleColor = Color.White.copy(alpha = 0.10f),
        rowChevron = Color.White.copy(alpha = 0.5f),
        cardRowIconsColor = Color(0XFF989898),
        imageShimmerBase = Color(0xFF1D232B),
        imageShimmerHighlight = Color(0xFF343D48),
        chipSelectedBackground = Color(0xFF232A32),
    )

    fun colors(isDarkMode: Boolean): AppColors {
        return if (isDarkMode) darkColors else lightColors
    }
}
