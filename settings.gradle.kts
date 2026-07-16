rootProject.name = "nublaros"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // jediterm (IntelliJ's embedded-terminal library) is not published to
        // Maven Central — it lives in JetBrains' own dependency repository.
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }
}

include(
    ":design-system",
    ":dashboard",
    ":stormtrack",
    ":system-navigator",
    ":command-interface",
)
