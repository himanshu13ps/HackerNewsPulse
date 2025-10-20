# Project Structure

## Root Level
- `app/` - Main Android application module
- `build.gradle.kts` - Root build configuration
- `settings.gradle.kts` - Project settings and module inclusion
- `gradle/` - Gradle wrapper files
- `gradlew` / `gradlew.bat` - Gradle wrapper scripts

## Application Module (`app/`)
```
app/
├── build.gradle.kts          # App-level build configuration
├── proguard-rules.pro        # ProGuard configuration
└── src/main/
    ├── AndroidManifest.xml   # App manifest
    ├── java/com/hackernewspulse/
    │   ├── MainActivity.kt           # Main activity
    │   ├── HackerNewsPulseApplication.kt  # Application class
    │   ├── data/                     # Data layer
    │   │   ├── paging/              # Paging 3 sources
    │   │   ├── remote/              # API services & responses
    │   │   └── repository/          # Repository implementations
    │   ├── di/                      # Dependency injection modules
    │   ├── ui/                      # UI components & screens
    │   │   └── theme/               # Compose theme definitions
    │   └── viewmodel/               # ViewModels
    └── res/                         # Android resources
        ├── values/                  # Strings, colors, themes
        └── xml/                     # Backup & data extraction rules
```

## Architecture Layers

### Data Layer (`data/`)
- `remote/` - API services and response models
- `repository/` - Repository pattern implementations
- `paging/` - Paging 3 data sources

### UI Layer (`ui/`)
- Screen composables and UI components
- `theme/` - Material Design 3 theme configuration

### Dependency Injection (`di/`)
- Hilt modules for dependency provision

### ViewModels (`viewmodel/`)
- UI state management and business logic

## Package Naming Convention
- Base package: `com.hackernewspulse`
- Feature-based sub-packages within each layer
- Clear separation between data, UI, and business logic layers

## File Naming Conventions
- Activities: `*Activity.kt`
- Composables: `*Screen.kt` for screens, `*Card.kt` for components
- ViewModels: `*ViewModel.kt`
- Repositories: `*Repository.kt`
- API Services: `*ApiService.kt`
- Response Models: `*Response.kt`