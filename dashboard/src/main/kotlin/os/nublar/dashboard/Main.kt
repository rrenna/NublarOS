package os.nublar.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import os.nublar.dashboard.ui.SecurityCamPanel
import os.nublar.dashboard.ui.screens.ControlRoomPlanView
import os.nublar.dashboard.ui.screens.IslandMapView
import os.nublar.dashboard.ui.map.PaddockEnclosure
import os.nublar.dashboard.data.DockStatus
import os.nublar.dashboard.data.GateStatus
import os.nublar.dashboard.data.HelicopterLocation
import os.nublar.dashboard.data.HelipadStatus
import os.nublar.dashboard.data.INGEN_HELICOPTER
import os.nublar.dashboard.data.MAINTENANCE_SHED_BREAKER_COUNT
import os.nublar.dashboard.data.MaintenanceShedStatus
import os.nublar.dashboard.data.Ship
import os.nublar.dashboard.data.VisitorCenterStatus
import os.nublar.dashboard.ui.map.FractionalPoint
import os.nublar.dashboard.show.JURASSIC_PARK_TIMELINE
import os.nublar.dashboard.show.MOVIE_DURATION_SECONDS
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
            ShowController(JURASSIC_PARK_TIMELINE, MOVIE_DURATION_SECONDS) { action ->
                when (action) {
                    is ShowAction.HighlightMachine -> appViewModel.highlightMachine(action.machine)
                    is ShowAction.ShowScreen -> appViewModel.navigateTo(action.screen)
                    is ShowAction.SelectPaddock -> islandMapViewModel.selectPaddock(action.paddockId)
                    is ShowAction.FailFence -> islandMapViewModel.failFence(action.paddockId)
                    ShowAction.RearmAllFences -> islandMapViewModel.rearmAllFences()
                    is ShowAction.UpdateRaptorBay -> islandMapViewModel.updateLoadingBay { bay ->
                        bay.copy(
                            gate = action.gate ?: bay.gate,
                            loaderOffsetMeters = action.loaderOffsetMeters ?: bay.loaderOffsetMeters,
                            lockEngaged = action.lockEngaged ?: bay.lockEngaged,
                            alert = if (action.clearAlert) null else action.alert ?: bay.alert,
                        )
                    }
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
                    // Toggle each tour car's headlights (checked = on). The
                    // Vehicle Status panel's beams/indicator follow the model.
                    Menu("Headlights") {
                        islandMapViewModel.tourCars.forEach { car ->
                            CheckboxItem(
                                car.label,
                                checked = car.headlightsOn,
                                onCheckedChange = { islandMapViewModel.setCarHeadlights(car.id, it) },
                            )
                        }
                    }
                    // Toggle each tour car's comms (checked = responding). An
                    // unchecked car shows the red "Not Responding" flag.
                    Menu("Vehicle Response") {
                        islandMapViewModel.tourCars.forEach { car ->
                            CheckboxItem(
                                car.label,
                                checked = car.responding,
                                onCheckedChange = { islandMapViewModel.setCarResponding(car.id, it) },
                            )
                        }
                    }
                    // Manually drive the raptor pen's loading-bay incident (the
                    // same states the show timeline scripts for the opening scene).
                    Menu("Raptor Pen") {
                        Item(
                            "Open Loading Gate",
                            onClick = { islandMapViewModel.updateLoadingBay { it.copy(gate = GateStatus.Open) } },
                        )
                        Item(
                            "Raptor Strike (shove loader)",
                            onClick = {
                                islandMapViewModel.updateLoadingBay {
                                    it.copy(loaderOffsetMeters = it.loaderOffsetMeters + 2)
                                }
                            },
                        )
                        Item(
                            "Loading Lock Failure",
                            onClick = { islandMapViewModel.updateLoadingBay { it.copy(lockEngaged = false) } },
                        )
                        Item(
                            "Engage Loading Lock",
                            onClick = { islandMapViewModel.updateLoadingBay { it.copy(lockEngaged = true) } },
                        )
                        Item(
                            "Worker Down",
                            onClick = { islandMapViewModel.updateLoadingBay { it.copy(alert = "WORKER DOWN") } },
                        )
                        Separator()
                        Item("Reset Bay", onClick = { islandMapViewModel.resetLoadingBay() })
                    }
                    // Manually drive the East Dock's berth status and which
                    // ship (if any) is present — the model backing the East
                    // Dock status panel shown when that facility is selected.
                    Menu("East Dock") {
                        Item(
                            "Ship Approaching",
                            onClick = { islandMapViewModel.updateEastDock { it.copy(status = DockStatus.Docking) } },
                        )
                        Item(
                            "Dock Ship",
                            onClick = {
                                islandMapViewModel.updateEastDock {
                                    it.copy(
                                        status = DockStatus.Docked,
                                        ship = Ship(id = "supply-01", name = "SS Nublar Runner"),
                                    )
                                }
                            },
                        )
                        Item(
                            "Ship Departs",
                            onClick = {
                                islandMapViewModel.updateEastDock {
                                    it.copy(status = DockStatus.Departing)
                                }
                            },
                        )
                        Item(
                            "Clear Berth",
                            onClick = { islandMapViewModel.updateEastDock { it.copy(status = DockStatus.Empty, ship = null) } },
                        )
                        Separator()
                        Item("Reset Dock", onClick = { islandMapViewModel.resetEastDock() })
                    }
                    // Move InGen's helicopter between Costa Rica, in transit
                    // (with live coordinates, plotted on the map), and on the
                    // island — the model backing its map marker.
                    Menu("Helicopter") {
                        Item(
                            "Depart Costa Rica",
                            onClick = {
                                islandMapViewModel.updateHelicopter {
                                    it.copy(
                                        location = HelicopterLocation.InTransit,
                                        position = FractionalPoint(0.02f, 0.75f),
                                    )
                                }
                            },
                        )
                        Item(
                            "Advance Halfway",
                            onClick = {
                                islandMapViewModel.updateHelicopter {
                                    it.copy(position = FractionalPoint(0.30f, 0.55f))
                                }
                            },
                        )
                        Item(
                            "Arrive on Island",
                            onClick = {
                                islandMapViewModel.updateHelicopter {
                                    it.copy(location = HelicopterLocation.OnIsland, position = null)
                                }
                            },
                        )
                        Separator()
                        Item("Reset to Costa Rica", onClick = { islandMapViewModel.resetHelicopter() })
                    }
                    // Manually drive the Helipad's landing status and which
                    // helicopter (if any) is present — the model backing the
                    // Helipad status panel shown when that facility is selected.
                    Menu("Helipad") {
                        Item(
                            "Helicopter Approaching",
                            onClick = { islandMapViewModel.updateHelipad { it.copy(status = HelipadStatus.Landing) } },
                        )
                        Item(
                            "Touch Down",
                            onClick = {
                                islandMapViewModel.updateHelipad {
                                    it.copy(status = HelipadStatus.Occupied, helicopter = INGEN_HELICOPTER)
                                }
                            },
                        )
                        Item(
                            "Helicopter Departs",
                            onClick = { islandMapViewModel.updateHelipad { it.copy(status = HelipadStatus.Departing) } },
                        )
                        Item(
                            "Clear Pad",
                            onClick = {
                                islandMapViewModel.updateHelipad { it.copy(status = HelipadStatus.Empty, helicopter = null) }
                            },
                        )
                        Separator()
                        Item("Reset Pad", onClick = { islandMapViewModel.resetHelipad() })
                    }
                    // Manually drive the Visitor Center's operational status
                    // and on-site occupancy — the model backing the Visitor
                    // Center status panel shown when that facility is selected.
                    Menu("Visitor Center") {
                        Item(
                            "Set Operational",
                            onClick = {
                                islandMapViewModel.updateVisitorCenter {
                                    it.copy(status = VisitorCenterStatus.Operational)
                                }
                            },
                        )
                        Item(
                            "Lockdown",
                            onClick = {
                                islandMapViewModel.updateVisitorCenter { it.copy(status = VisitorCenterStatus.Lockdown) }
                            },
                        )
                        Item(
                            "Power Failure",
                            onClick = {
                                islandMapViewModel.updateVisitorCenter {
                                    it.copy(status = VisitorCenterStatus.PowerFailure)
                                }
                            },
                        )
                        Item(
                            "Evacuate (mark unoccupied)",
                            onClick = { islandMapViewModel.updateVisitorCenter { it.copy(occupied = false) } },
                        )
                        Item(
                            "Staff Return",
                            onClick = { islandMapViewModel.updateVisitorCenter { it.copy(occupied = true) } },
                        )
                        Separator()
                        Item("Reset Visitor Center", onClick = { islandMapViewModel.resetVisitorCenter() })
                    }
                    // Manually drive the Maintenance Shed's repair status and
                    // which tour vehicle (if any) is in the bay — the model
                    // backing the Maintenance Shed status panel shown when
                    // that facility is selected.
                    Menu("Maintenance Shed") {
                        islandMapViewModel.tourCars.forEach { car ->
                            Item(
                                "Bring In ${car.label}",
                                onClick = {
                                    islandMapViewModel.updateMaintenanceShed {
                                        it.copy(status = MaintenanceShedStatus.Repairing, vehicle = car)
                                    }
                                },
                            )
                        }
                        Item(
                            "Vehicle Repaired (clear bay)",
                            onClick = {
                                islandMapViewModel.updateMaintenanceShed {
                                    it.copy(status = MaintenanceShedStatus.Idle, vehicle = null)
                                }
                            },
                        )
                        Item(
                            "Shed Offline (power down)",
                            onClick = {
                                islandMapViewModel.updateMaintenanceShed {
                                    it.copy(
                                        status = MaintenanceShedStatus.Offline,
                                        breakers = List(MAINTENANCE_SHED_BREAKER_COUNT) { false },
                                    )
                                }
                            },
                        )
                        Separator()
                        Item("Reset Shed", onClick = { islandMapViewModel.resetMaintenanceShed() })
                    }
                    // Adjust the tracked Explorer's speed; the status panel's
                    // mph badge and roadway-dot animation follow the model.
                    val tracked = islandMapViewModel.trackedCar
                    Menu("Speed (${tracked.label}: ${tracked.speedMph} mph)") {
                        Item(
                            "Speed Up (+1 mph)",
                            onClick = { islandMapViewModel.setCarSpeed(tracked.id, tracked.speedMph + 1) },
                        )
                        Item(
                            "Speed Down (-1 mph)",
                            enabled = tracked.speedMph > 0,
                            onClick = { islandMapViewModel.setCarSpeed(tracked.id, tracked.speedMph - 1) },
                        )
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
                        // Live security-camera subwindow (plays local CCTV frames).
                        SecurityCamPanel(modifier = Modifier.width(480.dp).height(280.dp))
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
