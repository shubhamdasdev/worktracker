package com.example.worktracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Updated Material 3 Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = md3_dark_primary,
    onPrimary = md3_dark_onPrimary,
    primaryContainer = md3_dark_primaryContainer,
    onPrimaryContainer = md3_dark_onPrimaryContainer,
    secondary = md3_dark_secondary,
    onSecondary = md3_dark_onSecondary,
    secondaryContainer = md3_dark_secondaryContainer,
    onSecondaryContainer = md3_dark_onSecondaryContainer,
    tertiary = md3_dark_tertiary,
    onTertiary = md3_dark_onTertiary,
    tertiaryContainer = md3_dark_tertiaryContainer,
    onTertiaryContainer = md3_dark_onTertiaryContainer,
    error = md3_dark_error,
    errorContainer = md3_dark_errorContainer,
    onError = md3_dark_onError,
    onErrorContainer = md3_dark_onErrorContainer,
    background = md3_dark_background,
    onBackground = md3_dark_onBackground,
    surface = md3_dark_surface,
    onSurface = md3_dark_onSurface,
    surfaceVariant = md3_dark_surfaceVariant,
    onSurfaceVariant = md3_dark_onSurfaceVariant,
    outline = md3_dark_outline
)

// Updated Material 3 Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = md3_light_primary,
    onPrimary = md3_light_onPrimary,
    primaryContainer = md3_light_primaryContainer,
    onPrimaryContainer = md3_light_onPrimaryContainer,
    secondary = md3_light_secondary,
    onSecondary = md3_light_onSecondary,
    secondaryContainer = md3_light_secondaryContainer,
    onSecondaryContainer = md3_light_onSecondaryContainer,
    tertiary = md3_light_tertiary,
    onTertiary = md3_light_onTertiary,
    tertiaryContainer = md3_light_tertiaryContainer,
    onTertiaryContainer = md3_light_onTertiaryContainer,
    error = md3_light_error,
    errorContainer = md3_light_errorContainer,
    onError = md3_light_onError,
    onErrorContainer = md3_light_onErrorContainer,
    background = md3_light_background,
    onBackground = md3_light_onBackground,
    surface = md3_light_surface,
    onSurface = md3_light_onSurface,
    surfaceVariant = md3_light_surfaceVariant,
    onSurfaceVariant = md3_light_onSurfaceVariant,
    outline = md3_light_outline
)

val LocalIsDarkMode = compositionLocalOf { false }

@Composable
fun WorkTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val currentWindow = (view.context as? Activity)?.window
                ?: throw Exception("Not in an activity - unable to get Window reference")
            // Set the status bar color to match the app theme
            currentWindow.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    CompositionLocalProvider(LocalIsDarkMode provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}