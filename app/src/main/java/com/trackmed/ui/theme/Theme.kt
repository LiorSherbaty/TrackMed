package com.trackmed.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.trackmed.data.local.entity.EThemeMode

val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnSurface,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = OnSurface,
    tertiary = EveningColor,
    onTertiary = OnPrimary,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = Background,
    onSurfaceVariant = OnBackground,
    outline = StatusPending
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = OnSurface,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryLight,
    onSecondary = OnSurface,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = OnSecondary,
    tertiary = EveningColor,
    onTertiary = OnPrimary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = OnBackgroundDark,
    outline = StatusPending
)

@Composable
fun TrackMedTheme(
    themeMode: EThemeMode = EThemeMode.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        EThemeMode.SYSTEM -> systemDarkTheme
        EThemeMode.LIGHT -> false
        EThemeMode.DARK -> true
    }

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
