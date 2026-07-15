plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // jediterm is not published to Maven Central — see settings.gradle.kts.
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}
