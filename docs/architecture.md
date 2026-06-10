# Architecture

Mool follows **Clean Architecture** with 3 explicit layers, **MVI** state management per feature, and manual dependency injection via constructor wiring in the composition root.

---

## Layer Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    :app:android / :app:ios               │
│              (Platform entry points)                     │
└───────────────────────┬─────────────────────────────────┘
                        │ depends on
┌───────────────────────▼─────────────────────────────────┐
│                    :shared                               │
│         (Umbrella module, composition root)              │
│  - Wires all repos → VMs → Screens                      │
│  - Produces iOS framework via Kotlin/Native              │
└────┬────────────┬──────────────┬──────────────┬──────────┘
     │            │              │              │
     ▼            ▼              ▼              ▼
┌─────────┐ ┌──────────┐ ┌──────────────┐ ┌──────────┐
│:feature:│ │:feature: │ │:feature:     │ │:feature: │
│dashboard│ │transact. │ │remittance    │ │settings  │
│(MVI)    │ │(MVI)     │ │(MVI, stub)   │ │(MVI)     │
└────┬────┘ └────┬─────┘ └──────┬───────┘ └────┬─────┘
     │           │              │              │
     └───────────┼──────────────┼──────────────┘
                 │              │
         ┌───────▼──────────────▼───────┐
         │         :core:domain          │
          │  (Models, Repository IFaces,  │
          │   UseCases — no dependencies) │
          └───────┬──────────────┬───────┘
                  │              │
          ┌───────▼──────────────▼───────┐
          │         :core:data             │
          │  (Repository Impls that need   │
          │   network + database together) │
          └───────┬──────────────┬───────┘
                  │              │
          ┌───────▼─────┐  ┌────▼────────┐
          │:core:network │  │:core:database│
          │(Ktor client, │  │(SQLDelight   │
          │ API models)  │  │ queries,     │
          │              │  │ Repository   │
          │              │  │ Impls)       │
          └─────────────┘  └─────────────┘
          
:core:security  (Biometric gate, encryption — expect/actual)
:core:ui        (Shared theme, reusable composables)
```

---

## Module Responsibilities

| Module | Responsibility | Key Dependencies |
|---|---|---|
| `:core:domain` | Pure Kotlin models (`Transaction`, `ExchangeRate`), repository interfaces, `Clock` interface, use cases | None |
| `:core:data` | Repository impls that coordinate multiple data sources (`ExchangeRateRepositoryImpl`) | `:core:database`, `:core:network` |
| `:core:network` | Ktor `HttpClient` factory, typed API client (`FxApiClient`), serializable response models | Ktor, kotlinx-serialization |
 | `:core:database` | SQLDelight `.sq` files (3 tables), pure-database `RepositoryImpl` classes (`TransactionRepositoryImpl`, `SettingsRepositoryImpl`), `SystemClock` (implements `:core:domain`'s `Clock`) | SQLDelight |
| `:core:security` | `expect`/`actual` declarations for biometric auth, encryption, cert pinning | Platform-specific SDKs |
| `:core:ui` | `MoolTheme` (Material3 light/dark color schemes), `ErrorBanner`, canvas nav icons, `Double.toFixed()` formatting utility | Compose Multiplatform |
| `:feature:dashboard` | MVI: `DashboardState`, `DashboardIntent`, `DashboardEffect`, `DashboardViewModel`, `DashboardScreen` | `:core:domain`, `:core:database` |
| `:feature:transactions` | MVI: form + history screens | `:core:domain`, `:core:database` |
| `:feature:remittance` | MVI: remittance calculator (stub) | `:core:domain` |
| `:feature:settings` | MVI: currency preference toggle | `:core:domain`, `:core:database` |
| `:shared` | Composition root — wires all dependencies, hosts `App()` composable, bottom nav | All feature + core modules |
| `:app:android` | Android `Activity`, `AndroidManifest.xml` with INTERNET permission | `:shared` |
| `:app:ios` | Xcode project wrapper for iOS | `:shared` (via framework) |

---

## MVI Pattern (per feature)

Each feature follows the **Model-View-Intent** pattern:

```
  ┌──────────┐   Intent    ┌──────────────┐
  │  Screen  │ ──────────► │  ViewModel   │
  │ (Compose)│             │              │
  │          │ ◄────────── │  - accept()  │
  └──────────┘   State     │  - scope     │
       │                   │  - jobs      │
       │ Effect            └──────┬───────┘
       ▼                         │ calls
  ┌──────────┐                   ▼
  │  Toast / │           ┌──────────────┐
  │  Nav     │           │  Repository  │
  └──────────┘           │  (interface) │
                          └──────┬───────┘
                                 │
                          ┌──────▼───────┐
                          │  Impl class  │
                          │  (SQLDelight │
                          │   / Ktor)    │
                          └──────────────┘
