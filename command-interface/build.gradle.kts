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
    implementation(libs.pty4j)
    implementation(libs.jediterm.core)
    implementation(libs.jediterm.ui)
}
