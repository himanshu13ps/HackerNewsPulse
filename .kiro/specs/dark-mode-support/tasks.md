# Implementation Plan

- [x] 1. Update color definitions for light and dark modes
  - Define complete light mode color palette with Material Design 3 semantic naming
  - Define complete dark mode color palette with appropriate contrast ratios
  - Remove unused hardcoded color constants (TextPrimary, TextSecondary, CardBackground)
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 2. Configure Material Design 3 color schemes in Theme.kt
  - Implement complete lightColorScheme with all semantic tokens (primary, secondary, background, surface, onSurface, onSurfaceVariant, etc.)
  - Implement complete darkColorScheme with all semantic tokens matching dark mode palette
  - Fix status bar appearance logic to correctly invert based on theme (isAppearanceLightStatusBars should be !darkTheme)
  - Update status bar color to use appropriate theme color instead of primary
  - _Requirements: 1.1, 1.2, 2.1, 2.5_

- [x] 3. Update MainScreen.kt to use theme colors
  - Replace hardcoded Color.White text colors with MaterialTheme.colorScheme.onBackground
  - Update gradient background to use theme-aware colors (primary and secondary from colorScheme)
  - Update loading indicator text color to use theme color
  - Update "Loading more..." text color to use theme color
  - Update tab selector background and text colors to use theme tokens
  - _Requirements: 1.1, 1.4, 3.1, 3.4, 3.5_

- [x] 4. Update StoryCard.kt to use theme colors
  - Remove direct imports of TextPrimary and TextSecondary color constants
  - Replace TextPrimary with MaterialTheme.colorScheme.onSurface for story title
  - Replace TextSecondary with MaterialTheme.colorScheme.onSurfaceVariant for metadata text (author, domain, time, score)
  - Verify Card component automatically uses surface color from theme
  - _Requirements: 1.1, 1.4, 1.5, 3.2, 3.3_

- [x] 5. Verify theme switching behavior
  - Test app launches correctly in light mode when system is in light mode
  - Test app launches correctly in dark mode when system is in dark mode
  - Test app updates immediately when system theme changes while app is running
  - Verify no app restart required for theme changes
  - _Requirements: 1.1, 1.2, 1.3_

- [ ]* 6. Validate accessibility and visual consistency
  - Run Android Accessibility Scanner to verify contrast ratios meet WCAG AA standards
  - Test with large font sizes to ensure text remains readable
  - Verify all screens render correctly in both light and dark modes
  - Check for any remaining hardcoded colors that weren't migrated
  - _Requirements: 1.5, 2.4, 3.3_
