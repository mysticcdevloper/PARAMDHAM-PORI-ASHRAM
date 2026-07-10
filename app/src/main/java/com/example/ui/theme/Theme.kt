package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AshramThemeMode {
  LIGHT,
  DARK,
  GOLDEN,
  HOLY
}

private val LightColorScheme =
  lightColorScheme(
    primary = SaffronLight,
    onPrimary = Color.White,
    secondary = RoyalBlueLight,
    onSecondary = Color.White,
    tertiary = GoldenGlowLight,
    background = CreamBackground,
    onBackground = CharcoalText,
    surface = Color.White,
    onSurface = CharcoalText,
    surfaceVariant = Color(0xFFF5EFE6),
    onSurfaceVariant = CharcoalText,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = SaffronDark,
    onPrimary = DarkNightBackground,
    secondary = RoyalBlueDark,
    onSecondary = Color.White,
    tertiary = GoldenGlowDark,
    background = DarkNightBackground,
    onBackground = IvoryText,
    surface = DarkSurface,
    onSurface = IvoryText,
    surfaceVariant = Color(0xFF1E244A),
    onSurfaceVariant = IvoryText,
  )

private val GoldenColorScheme =
  lightColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    secondary = GoldSecondary,
    onSecondary = Color.White,
    tertiary = GoldTertiary,
    background = GoldBackground,
    onBackground = BrownText,
    surface = GoldSurface,
    onSurface = BrownText,
    surfaceVariant = Color(0xFFFBE9E7),
    onSurfaceVariant = BrownText,
  )

private val HolyColorScheme =
  lightColorScheme(
    primary = HolyPrimary,
    onPrimary = Color.White,
    secondary = HolySecondary,
    onSecondary = Color.White,
    tertiary = HolyTertiary,
    background = HolyBackground,
    onBackground = DeepMaroonText,
    surface = HolySurface,
    onSurface = DeepMaroonText,
    surfaceVariant = Color(0xFFFFE0B2),
    onSurfaceVariant = DeepMaroonText,
  )

@Composable
fun MyApplicationTheme(
  themeMode: AshramThemeMode = AshramThemeMode.LIGHT,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when (themeMode) {
      AshramThemeMode.LIGHT -> LightColorScheme
      AshramThemeMode.DARK -> DarkColorScheme
      AshramThemeMode.GOLDEN -> GoldenColorScheme
      AshramThemeMode.HOLY -> HolyColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
