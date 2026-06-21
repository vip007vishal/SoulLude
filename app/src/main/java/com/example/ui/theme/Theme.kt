package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    tertiary = Purple80,
    background = PremiumBackground,
    surface = SurfaceColor,
    onPrimary = OnTextPrimary,
    onSecondary = OnTextPrimary,
    onTertiary = OnTextPrimary,
    onBackground = OnTextPrimary,
    onSurface = OnTextPrimary,
    surfaceVariant = GlassWhite,
    onSurfaceVariant = OnTextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, 
    customPrimaryColor: Color? = null,
    customBackgroundColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (customPrimaryColor != null) {
        darkColorScheme(
            primary = customPrimaryColor,
            secondary = SecondaryColor,
            tertiary = Purple80,
            background = customBackgroundColor ?: PremiumBackground,
            surface = SurfaceColor,
            onPrimary = OnTextPrimary,
            onSecondary = OnTextPrimary,
            onTertiary = OnTextPrimary,
            onBackground = OnTextPrimary,
            onSurface = OnTextPrimary,
            surfaceVariant = GlassWhite,
            onSurfaceVariant = OnTextSecondary
        )
    } else {
        darkColorScheme(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            tertiary = Purple80,
            background = customBackgroundColor ?: PremiumBackground,
            surface = SurfaceColor,
            onPrimary = OnTextPrimary,
            onSecondary = OnTextPrimary,
            onTertiary = OnTextPrimary,
            onBackground = OnTextPrimary,
            onSurface = OnTextPrimary,
            surfaceVariant = GlassWhite,
            onSurfaceVariant = OnTextSecondary
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
