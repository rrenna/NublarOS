package os.nublar.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
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
import os.nublar.dashboard.show.DEMO_DURATION_SECONDS
import os.nublar.dashboard.show.DEMO_TIMELINE
import os.nublar.dashboard.show.ShowAction
import os.nublar.dashboard.show.ShowClock
import os.nublar.dashboard.show.ShowController
import os.nublar.dashboard.ui.screens.ChunkyButton
import os.nublar.dashboard.ui.screens.JurassicParkSystemView
import os.nublar.dashboard.ui.screens.NedryStopwatchWindow
import os.nublar.dashboard.ui.screens.SettingsView
import os.nublar.dashboard.ui.screens.ShowSyncView
import os.nublar.stormtrack.EarthWatchView
import os.nublar.dashboard.viewmodel.AppViewModel
import os.nublar.dashboard.viewmodel.ControlRoomViewModel
import os.nublar.dashboard.viewmodel.IslandMapViewModel
import os.nublar.dashboard.viewmodel.Screen
import os.nublar.designsystem.CrtScreen
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
        // Show-sync engine: fires timed events into the app's high-level actions.
        val showController = remember {
            ShowController(DEMO_TIMELINE, DEMO_DURATION_SECONDS) { action ->
                when (action) {
                    is ShowAction.HighlightMachine -> appViewModel.highlightMachine(action.machine)
                    is ShowAction.ShowScreen -> appViewModel.navigateTo(action.screen)
                    is ShowAction.FailFence -> islandMapViewModel.failFence(action.paddockId)
                    ShowAction.RearmAllFences -> islandMapViewModel.rearmAllFences()
                }
            }
        }
        val windowState = rememberWindowState(
            width = 1280.dp,
            height = 1000.dp,
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "NEDRYLAND MONITOR",
            state = windowState,
        ) {
            // Kiosk fullscreen: Java full-screen EXCLUSIVE mode makes the window
            // take over the whole display and hides the macOS menu bar and dock
            // (like a game) — unlike WindowPlacement.Fullscreen, which keeps the
            // auto-hiding menu bar. Driven reactively from the Settings toggle.
            LaunchedEffect(appViewModel.fullscreen) {
                val device = java.awt.GraphicsEnvironment
                    .getLocalGraphicsEnvironment().defaultScreenDevice
                if (appViewModel.fullscreen) {
                    if (device.isFullScreenSupported) device.fullScreenWindow = window
                } else if (device.fullScreenWindow == window) {
                    device.fullScreenWindow = null
                }
            }
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
                Menu("View") {
                    Item("Settings…", onClick = { appViewModel.navigateTo(Screen.Settings) })
                }
            }
            // Drives the show clock while playing, regardless of active screen.
            ShowClock(showController)
            NublarTheme {
                CrtScreen(modifier = Modifier.fillMaxSize(), shader = appViewModel.selectedShader) {
                Box(modifier = Modifier.fillMaxSize()) {
                when (appViewModel.screen) {
                    Screen.Dashboard -> Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = "PARK SYSTEMS STATUS", style = NublarType.Header)
                        MetricsGrid(metricsReader = metricsReader)
                        NetworkMachinesGrid(
                            onOpen = { appViewModel.navigateTo(it) },
                            highlightedMachine = appViewModel.highlightedMachine,
                        )
                        Spacer(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChunkyButton(
                                "NEDRY TIMER",
                                modifier = Modifier.width(120.dp),
                                highlight = appViewModel.nedryTimerVisible,
                                onClick = { appViewModel.toggleNedryTimer() },
                            )
                            ChunkyButton(
                                "SHOW SYNC",
                                modifier = Modifier.width(120.dp),
                                onClick = { appViewModel.navigateTo(Screen.ShowSync) },
                            )
                            ChunkyButton(
                                "SETTINGS",
                                modifier = Modifier.width(120.dp),
                                onClick = { appViewModel.navigateTo(Screen.Settings) },
                            )
                            ChunkyButton("EXIT", modifier = Modifier.width(120.dp), onClick = ::exitApplication)
                        }
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

                    Screen.Settings -> SettingsView(
                        selectedShader = appViewModel.selectedShader,
                        onSelectShader = { appViewModel.selectShader(it) },
                        fullscreen = appViewModel.fullscreen,
                        onSetFullscreen = { appViewModel.updateFullscreen(it) },
                        onClose = { appViewModel.navigateTo(Screen.Dashboard) },
                    )

                    Screen.ShowSync -> ShowSyncView(
                        controller = showController,
                        onClose = { appViewModel.navigateTo(Screen.Dashboard) },
                    )
                }

                    // Dennis Nedry's desktop stopwatch — an overlay window that
                    // floats over whatever screen is active.
                    if (appViewModel.nedryTimerVisible) {
                        NedryStopwatchWindow(
                            onClose = { appViewModel.hideNedryTimer() },
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                }
            }
        }
    }
}