```

### State
A `data class` holding all UI state. Example:
```kotlin
data class DashboardState(
    val isLoading: Boolean = false,
    val balance: Double = 0.0,
    val preferredCurrency: String = "USD",
    val rates: List<ExchangeRate> = emptyList(),
    val error: String? = null,
)
```

### Intent
A `sealed interface` representing user actions:
```kotlin
sealed interface DashboardIntent {
    data object Refresh : DashboardIntent
}
```

### Effect
A `sealed interface` for one-shot side effects (navigation, snackbar):
```kotlin
sealed interface DashboardEffect {
    data class ShowError(val message: String) : DashboardEffect
}
```

### ViewModel
A plain class (not `androidx.lifecycle.ViewModel`) with a `CoroutineScope(SupervisorJob() + Dispatchers.Main)`. Observables are started from `LaunchedEffect` in the composable. Multiple independent flows use separate `Job` references to avoid cancellation cross-talk:

```kotlin
class DashboardViewModel(...) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var ratesJob: Job? = null
    private var balanceJob: Job? = null
    private var settingsJob: Job? = null

    fun accept(intent: DashboardIntent) { ... }
    fun observeBalance() { ... }
    fun observePreferredCurrency() { ... }
}
```

---

## Dependency Injection

Manual DI via constructor wiring in `App.kt`:

```kotlin
@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    val httpClient = remember { MoolHttpClient.create() }
    val database = remember { MoolDatabase(databaseDriverFactory.createDriver()) }
    val exchangeRepo = remember { ExchangeRateRepositoryImpl(database) }
    val transactionRepo = remember { TransactionRepositoryImpl(database) }
    val settingsRepo = remember { SettingsRepositoryImpl(database) }
    val dashboardVm = remember { DashboardViewModel(exchangeRepo, transactionRepo, settingsRepo) }
    val transactionVm = remember { TransactionFormViewModel(transactionRepo) }
    val historyVm = remember { TransactionHistoryViewModel(transactionRepo) }
    val settingsVm = remember { SettingsViewModel(settingsRepo) }
    ...
}
```

Koin annotations is planned for a future iteration. The manual approach keeps the dependency graph explicit and avoids KSP compilation overhead during early development.

---

## Data Flow

```
User taps "Refresh" on Dashboard
        │
        ▼
DashboardScreen → DashboardIntent.Refresh → DashboardViewModel.accept()
        │
        ▼
loadRates():
  ExchangeRateRepositoryImpl.refreshRates()
    │
    ▼
  FxApiClient.fetchRates("USD")
    │  HTTP GET https://open.er-api.com/v6/latest/USD
    ▼
  FxRateResponse (JSON → kotlinx.serialization)
    │
    ▼
  upsertRate() × N (SQLDelight transaction)
    │
    ▼
  observeRates().collect { rates → _state.update { it.copy(rates = rates) } }
    │
    ▼
  Compose recomposes DashboardScreen with new rates
```

For balance:
```
TransactionRepositoryImpl.observeBalance()
  │
  ▼
SELECT SUM(CASE WHEN type='INCOME' THEN amount ELSE -amount END) FROM transactions
  │  SQLDelight → Flow<Double>
  ▼
_state.update { it.copy(balance = ...) }
  │
  ▼
DashboardScreen shows reactive balance in preferred currency
```

---

## SQLDelight Schema

Three tables in `core/database/src/commonMain/sqldelight/com/mool/core/database/`:

### `transactions`
| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | PK, autoincrement |
| `amount` | REAL | |
| `currency` | TEXT | Default USD |
| `description` | TEXT | |
| `type` | TEXT | CHECK IN ('INCOME', 'EXPENSE') |
| `category` | TEXT | |
| `created_at` | INTEGER | Unix millis |

### `exchange_rates`
| Column | Type | Notes |
|---|---|---|
| `from_currency` | TEXT | PK (composite) |
| `to_currency` | TEXT | PK (composite) |
| `rate` | REAL | |
| `updated_at` | INTEGER | Unix millis |

### `app_settings`
| Column | Type | Notes |
|---|---|---|
| `key` | TEXT | PK |
| `value` | TEXT | Generic string value |

---

## Key Decisions & Trade-Offs

| Decision | Rationale |
|---|---|
| **ViewModel = plain class** | JetBrains KMP `ViewModel` requires `ViewModelStoreOwner`; crashes in `remember { ViewModel() }`. Using `CoroutineScope(SupervisorJob() + Dispatchers.Main)` with `LaunchedEffect`-triggered observables avoids the issue entirely. |
| **Manual DI over Koin** | Koin Annotations requires KSP and adds complexity. Manual DI in the composition root is explicit, easy to debug, and sufficient at this scale. |
| **Canvas-drawn icons over material-icons** | `material-icons-core` artifact is not available at Compose Multiplatform `1.11.1` on Maven Central (latest available is `1.7.3`). Canvas icons avoid the dependency conflict with zero runtime cost. |
| **Separate coroutine jobs per flow** | Prevents cancellation cross-talk. If `loadRates()` cancels its job, the balance flow and settings flow continue unaffected. |
| **SQLDelight `AS` engine** | Pure Kotlin multiplatform SQL. Generates typesafe queries from `.sq` files. No ORM overhead. |
| **`mapToOne` vs `mapToOneOrNull`** | Balance query uses `mapToOne` because `SUM` always returns a row. Settings query uses `mapToOneOrNull` because a setting key may not exist. |
| **`ktor-client-core` as `api` dependency** | Consumers (`:shared`) need to see `HttpClient` type transitively. |

---

## Version Catalog

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.4.0 | Language |
| Compose Multiplatform | 1.11.1 | Shared UI |
| Ktor | 3.0.3 | HTTP client |
| SQLDelight | 2.0.2 | Local persistence |
| Kotlinx Coroutines | 1.9.0 | Async |
| Kotlinx Serialization | 1.7.3 | JSON |
| Kotlinx Datetime | 0.6.1 | Cross-platform time |
| Koin | 4.0.2 | DI (planned) |
| AGP | 9.0.1 | Android build |

---

## Build

```bash
# Android APK
./gradlew :app:android:assembleDebug
# → app/android/build/outputs/apk/debug/android-debug.apk

# Install on device
adb install -r app/android/build/outputs/apk/debug/android-debug.apk

# iOS framework (macOS only)
./gradlew :shared:iosSimulatorArm64MainBinaries

# Run all tests
./gradlew allTests
```
