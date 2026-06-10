rootProject.name = "mool"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
            mavenContent {
                includeGroupAndSubgroups("org.jetbrains.compose")
            }
        }
    }
}

include(":shared")
include(":app:android")
include(":app:ios")
include(":core:domain")
include(":core:network")
include(":core:database")
include(":core:security")
include(":core:ui")
include(":feature:dashboard")
include(":feature:transactions")
include(":feature:remittance")
include(":feature:settings")
