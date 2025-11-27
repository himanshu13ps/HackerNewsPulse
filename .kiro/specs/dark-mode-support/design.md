# Design Document

## Overview

This design implements system-responsive dark mode support for HackerNewsPulse using Material Design 3's built-in theming capabilities. The app will automatically detect and respond to the device's system dark mode setting, providing appropriate color schemes for both light and dark environments.

The implementation leverages Jetpack Compose's `isSystemInDarkTheme()` function and Material Design 3's color scheme system to create a seamless dark mode experience without requiring user configuration.

## Architecture

### Theme System Architecture

```
┌─────────────────────────────────────────┐
│         System Dark Mode Setting        │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│    isSystemInDarkTheme() Detection      │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│      HackerNewsPulseTheme Selection     │
│  ┌─────────────┐    ┌────────────────┐ │
│  │ Light Color │ or │  Dark Color    │ │
│  │   Scheme    │    │    Scheme      │ │
│  └─────────────┘    └────────────────┘ │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│        MaterialTheme Provider           │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│         All UI Components               │
│  (MainScreen, StoryCard, TopBar, etc.)  │
└─────────────────────────────────────────┘
```

### Component Hierarchy

1. **Theme Layer**: Defines color schemes and applies them based on system settings
2. **Screen Layer**: Uses theme colors through MaterialTheme
3. **Component Layer**: Individual UI elements inherit theme colors automatically

## Components and Interfaces

### 1. Color Definitions (Color.kt)

**Current State:**
- Hardcoded colors for light mode only
- Direct color references in components

**New Design:**
- Separate light and dark color palettes
- Material Design 3 semantic color tokens
- No direct color references in components

**Color Palette:**

```kotlin
// Light Mode Colors
val LightPrimary = Color(0xFF8EC5FC)
val LightSecondary = Color(0xFFE0C3FC)
val LightBackground = Color(0xFF8EC5FC)
val LightSurface = Color.White
val LightOnSurface = Color(0xFF212121)
val LightOnSurfaceVariant = Color(0xFF757575)

// Dark Mode Colors
val DarkPrimary = Color(0xFF6BA3E8)
val DarkSecondary = Color(0xFFC8A8E8)
val DarkBackground = Color(0xFF1A1A2E)
val DarkSurface = Color(0xFF252538)
val DarkOnSurface = Color(0xFFE8E8E8)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0)
```

### 2. Theme Configuration (Theme.kt)

**Current State:**
- Incomplete dark color scheme
- Incorrect status bar color logic
- Limited Material Design 3 token usage

**New Design:**
- Complete light and dark color schemes
- Proper Material Design 3 semantic tokens
- Correct status bar appearance handling

**Color Scheme Mapping:**

| Token | Light Mode | Dark Mode | Usage |
|-------|-----------|-----------|-------|
| primary | LightPrimary | DarkPrimary | App bar, FABs |
| secondary | LightSecondary | DarkSecondary | Accents |
| background | LightBackground | DarkBackground | Screen background |
| surface | LightSurface | DarkSurface | Cards, sheets |
| onSurface | LightOnSurface | DarkOnSurface | Text on surfaces |
| onSurfaceVariant | LightOnSurfaceVariant | DarkOnSurfaceVariant | Secondary text |

### 3. UI Components

**MainScreen.kt Changes:**
- Replace hardcoded `Color.White` with `MaterialTheme.colorScheme.onBackground`
- Replace gradient background colors with theme-aware colors
- Update loading indicators to use theme colors
- Update tab selector colors to use theme tokens

**StoryCard.kt Changes:**
- Remove direct `TextPrimary` and `TextSecondary` imports
- Use `MaterialTheme.colorScheme.onSurface` for primary text
- Use `MaterialTheme.colorScheme.onSurfaceVariant` for secondary text
- Card background automatically uses `surface` color from theme

## Data Models

No data model changes required. This is purely a UI/theme implementation.

## Error Handling

### Theme Application Errors

**Scenario**: Theme fails to apply correctly
- **Detection**: Visual inspection during development
- **Handling**: Fallback to light theme as default
- **Prevention**: Comprehensive testing on multiple devices

