import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":design-system"))
    implementation(compose.desktop.currentOs)
    implementation(libs.oshi.core)
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
