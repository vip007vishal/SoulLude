package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// HarmoniX Premium Colors
val PrimaryColor = Color(0xFFD0BCFF) 
val SecondaryColor = Color(0xFFE8DEF8) 
val OnPrimaryColor = Color(0xFF381E72)
val PremiumBackground = Color(0xFF0F0F12) 
val SurfaceColor = Color(0xFF1C1B1F) 
val OnTextPrimary = Color(0xFFFFFFFF)
val OnTextSecondary = Color(0xFFCCC4D1)
val GlassWhite = Color(0x0DFFFFFF)
val GradientTop = Color(0xFF1C1B1F)
val GradientBottom = Color(0xFF0F0F12)

fun getThemeGradientColors(themeMode: String): List<Color> {
    return when (themeMode) {
        "AMOLED Black" -> listOf(Color.Black, Color.Black)
        "Warm Orange" -> listOf(Color(0xFF2E1900), Color(0xFF150A00))
        "Forest Green" -> listOf(Color(0xFF0D1F11), Color(0xFF050F08))
        "Deep Blue" -> listOf(Color(0xFF0F1E36), Color(0xFF070F1E))
        "Light" -> listOf(Color(0xFFF5F5F7), Color(0xFFE5E5EA))
        else -> listOf(Color(0xFF0D0D0D), Color(0xFF5F4B8B), Color(0xFFCBBBF6)) // Elegant Purple Gradient
    }
}

fun getThemeSingleColor(themeMode: String): Color {
    return when (themeMode) {
        "AMOLED Black" -> Color.Black
        "Warm Orange" -> Color(0xFF150A00)
        "Forest Green" -> Color(0xFF050F08)
        "Deep Blue" -> Color(0xFF070F1E)
        "Light" -> Color(0xFFE5E5EA)
        else -> Color(0xFF0D0D0D)
    }
}


