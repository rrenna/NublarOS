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

tasks.register<JavaExec>("renderStormFrame") {
    group = "application"
    description = "Renders one storm frame headlessly to stormtrack/build/storm-frame.png for renderer verification."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("os.nublar.stormtrack.RenderFrameMainKt")
}

// StormTrack is an auxiliary/debug app. It is launched via this dedicated task
// rather than the Compose `application {}` block so it does NOT register a
// generic `run` task — otherwise the root `./gradlew run` would fan out and
// open StormTrack alongside the main dashboard. Run it explicitly with
// `./gradlew :stormtrack:runStormTrack`.
tasks.register<JavaExec>("runStormTrack") {
    group = "application"
    description = "Launches the StormTrack 3D storm view window."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("os.nublar.stormtrack.StormTrackMainKt")
}
