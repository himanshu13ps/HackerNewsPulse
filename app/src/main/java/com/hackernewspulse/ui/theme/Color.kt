package com.hackernewspulse.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
val LightPrimary = Color(0xFFD35400) // Professional Amber/Orange
val LightSecondary = Color(0xFF4A6572)
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightOnSurfaceVariant = Color(0xFF44474E)
val LightStorySelector = Color(0xFFFFE0B2)

// Dark Mode Colors
val DarkPrimary = Color(0xFFFB8C00)
val DarkSecondary = Color(0xFFBAC8D3)
val DarkBackground = Color(0xFF141210) // Warm Dark
val DarkSurface = Color(0xFF1F1B16)    // Warm Surface
val DarkOnSurface = Color(0xFFE6E1DC)
val DarkOnSurfaceVariant = Color(0xFFD0C4B8)
val DarkStorySelector = Color(0xFF4D2600)

// Extension property for custom storySelector color
val androidx.compose.material3.ColorScheme.storySelector: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkStorySelector else LightStorySelector
