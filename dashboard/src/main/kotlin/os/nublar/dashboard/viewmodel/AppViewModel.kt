package os.nublar.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import os.nublar.dashboard.AppPreferences
import os.nublar.designsystem.ScreenShader

/** Top-level screens the app can show. */
enum class Screen { Dashboard, ControlRoomPlanView, IslandMap, JurassicParkSystem, WeatherComputer, Settings, ShowSync }

/**
 * App-level ViewModel: which screen is active (persisted across launches),
 * fullscreen state, and the pane-split fraction shared by the two
 * control-room screens so the divider stays put when switching.
 *
 * ViewModels in NublarOS are plain classes holding Compose snapshot state —
 * no AndroidX lifecycle on desktop. Screens observe the state and call the
 * intent methods; they never mutate state directly.
 */
class AppViewModel(
    // Persistence is injected (defaulting to AppPreferences) so unit tests
    // can exercise the ViewModel without touching the user's real prefs.
    restoreScreen: () -> String? = { AppPreferences.lastScreen },
    private val persistScreen: (String) -> Unit = { AppPreferences.lastScreen = it },
    restoreShader: () -> String? = { AppPreferences.shader },
    private val persistShader: (String) -> Unit = { AppPreferences.shader = it },
    restoreFullscreen: () -> Boolean = { AppPreferences.fullscreen },
    private val persistFullscreen: (Boolean) -> Unit = { AppPreferences.fullscreen = it },
) {
    var screen by mutableStateOf(
        restoreScreen()
            ?.let { runCatching { Screen.valueOf(it) }.getOrNull() }
            ?: Screen.Dashboard,
    )
        private set

    /** Kiosk fullscreen: the window fills the screen with no title bar. Off by default. */
    var fullscreen by mutableStateOf(restoreFullscreen())
        private set

    /** Full-screen post-process shader. Defaults to the scanline CRT effect. */
    var selectedShader by mutableStateOf(
        restoreShader()
            ?.let { runCatching { ScreenShader.valueOf(it) }.getOrNull() }
            ?: ScreenShader.Crt,
    )
        private set

    var splitFraction by mutableStateOf(0.535f)
        private set

    /** Networked machine highlighted by a show event (by name), or null. */
    var highlightedMachine by mutableStateOf<String?>(null)
        private set

    fun highlightMachine(name: String?) {
        highlightedMachine = name
    }

    /** Whether Dennis Nedry's desktop stopwatch window is shown (overlay). */
    var nedryTimerVisible by mutableStateOf(false)
        private set

    fun toggleNedryTimer() {
        nedryTimerVisible = !nedryTimerVisible
    }

    fun hideNedryTimer() {
        nedryTimerVisible = false
    }

    fun navigateTo(target: Screen) {
        screen = target
        // Utility screens aren't persisted: relaunching restores the last
        // MAIN screen rather than dropping the user back into Settings.
        if (target !in TRANSIENT_SCREENS) persistScreen(target.name)
    }

    private companion object {
        val TRANSIENT_SCREENS = setOf(Screen.Settings, Screen.ShowSync)
    }

    fun updateFullscreen(value: Boolean) {
        fullscreen = value
        persistFullscreen(value)
    }

    fun selectShader(shader: ScreenShader) {
        selectedShader = shader
        persistShader(shader.name)
    }

    fun updateSplitFraction(fraction: Float) {
        splitFraction = fraction
    }
}
