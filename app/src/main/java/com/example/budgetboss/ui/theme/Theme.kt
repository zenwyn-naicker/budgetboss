/*
 * App theme setup for BudgetBoss.
 * It applies the Material 3 colour scheme, typography, and overall visual styling to the Compose UI.
 */
package com.example.budgetboss.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = NightCanvas,
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = NightText,
    secondary = BlueSecondaryDark,
    onSecondary = NightCanvas,
    secondaryContainer = BlueSecondaryContainerDark,
    onSecondaryContainer = NightText,
    tertiary = BlueTertiaryDark,
    onTertiary = NightCanvas,
    background = NightCanvas,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightMuted,
    outline = NightOutline,
    error = RedErrorDark,
    onError = NightCanvas,
    errorContainer = Color(0xFF4A1520),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = InkText,
    secondary = BlueSecondary,
    onSecondary = Color.White,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = InkText,
    tertiary = BlueTertiary,
    onTertiary = Color.White,
    background = CoolBackground,
    onBackground = InkText,
    surface = WhiteSurface,
    onSurface = InkText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = MutedInk,
    outline = LightOutline,
    error = RedError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

@Composable
fun BudgetBossTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}


