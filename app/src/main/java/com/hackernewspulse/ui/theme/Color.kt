package com.hackernewspulse.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Colors
val LightPrimary = Color(0xFF3333CC)
val LightSecondary = Color(0xFFE0C3FC)
val LightBackground = Color(0xFF8EC5FC)
val LightSurface = Color(0xFFE7E0EC)
val LightOnSurface = Color(0xFF212121)
val LightOnSurfaceVariant = Color(0xFF757575)
val LightStorySelector = Color(0xFFCE7632)

// Dark Mode Colors
val DarkPrimary = Color(0xFF3333CC)
val DarkSecondary = Color(0xFFC8A8E8)
val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF13134D)
val DarkOnSurface = Color(0xFFE8E8E8)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0)
val DarkStorySelector = Color(0xFFC25400)

// Extension property for custom storySelector color
val androidx.compose.material3.ColorScheme.storySelector: Color
    @androidx.compose.runtime.Composable
    get() = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkStorySelector else LightStorySelector
