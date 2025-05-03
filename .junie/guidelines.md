# File Organizer - Developer Guidelines

## Project Overview
File Organizer is an Android application that automatically organizes files based on customizable rules and conditions. The app uses a rule-based system to match files based on criteria such as file type, name pattern, date, and size.

## Project Structure
```
file_organizer/
├── app/                      # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/shevapro/filesorter/
│   │   │   │   ├── data/     # Repositories and data sources
│   │   │   │   ├── di/       # Dependency injection configuration
│   │   │   │   ├── model/    # Data models and entities
│   │   │   │   ├── service/  # Background services and utilities
│   │   │   │   └── ui/       # UI components and screens
│   │   │   └── res/          # Android resources
│   │   ├── test/             # Unit tests
│   │   └── androidTest/      # Instrumentation tests
│   └── build.gradle          # App-level build configuration
├── docs/                     # Project documentation
├── gradle/                   # Gradle wrapper files
└── build.gradle              # Project-level build configuration
```

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Koin
- **Database**: Room
- **Asynchronous Programming**: Kotlin Coroutines
- **Testing**: JUnit, MockK, Turbine, Robolectric

## Development Workflow
1. **Setup**: Clone the repository and open in Android Studio
2. **Build**: Use Gradle to build the project (`./gradlew build`)
3. **Run**: Deploy to an emulator or physical device
4. **Develop**: Follow the MVVM architecture pattern:
   - Models in `model/` package
   - ViewModels in `ui/viewmodels/` package
   - UI components in `ui/screens/` and `ui/components/`
   - Data operations in `data/` package
   - Background services in `service/` package

## Testing Guidelines
- **Unit Tests**: Located in `app/src/test/`
  - Run with: `./gradlew test`
  - Focus on testing ViewModels, Repositories, and Services
  - Use MockK for mocking dependencies
  - Use Turbine for testing Flow emissions
  - Use Robolectric for tests requiring Android framework

- **Instrumentation Tests**: Located in `app/src/androidTest/`
  - Run with: `./gradlew connectedAndroidTest`
  - Focus on UI testing with Compose Test

## Best Practices
1. **Code Organization**:
   - Follow the package structure
   - Keep classes focused on a single responsibility
   - Use dependency injection for all dependencies

2. **UI Development**:
   - Use Composable functions for UI components
   - Extract reusable components to separate files
   - Keep UI logic in ViewModels, not in Composables

3. **Asynchronous Operations**:
   - Use Coroutines and Flow for asynchronous operations
   - Handle exceptions properly
   - Use proper dispatchers (IO for disk/network, Default for CPU-intensive work)

4. **Testing**:
   - Write tests for all new features
   - Maintain high test coverage for critical components
   - Use test-driven development when appropriate

## Common Commands
- Build the project: `./gradlew build`
- Run unit tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
- Generate APK: `./gradlew assembleDebug`
- Install on connected device: `./gradlew installDebug`
- Run lint checks: `./gradlew lint`
- Clean build: `./gradlew clean`
