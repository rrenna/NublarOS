import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":design-system"))
    implementation(compose.desktop.currentOs)
    implementation(libs.oshi.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

// kotlin("test") aligns to kotlin-test-junit5 once the JUnit Platform is enabled.
tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "os.nublar.dashboard.MainKt"

        nativeDistributions {
            targetFormats(
                *when {
                    OperatingSystem.current().isMacOsX -> arrayOf(TargetFormat.Dmg)
                    OperatingSystem.current().isLinux -> arrayOf(TargetFormat.Deb, TargetFormat.AppImage)
                    else -> arrayOf(TargetFormat.Exe)
                }
            )
            packageName = "Nedryland Monitor"
            packageVersion = "1.0.0"
        }
    }
}

tasks.register<JavaExec>("runMapPreview") {
    group = "application"
    description = "Launches the Island Map component alone in a debug window (layer toggles, sample data)."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("os.nublar.dashboard.MapPreviewMainKt")
}
