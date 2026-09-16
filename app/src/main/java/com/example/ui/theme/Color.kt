package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ROHIT VIP PANEL - Premium Stealth Crimson, Gold & Obsidian Theme
val CyberBlack = Color(0xFF07080D)
val CyberBackground = Color(0xFF0B0E17)
val CyberSurface = Color(0xFF121726)
val CyberSurfaceVariant = Color(0xFF192136)
val CyberGlassBg = Color(0x33121726)
val CyberGlassBorder = Color(0x55FF1E56)

// High-Impact VIP Accent Palette
val NeonCrimson = Color(0xFFFF1E56)   // Hyper Crimson Red
val NeonGold = Color(0xFFFFB800)      // Radiant Amber Gold
val NeonAzure = Color(0xFF00D2FF)     // Electric Azure
val NeonEmerald = Color(0xFF00E676)   // High-Vis Emerald Green
val NeonCyan = Color(0xFFFF1E56)       // Primary action transformed to VIP Crimson
val NeonBlue = Color(0xFF00D2FF)       // Electric Azure Blue
val NeonPurple = Color(0xFF8B2CF5)     // Cyber Royal Purple
val NeonMagenta = Color(0xFFFF007A)    // Electric Magenta
val NeonRed = Color(0xFFFF2A4B)        // Alert Red
val NeonGreen = Color(0xFF00E676)      // Emerald Green
val NeonYellow = Color(0xFFFFB800)     // Amber Gold
val NeonOrange = Color(0xFFFF6D00)     // Cyber Orange

// Text Colors
val TextWhite = Color(0xFFF9FAFB)
val TextGray = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// RGB Gradient Sequence
val RgbGradientColors = listOf(
  Color(0xFFFF1E56), // Crimson
  Color(0xFFFFB800), // Gold
  Color(0xFF00D2FF), // Azure
  Color(0xFF00E676), // Emerald
  Color(0xFF8B2CF5), // Purple
  Color(0xFFFF007A), // Magenta
  Color(0xFFFF1E56)  // Loop back
)

val CrimsonGoldGradient = Brush.linearGradient(
  colors = listOf(NeonCrimson, NeonGold)
)

val CyanPurpleGradient = Brush.linearGradient(
  colors = listOf(NeonCrimson, NeonPurple)
)

val MagentaBlueGradient = Brush.linearGradient(
  colors = listOf(NeonMagenta, NeonAzure)
)

val DarkCyberGradient = Brush.verticalGradient(
  colors = listOf(CyberBlack, CyberBackground, Color(0xFF05060A))
)

// 3D Metallic & Luxury Shading Brushes
val GoldMetallic3D = Brush.linearGradient(
  colors = listOf(Color(0xFFFFDF7A), Color(0xFFFFB800), Color(0xFFB37B00), Color(0xFFFFD54F))
)

val CrimsonMetallic3D = Brush.linearGradient(
  colors = listOf(Color(0xFFFF5277), Color(0xFFFF1E56), Color(0xFF9E002B), Color(0xFFFF3366))
)

val Titanium3D = Brush.linearGradient(
  colors = listOf(Color(0xFF3A4252), Color(0xFF1E2430), Color(0xFF0F131D), Color(0xFF283142))
)

val Obsidian3DBrush = Brush.verticalGradient(
  colors = listOf(Color(0xFF181F30), Color(0xFF0F1422), Color(0xFF080B12))
)

val SpecularHighlight3D = Color(0x40FFFFFF)
val BevelShadow3D = Color(0x99000000)
