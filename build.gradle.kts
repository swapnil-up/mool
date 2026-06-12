plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.detekt)
}

subprojects {
    if (name == "ios") return@subprojects // empty module, no Kotlin plugin

    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        detekt {
            buildUponDefaultConfig = true
            config.setFrom(rootProject.file("detekt.yml"))
            source.setFrom(
                files("src/commonMain/kotlin"),
                files("src/androidMain/kotlin"),
                files("src/iosMain/kotlin"),
                files("src/commonTest/kotlin"),
                files("src/main/kotlin"),
                files("src/test/kotlin")
            )
        }
    }
}
