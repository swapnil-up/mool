# Mool — Personal Finance & Remittance Tracker

**Stack:** Kotlin Multiplatform + Compose Multiplatform  
**Targets:** Android, iOS  
**License:** MIT

## Architecture

```
app/android/       — Android entry point
app/ios/           — iOS entry point (Xcode)
core/domain/       — Domain models, repository interfaces
core/network/      — Ktor HTTP client, FX API
core/database/     — SQLDelight persistence
core/security/     — Biometric gate, encryption
core/ui/           — Shared Compose theme, components
feature/dashboard/ — Dashboard (MVI)
feature/transactions/ — Transactions (MVI)
feature/remittance/   — Remittance calculator (MVI)
feature/settings/     — Settings (MVI)
shared/            — Umbrella module, produces iOS framework
```

## Build

```bash
# Android
./gradlew :app:android:assembleDebug

# iOS (macOS only)
./gradlew :shared:iosSimulatorArm64MainBinaries
open app/ios/iosApp.xcodeproj
```

## License

MIT
