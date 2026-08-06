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

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldGreen,
    secondary = SkyBlue,
    tertiary = ElectricPurple,
    background = DeepCharcoal,
    surface = DarkGray,
    onPrimary = DeepCharcoal,
    onSecondary = DeepCharcoal,
    onTertiary = DeepCharcoal,
    onBackground = LightText,
    onSurface = LightText,
    error = ErrorRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldGreen,
    secondary = SkyBlue,
    tertiary = ElectricPurple,
    background = LightText,
    surface = LightText, // keeping it simple, but maybe use white
    onPrimary = LightText,
    onSecondary = LightText,
    onTertiary = LightText,
    onBackground = DeepCharcoal,
    onSurface = DeepCharcoal,
    error = ErrorRed
  )

@Composable
fun AuroraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disabling dynamic color to strictly adhere to the Aurora brand colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> DarkColorScheme // Force dark theme for premium feel per specification, or use LightColorScheme if requested. The spec says "Background: Deep Charcoal". Let's use DarkColorScheme for now to prioritize the premium look.
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
