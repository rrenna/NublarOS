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

// Placeholder module — see README "Signature Component: Storm Simulation"
// and docs/architecture.md "StormTrack Rendering". First prototype target:
// Compose Canvas island overlay + storm field, loading the YAML simulation
// data model described in the README.
