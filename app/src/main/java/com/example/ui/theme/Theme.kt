package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDensityConfig(
    val name: String = "Comfortable",
    val paddingFactor: Float = 1.0f,
    val itemSpacing: Dp = 12.dp,
    val cardHeightMultiplier: Float = 1.0f,
    val cardWidth: Dp = 160.dp,
    val chipHeight: Dp = 38.dp,
    val rowHeight: Dp = 64.dp
)

val LocalAppDensity = compositionLocalOf { AppDensityConfig() }
val LocalCardSurface = compositionLocalOf { Color(0xFFFFFFFF) }

@Composable
fun AuroraTheme(
    themeMode: String = "System Default",
    uiDensity: String = "Comfortable",
    accentColor: String = "Monochrome",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        "Dark", "Dark Mode", "AMOLED Pitch Black" -> true
        "Light", "Light Mode" -> false
        else -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> darkColorScheme(
            primary = DarkAccent,
            onPrimary = DarkButtonText,
            secondary = DarkSecondarySurface,
            onSecondary = DarkPrimaryText,
            background = DarkBackground,
            onBackground = DarkPrimaryText,
            surface = DarkPrimarySurface,
            onSurface = DarkPrimaryText,
            surfaceVariant = DarkSecondarySurface,
            onSurfaceVariant = DarkSecondaryText,
            surfaceContainer = DarkCardSurface,
            surfaceContainerHigh = DarkCardSurface,
            outline = DarkBorder,
            outlineVariant = DarkBorder,
            error = ErrorRed
        )
        else -> lightColorScheme(
            primary = LightAccent,
            onPrimary = LightButtonText,
            secondary = LightSecondarySurface,
            onSecondary = LightPrimaryText,
            background = LightBackground,
            onBackground = LightPrimaryText,
            surface = LightPrimarySurface,
            onSurface = LightPrimaryText,
            surfaceVariant = LightSecondarySurface,
            onSurfaceVariant = LightSecondaryText,
            surfaceContainer = LightPrimarySurface,
            surfaceContainerHigh = LightPrimarySurface,
            outline = LightBorder,
            outlineVariant = LightBorder,
            error = ErrorRed
        )
    }

    val cardSurface = if (isDark) DarkCardSurface else LightPrimarySurface

    val densityConfig = when (uiDensity) {
        "Compact" -> AppDensityConfig(
            name = "Compact",
            paddingFactor = 0.75f,
            itemSpacing = 8.dp,
            cardHeightMultiplier = 0.85f,
            cardWidth = 136.dp,
            chipHeight = 32.dp,
            rowHeight = 52.dp
        )
        "Spacious" -> AppDensityConfig(
            name = "Spacious",
            paddingFactor = 1.3f,
            itemSpacing = 16.dp,
            cardHeightMultiplier = 1.15f,
            cardWidth = 180.dp,
            chipHeight = 44.dp,
            rowHeight = 72.dp
        )
        else -> AppDensityConfig(
            name = "Comfortable",
            paddingFactor = 1.0f,
            itemSpacing = 12.dp,
            cardHeightMultiplier = 1.0f,
            cardWidth = 156.dp,
            chipHeight = 38.dp,
            rowHeight = 62.dp
        )
    }

    CompositionLocalProvider(
        LocalAppDensity provides densityConfig,
        LocalCardSurface provides cardSurface
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}



