# Technology Stack

## Build System
- **Gradle** with Kotlin DSL (`.gradle.kts` files)
- **Android Gradle Plugin** 8.4.0
- **Kotlin** 1.9.22

## Core Technologies
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - UI design system
- **Hilt** - Dependency injection framework
- **Paging 3** - Pagination library for large datasets

## Networking & Data
- **Retrofit** 2.9.0 - HTTP client
- **Kotlinx Serialization** - JSON parsing
- **OkHttp** - HTTP client implementation
- **Coil** - Image loading for Compose

## Architecture Components
- **ViewModel** - UI state management
- **Repository Pattern** - Data layer abstraction
- **Flow** - Reactive data streams
- **Chrome Custom Tabs** - In-app browser

## Common Commands

### Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on device/emulator
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all tests
./gradlew check
```

### Code Quality
```bash
# Clean build
./gradlew clean

# Build with full clean
./gradlew clean build
```

## Development Requirements
- **Java 8** compatibility (source/target)
- **Android SDK 34** (compile SDK)
- **Minimum SDK 26** (Android 8.0+)