### System Theme Detection Errors

**Scenario**: `isSystemInDarkTheme()` returns unexpected value
- **Detection**: Automated tests
- **Handling**: Default to light theme
- **Prevention**: Use stable Compose APIs

### Color Contrast Issues

**Scenario**: Text not readable in certain theme combinations
- **Detection**: Accessibility scanner during development
- **Handling**: Adjust color values to meet WCAG AA standards
- **Prevention**: Use Material Design 3 recommended color ratios

## Testing Strategy

### Manual Testing

1. **System Theme Toggle Test**
   - Enable dark mode in system settings
   - Verify app switches to dark theme
   - Disable dark mode in system settings
   - Verify app switches to light theme
   - Confirm no app restart required

2. **Visual Consistency Test**
   - Navigate through all screens in light mode
   - Navigate through all screens in dark mode
   - Verify all components render correctly
   - Check for any hardcoded colors

3. **Contrast Verification**
   - Use Android Accessibility Scanner
   - Verify text contrast ratios meet WCAG AA (4.5:1 for normal text)
   - Test with different font sizes

### Component Testing

1. **Theme Provider Test**
   - Verify correct color scheme selected based on system setting
   - Verify status bar color updates correctly
   - Verify status bar icon colors invert appropriately

2. **Color Token Test**
   - Verify all components use MaterialTheme.colorScheme
   - Verify no hardcoded Color values in components
   - Verify gradient backgrounds adapt to theme

3. **Card Component Test**
   - Verify card backgrounds use surface color
   - Verify text uses appropriate on-surface colors
   - Verify elevation/shadows render correctly in dark mode

### Edge Cases

1. **Theme Change During App Use**
   - User changes system theme while app is open
   - Verify app updates immediately without crash
   - Verify scroll position maintained

2. **Different Android Versions**
   - Test on Android 8.0 (API 26) - minimum supported
   - Test on Android 12+ with Material You
   - Test on Android 14 (latest)

3. **Accessibility Settings**
   - Test with large font sizes
   - Test with high contrast mode enabled
   - Test with color inversion enabled

## Implementation Notes

### Key Principles

1. **Use Semantic Tokens**: Always use `MaterialTheme.colorScheme.*` instead of direct colors
2. **No Hardcoded Colors**: Remove all `Color.White`, `Color.Black` references in components
3. **Automatic Adaptation**: Let Material Design 3 handle theme switching
4. **Maintain Readability**: Ensure 4.5:1 contrast ratio minimum

### Migration Strategy

1. Update Color.kt with complete color palettes
2. Update Theme.kt with proper color schemes
3. Update MainScreen.kt to use theme colors
4. Update StoryCard.kt to use theme colors
5. Remove unused color constants
6. Test on physical devices

### Performance Considerations

- Theme switching is handled by Compose runtime (no performance impact)
- Color scheme selection happens once per composition
- No additional memory overhead
- No impact on app startup time

## Design Decisions and Rationales

### Decision 1: Use Material Design 3 Color System

**Rationale**: Material Design 3 provides a robust, tested color system with built-in dark mode support. This reduces implementation complexity and ensures consistency with Android platform guidelines.

**Alternatives Considered**:
- Custom color system: More control but higher maintenance
- Material Design 2: Older system with less sophisticated dark mode support

### Decision 2: No User Toggle for Theme Selection

**Rationale**: Following system theme provides consistency across all apps and respects user's device-wide preference. Reduces UI complexity and settings management.

**Alternatives Considered**:
- In-app theme toggle: Adds complexity and potential for confusion
- Always dark/always light: Ignores user preference

### Decision 3: Gradient Background Adaptation

**Rationale**: Maintain app's visual identity while adapting colors to theme. Use darker gradient colors in dark mode to reduce eye strain.

**Alternatives Considered**:
- Solid background: Less visually interesting
- Same gradient in both modes: Poor contrast in dark mode

### Decision 4: Immediate Theme Updates

**Rationale**: Compose's reactive system allows instant theme updates without app restart, providing better user experience.

**Alternatives Considered**:
- Require app restart: Poor user experience
- Delayed update: Confusing and inconsistent
