# TravelEase

Small Android app to manage travel planning and packing checklists.

## Key points
- Language: Java/Kotlin
- Build: Gradle (wrapper included)
- IDE: Android Studio (Android Studio Meerkat Feature Drop | 2024.3.2 recommended)
- Branch: `main`

## Features
- Packing checklist with persistent storage (SharedPreferences)
- Modular Android app structure

## Requirements
- Android Studio (2024.3.2 or newer)
- JDK 17
- Android SDK (matching project's compileSdk)
- Project uses Gradle wrapper: use `.\gradlew.bat` on Windows or `./gradlew` on macOS/Linux

## Build & run (Windows)
1. Open project in Android Studio and let it sync.
2. Or from command line (project root):
   - `.\gradlew.bat assembleDebug`
   - Install/run on device/emulator via Android Studio or `.\gradlew.bat installDebug`

## Tests
- Unit tests: `.\gradlew.bat test`
- Instrumentation tests (device/emulator): `.\gradlew.bat connectedAndroidTest`