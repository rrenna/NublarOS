rootProject.name = "nublaros"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
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
