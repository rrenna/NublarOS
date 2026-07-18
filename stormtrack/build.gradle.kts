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

// LWJGL needs OS-specific native jars at runtime. The Java artifacts come
// from the version catalog (versions managed by the LWJGL BOM); the matching
// natives for the current OS are added here. Cross-platform packaging will
// need the other natives too.
val lwjglNatives = when {
    OperatingSystem.current().isLinux -> "natives-linux"
    OperatingSystem.current().isMacOsX ->
        if (System.getProperty("os.arch") == "aarch64") "natives-macos-arm64" else "natives-macos"
    OperatingSystem.current().isWindows -> "natives-windows"
    else -> error("Unsupported OS for LWJGL natives")
}

dependencies {
    implementation(project(":design-system"))
    implementation(compose.desktop.currentOs)

    implementation(platform(libs.lwjgl.bom))
    implementation(libs.lwjgl.core)
    implementation(libs.lwjgl.opengl)
    implementation(libs.lwjgl.osmesa)
    runtimeOnly("org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-osmesa::$lwjglNatives")
}

compose.desktop {
    application {
        mainClass = "os.nublar.stormtrack.StormTrackMainKt"

        nativeDistributions {
            targetFormats(
                *when {
                    OperatingSystem.current().isMacOsX -> arrayOf(TargetFormat.Dmg)
                    OperatingSystem.current().isLinux -> arrayOf(TargetFormat.Deb, TargetFormat.AppImage)
                    else -> arrayOf(TargetFormat.Exe)
                }
            )
            packageName = "StormTrack"
            packageVersion = "1.0.0"
        }
    }
}
