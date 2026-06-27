package com.hackernewspulse.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
val LightPrimary = Color(0xFFD35400) // Professional Amber/Orange
val LightAccentOrange = Color(0xFFFB923C) // Lighter Shade of Orange
val LightSecondary = Color(0xFF4A6572)
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightOnSurfaceVariant = Color(0xFF44474E)
val LightStorySelector = Color(0xFFFFE0B2)

// Dark Mode Colors
val DarkPrimary = Color(0xFFA855F7) // Vibrant Purple
val DarkAccentBlue = Color(0xFF3B82F6) // Vibrant Blue
val DarkSecondary = Color(0xFF7C3AED)
val DarkBackground = Color(0xFF0B0E14) // Deep Navy Black
val DarkSurface = Color(0xFF151921)    // Dark Navy Surface
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkOnSurfaceVariant = Color(0xFF94A3B8) // Slate Grey
val DarkStorySelector = Color(0xFF4C1D95)

// Extension property for custom storySelector color
val androidx.compose.material3.ColorScheme.storySelector: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkStorySelector else LightStorySelector

val androidx.compose.material3.ColorScheme.secondaryAccent: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkAccentBlue else LightAccentOrange
