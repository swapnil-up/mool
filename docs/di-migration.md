# Dependency Injection: Koin DSL → Annotations

The project migrated from **manual Koin DSL** (`val module = module { single { } }`) to **Koin Annotations** (`@Module`, `@Single`, `@Provided`) using the **Koin Compiler Plugin** — a native Kotlin 2 compiler plugin that replaces the deprecated KSP-based `koin-ksp-compiler`.

## What changed

| Before | After |
|---|---|
| `val appModule = module { single { MyRepo(get()) } }` | `@Module class AppModule { @Single fun provideMyRepo(): MyRepo = ... }` |
| `startKoin { modules(appModule) }` | `@KoinApplication(modules = [AppModule::class]) class MoolApp` + `startKoin { }` |
| `val vm = remember { MyViewModel(repo) }` | `val vm = koinInject<MyViewModel>()` |
| One-time `koinInject()` for repos in App.kt | Direct `koinInject<ViewModel>()` for all ViewModels |

## Problems encountered

### 1. Kotlin 2.4.0 incompatibility with Koin Compiler Plugin 1.0.0

The official stable release (v1.0.0, May 21 2026) was compiled against Kotlin 2.3.20. Kotlin 2.4.0 (June 4 2026) changed FIR extension internals, causing:

```
ClassCastException: FirExtensionRegistrarAdapter$Companion cannot be cast to ProjectExtensionDescriptor
```

Issue [#19](https://github.com/InsertKoinIO/koin-compiler-plugin/issues/19) was fixed on `main` (commit `0b25301`) with a version-adapter layer supporting both Kotlin 2.3.20 and 2.4.0, but **v1.0.1 has not been published to Maven Central yet**.

**Workaround:** The plugin was built from source and published to `~/.m2/repository`. The project uses `mavenLocal()` in both `pluginManagement` and `dependencyResolutionManagement` in `settings.gradle.kts`. The CI workflow (`.github/workflows/ci.yml`) also builds the plugin from source before the app build. This is temporary — swap back to Maven Central version once 1.0.1 drops.

### 2. Compile-time safety flagged platform-specific dependency

`MoolDatabase` (SQLDelight) is created in `platformModule()` using manual DSL (Android `ApplicationContext` needed for the driver). The compiler plugin's A4 call-site validation flags it as missing since it's not in the annotation-based graph.

**Fix:** `@Provided` annotation on the `MoolDatabase` parameter tells the safety checker the dependency is available at runtime:

```kotlin
@Single
fun provideSettingsRepository(@Provided db: MoolDatabase): SettingsRepository =
    SettingsRepositoryImpl(db)
```

### 3. All injected types must be registered

Every type used with `koinInject<T>()` must have a corresponding `@Single`, `@Factory`, or provider function in a `@Module`. Four ViewModels (`DashboardViewModel`, `TransactionFormViewModel`, `TransactionHistoryViewModel`, `RemittanceViewModel`) needed explicit `@Single` providers added to `AppModule`.

### 4. `SettingsViewModel` kept as manual `remember`

It takes `isBiometricSupported: Boolean` — a runtime platform value, not from DI. No clean way to convert without an expect/actual pattern for platform booleans.

## Key decisions

- **Compiler Plugin over KSP**: Native K2 plugin eliminates KSP version coordination and is cleaner for KMP.
- **`platformModule()` stays manual**: The Android SQLDelight driver setup (`AndroidDriver(MoolDatabase.Schema, androidContext())`) is inherently platform-specific. Converting it to an expect/actual `@Module` would add complexity without benefit.
- **`@Single` for ViewModels**: All four ViewModels are singletons (same as the original `remember` behavior — one instance per app lifetime). This is fine since they manage their own `CoroutineScope`.
- **Non-typed `startKoin`**: The typed `startKoin<MoolApp>()` DSL is a compiler plugin feature, but didn't resolve in the `app:android` module. The non-typed version works identically.

## Reverting to official release

When the Koin team publishes v1.0.1 (or later) to Maven Central:

1. Remove `mavenLocal()` from `settings.gradle.kts` (both `pluginManagement.repositories` and `dependencyResolutionManagement.repositories`)
2. Update `gradle/libs.versions.toml`: `koin-compiler-plugin = "1.0.1"`
3. Delete `~/.m2/repository/io/insert-koin/koin-compiler-plugin/` and `~/.m2/repository/io/insert-koin/koin-compiler-gradle-plugin/`
