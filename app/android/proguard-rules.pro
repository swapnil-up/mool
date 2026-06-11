# --- Android entry points (resolved by manifest) ---
-keep class com.mool.app.android.MoolApplication
-keep class com.mool.app.android.MainActivity

# --- Kotlin metadata ---
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*

# --- kotlinx.serialization ---
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep,includedescriptorclasses class com.mool.core.network.model.**$$serializer { *; }
-keepclassmembers class com.mool.core.network.model.FxRateResponse {
    *** Companion;
}
-keepclasseswithmembers class com.mool.core.network.model.FxRateResponse {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- SQLDelight generated classes ---
-keep class com.mool.core.database.** { *; }

# --- Koin DSL: classes constructed in modules ---
-keep class com.mool.core.network.MoolHttpClient
-keep class com.mool.core.network.FxApiClient
-keep class com.mool.core.database.SystemClock
-keep class com.mool.core.database.SettingsRepositoryImpl
-keep class com.mool.core.database.TransactionRepositoryImpl
-keep class com.mool.core.data.ExchangeRateRepositoryImpl
-keep class com.mool.feature.dashboard.DashboardViewModel
-keep class com.mool.feature.transactions.TransactionFormViewModel
-keep class com.mool.feature.transactions.TransactionHistoryViewModel
-keep class com.mool.feature.remittance.RemittanceViewModel
-keep class com.mool.feature.settings.SettingsViewModel

# --- expect/actual declarations ---
-keep class com.mool.Platform
-keep class com.mool.core.security.EncryptionManager
-keep class com.mool.core.security.BiometricLock
-keep class com.mool.core.database.DatabaseDriverFactory

# --- Compose ---
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
