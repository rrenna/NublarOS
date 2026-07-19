package os.nublar.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import os.nublar.dashboard.AppPreferences

/** Top-level screens the app can show. */
enum class Screen { Dashboard, ControlRoomPlanView, IslandMap, JurassicParkSystem, WeatherComputer }

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
) {
    var screen by mutableStateOf(
        restoreScreen()
            ?.let { runCatching { Screen.valueOf(it) }.getOrNull() }
            ?: Screen.Dashboard,
    )
        private set

    var isFullscreen by mutableStateOf(false)
        private set

    var splitFraction by mutableStateOf(0.535f)
        private set

    fun navigateTo(target: Screen) {
        screen = target
        persistScreen(target.name)
    }

    fun toggleFullscreen() {
        isFullscreen = !isFullscreen
    }

    fun updateSplitFraction(fraction: Float) {
        splitFraction = fraction
    }
}
