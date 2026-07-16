package os.nublar.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import os.nublar.dashboard.metrics.SystemMetricsReader
import os.nublar.dashboard.ui.MetricsGrid
import os.nublar.dashboard.ui.screens.ControlRoomPlanView
import os.nublar.dashboard.ui.screens.IslandMapView
import os.nublar.designsystem.NublarTheme
import os.nublar.designsystem.NublarType

private enum class Screen { Dashboard, ControlRoomPlanView, IslandMap }

/**
 * Nedryland Monitor entry point. Full-screen mode is a real native macOS/
 * Linux fullscreen (WindowPlacement.Fullscreen), per docs/architecture.md.
 */
fun main() = application {
    var isFullscreen by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf(Screen.Dashboard) }
    // Shared across ControlRoomPlanView and IslandMapView so the pane split
    // stays put when switching screens via the SCREEN button.
    var splitFraction by remember { mutableStateOf(0.535f) }
    val windowState = rememberWindowState(
        placement = if (isFullscreen) WindowPlacement.Fullscreen else WindowPlacement.Floating,
        width = 1280.dp,
        height = 1000.dp,
    )
    val metricsReader = remember { SystemMetricsReader() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "NEDRYLAND MONITOR",
        state = windowState,
    ) {
        NublarTheme {
            when (screen) {
                Screen.Dashboard -> Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(text = "PARK SYSTEMS STATUS", style = NublarType.Header)
                    MetricsGrid(
                        metricsReader = metricsReader,
                        onToggleFullscreen = { isFullscreen = !isFullscreen },
                    )
                    Button(onClick = { screen = Screen.ControlRoomPlanView }) {
                        Text("CONTROL ROOM / PLAN VIEW")
                    }
                }

                Screen.ControlRoomPlanView -> ControlRoomPlanView(
                    onClose = { screen = Screen.Dashboard },
                    onSwitchScreen = { screen = Screen.IslandMap },
                    splitFraction = splitFraction,
                    onSplitFractionChange = { splitFraction = it },
                )

                Screen.IslandMap -> IslandMapView(
                    onClose = { screen = Screen.Dashboard },
                    onSwitchScreen = { screen = Screen.ControlRoomPlanView },
                    splitFraction = splitFraction,
                    onSplitFractionChange = { splitFraction = it },
                )
            }
        }
    }
}
