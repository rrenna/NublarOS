plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Repositories are declared once in settings.gradle.kts
// (dependencyResolutionManagement) — including the JetBrains
// intellij-dependencies repo jediterm needs. Do not re-declare them here.
