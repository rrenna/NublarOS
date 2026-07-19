package os.nublar.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import os.nublar.dashboard.metrics.SystemMetricsReader
import os.nublar.dashboard.ui.MetricsGrid
import os.nublar.dashboard.ui.NetworkMachinesGrid
import os.nublar.dashboard.ui.screens.ControlRoomPlanView
import os.nublar.dashboard.ui.screens.IslandMapView
import os.nublar.dashboard.ui.map.PaddockEnclosure
import os.nublar.dashboard.ui.screens.JurassicParkSystemView
import os.nublar.stormtrack.EarthWatchView
import os.nublar.dashboard.viewmodel.AppViewModel
import os.nublar.dashboard.viewmodel.ControlRoomViewModel
import os.nublar.dashboard.viewmodel.IslandMapViewModel
import os.nublar.dashboard.viewmodel.Screen
import os.nublar.designsystem.NublarTheme
import os.nublar.designsystem.NublarType

/**
 * Nedryland Monitor entry point. Full-screen mode is a real native macOS/
 * Linux fullscreen (WindowPlacement.Fullscreen), per docs/architecture.md.
 */
fun main() {
    // oshi SystemInfo() init and repository JSON parsing are heavy and block,
    // so construct them on a background thread before any composition starts.
    val (metricsReader, controlRoomViewModel, islandMapViewModel) = runBlocking {
        withContext(Dispatchers.IO) {
            Triple(
                SystemMetricsReader(),
                ControlRoomViewModel(),
                IslandMapViewModel(),
            )
        }
    }

    application {
        val appViewModel = remember { AppViewModel() }
        val windowState = rememberWindowState(
            placement = if (appViewModel.isFullscreen) WindowPlacement.Fullscreen else WindowPlacement.Floating,
            width = 1280.dp,
            height = 1000.dp,
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "NEDRYLAND MONITOR",
            state = windowState,
        ) {
            // Events menu: trigger park incidents. Fences submenu fails (disarms)
            // an individual paddock's fence — the disarm animation plays when the
            // Island Map is on screen.
            MenuBar {
                Menu("Events") {
                    Menu("Fences") {
                        islandMapViewModel.paddocks
                            .filter { it.enclosure == PaddockEnclosure.Fenced }
                            .forEach { paddock ->
                                Item(
                                    "Fail ${paddock.label}",
                                    enabled = paddock.armed,
                                    onClick = { islandMapViewModel.failFence(paddock.id) },
                                )
                            }
                        Separator()
                        Item("Re-arm All Fences", onClick = { islandMapViewModel.rearmAllFences() })
                    }
                }
            }
            NublarTheme {
                when (appViewModel.screen) {
                    Screen.Dashboard -> Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = "PARK SYSTEMS STATUS", style = NublarType.Header)
                        MetricsGrid(
                            metricsReader = metricsReader,
                            onToggleFullscreen = { appViewModel.toggleFullscreen() },
                        )
                        NetworkMachinesGrid(onOpen = { appViewModel.navigateTo(it) })
                    }

                    Screen.ControlRoomPlanView -> ControlRoomPlanView(
                        onClose = { appViewModel.navigateTo(Screen.Dashboard) },
                        onSwitchScreen = { appViewModel.navigateTo(Screen.IslandMap) },
                        splitFraction = appViewModel.splitFraction,
                        onSplitFractionChange = { appViewModel.updateSplitFraction(it) },
                        viewModel = controlRoomViewModel,
                    )

                    Screen.IslandMap -> IslandMapView(
                        onClose = { appViewModel.navigateTo(Screen.Dashboard) },
                        onSwitchScreen = { appViewModel.navigateTo(Screen.JurassicParkSystem) },
                        splitFraction = appViewModel.splitFraction,
                        onSplitFractionChange = { appViewModel.updateSplitFraction(it) },
                        viewModel = islandMapViewModel,
                    )

                    Screen.JurassicParkSystem -> JurassicParkSystemView(
                        onClose = { appViewModel.navigateTo(Screen.Dashboard) },
                        onSwitchScreen = { appViewModel.navigateTo(Screen.ControlRoomPlanView) },
                        splitFraction = appViewModel.splitFraction,
                        onSplitFractionChange = { appViewModel.updateSplitFraction(it) },
                    )

                    Screen.WeatherComputer -> EarthWatchView(
                        modifier = Modifier.fillMaxSize(),
                        onExit = { appViewModel.navigateTo(Screen.Dashboard) },
                    )
                }
            }
        }
    }
}
