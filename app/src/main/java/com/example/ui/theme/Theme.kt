package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryAmber,
    secondary = AccentPurple,
    tertiary = PrimaryGold,
    background = DarkGrayBg,
    surface = DeepSurface,
    onPrimary = Color(0xFF0C0C12),
    onSecondary = TextLight,
    onBackground = TextLight,
    onSurface = TextLight,
    surfaceVariant = CozyCard,
    onSurfaceVariant = TextLight
  )

private val LightColorScheme = DarkColorScheme // Default both to dark scheme for unified premium "dusk" vibe

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force unified nighttime vibe
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful cosmic theme branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
