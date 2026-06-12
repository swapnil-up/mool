<div align="center">

# Mool

Personal finance tracker built with Kotlin Multiplatform + Compose Multiplatform. Targets Android and iOS.

_Mool_ derives from Nepali **मूल** (mūla) — _root, origin, principal sum._

[![Build](https://img.shields.io/github/actions/workflow/status/swapnil-up/mool/ci.yml?style=for-the-badge)](https://github.com/swapnil-up/mool/actions)
[![Stars](https://img.shields.io/github/stars/swapnil-up/mool?style=for-the-badge)](https://github.com/swapnil-up/mool/stargazers)

</div>

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

# Run tests
./gradlew allTests
```

## Project Structure

```
app/
  android/          — Android entry point (Koin startup, ProGuard)
  ios/              — iOS entry point (Xcode project)
core/
  data/             — repository implementations
  database/         — SQLDelight schema + drivers
  domain/           — models, repository interfaces, use cases (pure Kotlin)
  network/          — Ktor HTTP client, FX API
  security/         — biometric gate, encryption (expect/actual)
  ui/               — Material3 theme, shared composables
docs/
  architecture.md   — clean architecture + MVI conventions
  di-migration.md   — Koin DSL to annotations migration notes
feature/
  dashboard/        — MVI: balance overview
  remittance/       — MVI: FX transfer
  settings/         — MVI: currency preference, biometric toggle
  transactions/     — MVI: add expense/income + history
shared/             — composition root, bottom navigation, App.kt
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
| DI | Koin Annotations 4.2.1 + Compiler Plugin 1.0.1 |

## Documentation

| Resource | Description |
|---|---|
| [docs/architecture.md](docs/architecture.md) | Clean Architecture conventions, MVI pattern |
| [docs/di-migration.md](docs/di-migration.md) | Koin DSL → Annotations migration, problems & workarounds |

Dependency injection was migrated from manual Koin DSL to Koin Annotations using the Koin Compiler Plugin. The migration encountered a Kotlin 2.4.0 incompatibility resolved by building the plugin from source — see [docs/di-migration.md](docs/di-migration.md) for full details and reversion instructions.

## License

MIT
