package com.example.easy_memorization.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = IndigoOnPrimaryDark,
    primaryContainer = IndigoPrimaryContainerDark,
    onPrimaryContainer = IndigoOnPrimaryContainerDark,
    secondary = IndigoSecondaryDark,
    onSecondary = Color(0xFF13141C),
    secondaryContainer = IndigoSecondaryContainerDark,
    onSecondaryContainer = IndigoOnSecondaryContainerDark,
    tertiary = IndigoTertiaryDark,
    onTertiary = Color(0xFF3A1B76),
    tertiaryContainer = IndigoTertiaryContainerDark,
    onTertiaryContainer = IndigoOnTertiaryContainerDark,
    background = Color(0xFF12131A),
    onBackground = Color(0xFFE4E4EF),
    surface = Color(0xFF12131A),
    onSurface = Color(0xFFE4E4EF),
    surfaceVariant = Color(0xFF454654),
    onSurfaceVariant = Color(0xFFC5C6D6),
    surfaceContainerLowest = Color(0xFF0C0D13),
    surfaceContainerLow = Color(0xFF1A1B23),
    surfaceContainer = Color(0xFF1E1F28),
    surfaceContainerHigh = Color(0xFF282932),
    surfaceContainerHighest = Color(0xFF33343E),
    outline = Color(0xFF90909F),
    outlineVariant = Color(0xFF454654),
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = IndigoOnPrimaryLight,
    primaryContainer = IndigoPrimaryContainerLight,
    onPrimaryContainer = IndigoOnPrimaryContainerLight,
    secondary = IndigoSecondaryLight,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = IndigoSecondaryContainerLight,
    onSecondaryContainer = IndigoOnSecondaryContainerLight,
    tertiary = IndigoTertiaryLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = IndigoTertiaryContainerLight,
    onTertiaryContainer = IndigoOnTertiaryContainerLight,
    background = Color(0xFFFAF8FF),
    onBackground = Color(0xFF1A1B21),
    surface = Color(0xFFFAF8FF),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE2E3F0),
    onSurfaceVariant = Color(0xFF5A5B6E),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F2FA),
    surfaceContainer = Color(0xFFEDEDF6),
    surfaceContainerHigh = Color(0xFFE7E7F2),
    surfaceContainerHighest = Color(0xFFE1E1EE),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C5D6),
)

/** Generous corner radii — the minimal modern AI-app look. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun Easy_MemorizationTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
