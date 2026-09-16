package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
  primary = NeonCrimson,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF380815),
  onPrimaryContainer = Color(0xFFFFD1DC),
  secondary = NeonGold,
  onSecondary = CyberBlack,
  secondaryContainer = Color(0xFF382800),
  onSecondaryContainer = NeonGold,
  tertiary = NeonAzure,
  onTertiary = CyberBlack,
  background = CyberBlack,
  onBackground = TextWhite,
  surface = CyberSurface,
  onSurface = TextWhite,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextGray,
  error = NeonRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // We enforce the bespoke Cyber Neon Theme
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CyberDarkColorScheme,
    typography = Typography,
    content = content
  )
}
