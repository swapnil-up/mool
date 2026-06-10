# Mool

Personal finance tracker built with **Kotlin Multiplatform** + **Compose Multiplatform**. Targets Android and iOS.

## Features

- **Dashboard** — reactive net balance from aggregate SQL query with live FX rates via Ktor
- **Transactions** — add expense/income entries with SQLDelight persistence
- **History** — searchable transaction list with delete
- **Settings** — persisted currency preference (25 currencies)

## Quick Start

```bash
# Build APK
./gradlew :app:android:assembleDebug

# Install on connected device
adb install -r app/android/build/outputs/apk/debug/android-debug.apk
```

## Architecture

```
:core:domain       — models, repository interfaces, use cases (pure Kotlin)
:core:network      — Ktor HTTP client, FX API
:core:database     — SQLDelight schema + repository implementations
:core:security     — biometric gate, encryption (expect/actual)
:core:ui           — Material3 theme, shared composables

:feature:dashboard — MVI: DashboardIntent → State → Effect
:feature:settings  — MVI: currency preference toggle
:feature:transactions — MVI: form + history screens

:shared            — composition root, bottom navigation
:app:android       — Android entry point
:app:ios           — iOS entry point (Xcode)
```

Each feature follows **Clean Architecture** with **MVI** — a `sealed interface` for intents and effects, a `data class` for state, and a plain ViewModel class with `CoroutineScope`. See [docs/architecture.md](docs/architecture.md).

## Tech Stack

| Component | Choice |
|---|---|
| Language | Kotlin 2.4.0 |
| UI | Compose Multiplatform 1.11.1 (Material3) |
| Networking | Ktor 3.0.3 (OkHttp/Darwin engine) |
| Database | SQLDelight 2.0.2 |
| Async | Kotlinx Coroutines 1.9.0, Flow |
| Serialization | Kotlinx Serialization 1.7.3 |
| DI | Manual constructor wiring |

## Build

```bash
# Android
./gradlew :app:android:assembleDebug

# iOS (macOS only)
./gradlew :shared:iosSimulatorArm64MainBinaries
open app/ios/iosApp.xcodeproj

# Tests
./gradlew allTests
```

## License

MIT
