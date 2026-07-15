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
}

// Placeholder module — see README "Signature Component: Spatial File
// Navigator" and docs/architecture.md "System Navigator Rendering (spike
// pending)". Default direction: Compose + LWJGL (OpenGL via SwingPanel),
// same interop pattern as command-interface's embedded terminal. Godot is
// the fallback if that integration proves too fragile — record the spike
// outcome in docs/architecture.md once run.
