package os.nublar.dashboard.ui.screens

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.dashboard.data.DockStatus
import os.nublar.dashboard.data.EastDockState
import os.nublar.dashboard.data.GateStatus
import os.nublar.dashboard.data.HelipadState
import os.nublar.dashboard.data.HelipadStatus
import os.nublar.dashboard.data.LoadingBayState
import os.nublar.dashboard.data.MaintenanceShedState
import os.nublar.dashboard.data.MaintenanceShedStatus
import os.nublar.dashboard.data.TourCar
import os.nublar.dashboard.data.VisitorCenterState
import os.nublar.dashboard.data.VisitorCenterStatus
import os.nublar.dashboard.ui.VehicleImage
import os.nublar.dashboard.ui.VehicleInfoPanel
import os.nublar.dashboard.ui.map.FacilityStatusInfo
import os.nublar.dashboard.ui.map.IslandMap
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.MapViewport
import os.nublar.dashboard.viewmodel.IslandMapViewModel
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarFonts
import os.nublar.designsystem.rememberBlinkOn

/**
 * Close recreation of the film's "Raptor Paddock" island-map / vehicle
 * status screen. See README "Legal and Asset Guidelines" for the
 * fair-use/parody basis for this direct recreation. Redrawn from scratch —
 * an original terrain/paddock-boundary drawing and vehicle diagram in the
 * same visual style, not a traced copy or extracted film frame.
 */
@Composable
fun IslandMapView(
    onClose: () -> Unit,
    onSwitchScreen: () -> Unit = {},
    splitFraction: Float = 0.535f,
    onSplitFractionChange: (Float) -> Unit = {},
    viewModel: IslandMapViewModel,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NublarColors.MonitorGray)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DraggableSplitRow(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                splitFraction = splitFraction,
                onSplitFractionChange = onSplitFractionChange,
                left = { m -> PaddockMapPanel(viewModel = viewModel, modifier = m) },
                right = { m -> IslandRightColumn(modifier = m, onClose = onClose, viewModel = viewModel) },
            )
            BottomBar(screenLabel = "Animal Paddocks", onScreenClick = onSwitchScreen)
        }
    }
}

@Composable
private fun PaddockMapPanel(viewModel: IslandMapViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        MapViewport(
            contentWidth = 1200.dp,
            contentHeight = 1200.dp,
            panEnabled = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(NublarColors.DarkFrame)
                .padding(4.dp),
        ) {
            IslandMap(
                modifier = Modifier.size(1200.dp, 1200.dp),
                // Dinosaurs, Staff, and Vehicles layers off by default on this screen.
                activeLayers = MapLayer.entries.toSet() - MapLayer.Dinosaurs - MapLayer.Staff - MapLayer.Vehicles,
                paddockShapes = viewModel.paddocks,
                facilities = viewModel.facilities,
                dinosaurs = viewModel.dinosaurs,
                vehicles = viewModel.vehicles,
                staff = viewModel.staff,
                selectedPaddockId = viewModel.selectedPaddockId,
                onPaddockSelected = { viewModel.selectPaddock(it) },
                selectedFacilityId = viewModel.selectedFacilityId,
                onFacilitySelected = { viewModel.selectFacility(it) },
                helicopterPosition = viewModel.helicopter.mapPosition,
                facilityStatus = { id -> facilityStatusFor(viewModel, id) },
            )
        }
        PaddockLevelBar()
    }
}

/**
 * Live status for a facility marker, derived from the East Dock / Helipad
 * models. Appended to the marker's hover tooltip and used to tint its icon on
 * the map, so occupancy reads at a glance without hovering or selecting.
 * Facilities with no backing status model (most of them) return null.
 */
private fun facilityStatusFor(viewModel: IslandMapViewModel, facilityId: String): FacilityStatusInfo? =
    when (facilityId) {
        "east-dock" -> {
            val dock = viewModel.eastDock
            when (dock.status) {
                DockStatus.Empty -> FacilityStatusInfo("BERTH EMPTY")
                DockStatus.Docking -> FacilityStatusInfo("SHIP APPROACHING", NublarColors.HighlightYellow)
                DockStatus.Docked -> FacilityStatusInfo(
                    "DOCKED — ${dock.ship?.name ?: "unknown vessel"}",
                    NublarColors.StatusGreen,
                )
                DockStatus.Departing -> FacilityStatusInfo("DEPARTING", NublarColors.HighlightYellow)
            }
        }
        "helipad" -> {
            val pad = viewModel.helipad
            when (pad.status) {
                HelipadStatus.Empty -> FacilityStatusInfo("PAD EMPTY")
                HelipadStatus.Landing -> FacilityStatusInfo("HELICOPTER LANDING", NublarColors.HighlightYellow)
                HelipadStatus.Occupied -> FacilityStatusInfo(
                    "OCCUPIED — ${pad.helicopter?.callsign ?: "unknown aircraft"}",
                    NublarColors.StatusGreen,
                )
                HelipadStatus.Departing -> FacilityStatusInfo("DEPARTING", NublarColors.HighlightYellow)
            }
        }
        "visitor-center" -> {
            val center = viewModel.visitorCenter
            val occupancy = if (center.occupied) "staff on site" else "unoccupied"
            when (center.status) {
                VisitorCenterStatus.Operational -> FacilityStatusInfo(
                    "OPERATIONAL — $occupancy",
                    NublarColors.StatusGreen,
                )
                VisitorCenterStatus.Lockdown -> FacilityStatusInfo("LOCKDOWN — $occupancy", NublarColors.HighlightYellow)
                VisitorCenterStatus.PowerFailure -> FacilityStatusInfo(
                    "POWER FAILURE — $occupancy",
                    NublarColors.WarningRed,
                )
            }
        }
        "maintenance-shed" -> {
            val shed = viewModel.maintenanceShed
            when (shed.status) {
                MaintenanceShedStatus.Idle -> FacilityStatusInfo("BAY IDLE")
                MaintenanceShedStatus.Repairing -> FacilityStatusInfo(
                    "REPAIRING — ${shed.vehicle?.label ?: "unknown vehicle"}",
                    NublarColors.HighlightYellow,
                )
                MaintenanceShedStatus.Offline -> FacilityStatusInfo("OFFLINE", NublarColors.WarningRed)
            }
        }
        else -> null
    }


@Composable
private fun PaddockLevelBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NublarColors.HighlightYellow)
            .border(1.dp, Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PaddockLevelBadge()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Raptor Paddock",
                    color = Color.White,
                    fontFamily = NublarFonts.Display,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 19.sp,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Quadrant: tecotsky9087",
                    color = Color.White,
                    fontFamily = NublarFonts.Ui,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }
        PaddockLevelBadge()
    }
}

@Composable
private fun PaddockLevelBadge() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text("LEVEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.width(56.dp).background(Color.Black).padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("G", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun IslandRightColumn(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    viewModel: IslandMapViewModel,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // The top-right panel swaps to whichever selection it's relevant to:
        // the Raptor Paddock's loading-bay status, the East Dock's berth
        // status, the Helipad's landing status, the Visitor Center's
        // operational status, the Maintenance Shed's repair status, or (with
        // nothing special selected) the usual vehicle status.
        val raptorSelected = viewModel.selectedPaddockId == "raptor-paddock"
        val eastDockSelected = viewModel.selectedFacilityId == "east-dock"
        val helipadSelected = viewModel.selectedFacilityId == "helipad"
        val visitorCenterSelected = viewModel.selectedFacilityId == "visitor-center"
        val maintenanceShedSelected = viewModel.selectedFacilityId == "maintenance-shed"
        when {
            raptorSelected -> {
                TabRow(label = "PADDOCK", tabs = listOf("BAY", "FEED", "VITALS"))
                RaptorBayPanel(bay = viewModel.loadingBay, modifier = Modifier.weight(1f).fillMaxWidth())
            }
            eastDockSelected -> {
                TabRow(label = "FACILITY", tabs = listOf("DOCK", "CARGO", "CREW"))
                EastDockPanel(dock = viewModel.eastDock, modifier = Modifier.weight(1f).fillMaxWidth())
            }
            helipadSelected -> {
                TabRow(label = "FACILITY", tabs = listOf("PAD", "FUEL", "CREW"))
                HelipadPanel(pad = viewModel.helipad, modifier = Modifier.weight(1f).fillMaxWidth())
            }
            visitorCenterSelected -> {
                TabRow(label = "FACILITY", tabs = listOf("POWER", "GUESTS", "SECURITY"))
                VisitorCenterPanel(center = viewModel.visitorCenter, modifier = Modifier.weight(1f).fillMaxWidth())
            }
            maintenanceShedSelected -> {
                TabRow(label = "FACILITY", tabs = listOf("BAY", "PARTS", "CREW"))
                MaintenanceShedPanel(
                    shed = viewModel.maintenanceShed,
                    onToggleBreaker = { viewModel.toggleMaintenanceShedBreaker(it) },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            }
            else -> {
                TabRow(label = "VEHICLE", tabs = listOf("TOUR", "POWER", "TIME"))
                VehicleStatusPanel(car = viewModel.trackedCar, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
        Column(
            // Beveled outer panel around the whole cluster (matches the film
            // screen): a RECESSED frame (dark top/left, light bottom/right) with
            // the gray field showing around the keys, which stay raised.
            modifier = Modifier
                .background(NublarColors.MonitorGray)
                .bevelBorder(raised = false, width = 2.dp)
                .padding(3.dp),
        ) {
            Row {
                ChunkyButton("HOLD", modifier = Modifier.weight(1f))
                ChunkyButton("QUIT", modifier = Modifier.weight(1f), onClick = onClose)
                ChunkyButton("NEW", modifier = Modifier.weight(1f))
            }
            Row {
                // NEXT spans the same width as HOLD above (1/3 of the row): weight 2
                // vs. the four playback keys at weight 1 each -> 2/6 = 1/3.
                ChunkyButton("NEXT", modifier = Modifier.weight(2f))
                ChunkyButton("◄◄", modifier = Modifier.weight(1f))
                ChunkyButton("►►", modifier = Modifier.weight(1f))
                ChunkyButton("►", modifier = Modifier.weight(1f), highlight = true)
                ChunkyButton("■", modifier = Modifier.weight(1f))
            }
        }
        TabRow(label = "GLITCHES", tabs = listOf("MAPS", "SYSTEM", "EMERG."))
        QuadrantLog(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            selectedPaddockId = viewModel.selectedPaddockId,
            paddockIdForName = viewModel::paddockIdForName,
            onSelectPaddock = viewModel::selectPaddock,
        )
    }
}

@Composable
private fun VehicleStatusPanel(car: TourCar, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("vehicle status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (car.headlightsOn) "headlights on" else "headlights off",
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (car.headlightsOn) NublarColors.StatusGreen else NublarColors.InsetPanel),
                )
            }
        }

        // Upper vehicle-status area, layered bottom-to-top: the roadway stripe,
        // the top-down Explorer image over its center, then warning overlays.
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            VehicleRoadway(
                headlightsOn = car.headlightsOn,
                speedMph = car.speedMph,
                modifier = Modifier.matchParentSize(),
            )
            VehicleImage(
                assetName = "vehicle_explorer_top.png",
                contentDescription = "Top-down view of Ford Explorer",
                maxWidthRatio = 0.61f,
                showShadow = true,
                modifier = Modifier.matchParentSize(),
            )
            VehicleWarnings(modifier = Modifier.matchParentSize())
        }

        // Speed readout + alert banner, matching the film UI: a black-oval
        // "12 mph" badge and the red "Not Responding" flag to its right.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                ) {
                    Text(
                        "${car.speedMph}",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp,
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "mph",
                    color = NublarColors.DarkFrame,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 14.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (!car.responding) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE04A38))
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    Text(
                        "Not Responding",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontStyle = FontStyle.Italic,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // Lower vehicle-information panel: combined front + side Explorer graphic
        // with the description overlaid between the two views, and the EXP
        // readout overlaid ON TOP of the image, left-aligned.
        VehicleInfoPanel(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                listOf(
                    "EXP 4" to NublarColors.StatusGreen,
                    "EXP 5" to NublarColors.StatusGreen,
                    "EXP 6" to NublarColors.WarningRed,
                    "EXP 7" to NublarColors.MapBlue,
                ).forEach { (label, color) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(color))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.background(Color.Black).padding(horizontal = 6.dp, vertical = 1.dp)) {
                            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Loading-bay status for the raptor pen, shown in place of the vehicle status
 * while the Raptor Paddock is selected. Renders the gate / loader / lock state
 * from [LoadingBayState] plus the red alert banner — driven by the show-sync
 * opening-scene script or the Events -> Raptor Pen menu.
 */
@Composable
private fun RaptorBayPanel(bay: LoadingBayState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("loading bay status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "RAPTOR PEN",
                    color = NublarColors.WarningRed,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }

        val gateColor = when (bay.gate) {
            GateStatus.Closed -> NublarColors.StatusGreen
            GateStatus.Opening -> NublarColors.HighlightYellow
            GateStatus.Open -> NublarColors.WarningRed
        }
        BayStatusRow("LOADING GATE", bay.gate.label, gateColor)
        BayStatusRow(
            "LOADER",
            if (bay.loaderOffsetMeters <= 0) "DOCKED" else "PUSHED BACK ${bay.loaderOffsetMeters}m",
            if (bay.loaderOffsetMeters <= 0) NublarColors.StatusGreen else NublarColors.WarningRed,
        )
        LockStatusRow(engaged = bay.lockEngaged)

        Spacer(modifier = Modifier.weight(1f))
        if (bay.alert != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE04A38))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    bay.alert,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

/**
 * The loading-lock line: DISENGAGED (red) when open; when the lock flips to
 * engaged it plays an ENGAGING progress bar filling over ~2.5s, then settles
 * on the LOCKED chip. A lock that's already engaged on first show (e.g. the
 * bay's initial state) displays LOCKED without animating.
 */
@Composable
private fun LockStatusRow(engaged: Boolean) {
    var progress by remember { mutableStateOf(if (engaged) 1f else 0f) }
    var prevEngaged by remember { mutableStateOf(engaged) }
    LaunchedEffect(engaged) {
        val was = prevEngaged
        prevEngaged = engaged
        when {
            !engaged -> progress = 0f
            was -> progress = 1f      // already locked (no transition to animate)
            else -> animate(0f, 1f, animationSpec = tween(2500)) { v, _ -> progress = v }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text("LOADING LOCK", color = Color.White, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 11.sp)
        }
        when {
            !engaged -> Box(
                modifier = Modifier.background(NublarColors.WarningRed).padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text("DISENGAGED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
            progress < 1f -> Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(20.dp)
                    .background(Color.Black)
                    .border(1.dp, NublarColors.HighlightYellow),
                contentAlignment = Alignment.Center,
            ) {
                // Fill tracks the engage progress under the label.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(NublarColors.HighlightYellow.copy(alpha = 0.85f)),
                )
                Text(
                    "ENGAGING ${(progress * 100).toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                )
            }
            else -> Box(
                modifier = Modifier.background(NublarColors.StatusGreen).padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text("LOCKED", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}

/** One label/value status line in the loading-bay panel. */
@Composable
private fun BayStatusRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .background(Color.Black)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, fontSize = 11.sp)
        }
        Box(modifier = Modifier.background(color).padding(horizontal = 10.dp, vertical = 3.dp)) {
            Text(value, color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}

/**
 * East Dock status, shown in place of the vehicle status while the East Dock
 * facility is selected. Renders the berth status and which ship (if any) is
 * present, from [EastDockState] — driven by the Events -> East Dock menu (or,
 * later, a show-sync script).
 */
@Composable
private fun EastDockPanel(dock: EastDockState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("east dock status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "MOORING",
                    color = NublarColors.MapBlue,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }

        val statusColor = when (dock.status) {
            DockStatus.Empty -> NublarColors.LabelCream
            DockStatus.Docking, DockStatus.Departing -> NublarColors.HighlightYellow
            DockStatus.Docked -> NublarColors.StatusGreen
        }
        BayStatusRow("BERTH STATUS", dock.status.label, statusColor)
        BayStatusRow(
            "SHIP PRESENT",
            dock.ship?.name ?: "NONE",
            if (dock.ship != null) NublarColors.StatusGreen else NublarColors.LabelCream,
        )
        dock.ship?.let { ship ->
            BayStatusRow("ORIGIN", ship.origin.uppercase(), NublarColors.LabelCream)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Helipad status, shown in place of the vehicle status while the Helipad
 * facility is selected. Renders the pad status and which helicopter (if any)
 * is landing or present, from [HelipadState] — driven by the Events ->
 * Helipad menu (or, later, a show-sync script).
 */
@Composable
private fun HelipadPanel(pad: HelipadState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("helipad status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "AIRSPACE",
                    color = NublarColors.MapBlue,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }

        val statusColor = when (pad.status) {
            HelipadStatus.Empty -> NublarColors.LabelCream
            HelipadStatus.Landing, HelipadStatus.Departing -> NublarColors.HighlightYellow
            HelipadStatus.Occupied -> NublarColors.StatusGreen
        }
        BayStatusRow("PAD STATUS", pad.status.label, statusColor)
        BayStatusRow(
            "HELICOPTER",
            pad.helicopter?.callsign ?: "NONE",
            if (pad.helicopter != null) NublarColors.StatusGreen else NublarColors.LabelCream,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Visitor Center status, shown in place of the vehicle status while that
 * facility is selected. Renders operational status and on-site occupancy from
 * [VisitorCenterState] — driven by the Events -> Visitor Center menu (or,
 * later, a show-sync script).
 */
@Composable
private fun VisitorCenterPanel(center: VisitorCenterState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("visitor center status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "MAIN BUILDING",
                    color = NublarColors.MapBlue,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }

        val statusColor = when (center.status) {
            VisitorCenterStatus.Operational -> NublarColors.StatusGreen
            VisitorCenterStatus.Lockdown -> NublarColors.HighlightYellow
            VisitorCenterStatus.PowerFailure -> NublarColors.WarningRed
        }
        BayStatusRow("STATUS", center.status.label, statusColor)
        BayStatusRow(
            "OCCUPANCY",
            if (center.occupied) "STAFF ON SITE" else "UNOCCUPIED",
            if (center.occupied) NublarColors.StatusGreen else NublarColors.LabelCream,
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Maintenance Shed status, shown in place of the vehicle status while that
 * facility is selected. Renders repair status and which tour vehicle (if any)
 * is in the bay, from [MaintenanceShedState] — driven by the Events ->
 * Maintenance Shed menu (or, later, a show-sync script).
 */
@Composable
private fun MaintenanceShedPanel(
    shed: MaintenanceShedState,
    onToggleBreaker: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("maintenance shed status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    "GARAGE",
                    color = NublarColors.MapBlue,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }

        val statusColor = when (shed.status) {
            MaintenanceShedStatus.Idle -> NublarColors.LabelCream
            MaintenanceShedStatus.Repairing -> NublarColors.HighlightYellow
            MaintenanceShedStatus.Offline -> NublarColors.WarningRed
        }
        BayStatusRow("STATUS", shed.status.label, statusColor)
        BayStatusRow(
            "VEHICLE IN BAY",
            shed.vehicle?.label ?: "NONE",
            if (shed.vehicle != null) NublarColors.StatusGreen else NublarColors.LabelCream,
        )

        // While the shed is dark, an original "restore power one switch at a
        // time" breaker panel: every breaker must be pushed closed by hand —
        // the shed comes back online automatically once they all are.
        if (shed.status == MaintenanceShedStatus.Offline) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "POWER PANEL — CLOSE EVERY BREAKER",
                color = NublarColors.WarningRed,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 11.sp,
            )
            BreakerPanel(breakers = shed.breakers, onToggle = onToggleBreaker, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/** A small grid of physical-looking breaker switches, four to a row. */
@Composable
private fun BreakerPanel(breakers: List<Boolean>, onToggle: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.DarkFrame)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        breakers.chunked(4).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEachIndexed { colIndex, closed ->
                    val index = rowIndex * 4 + colIndex
                    BreakerSwitch(
                        number = index + 1,
                        closed = closed,
                        onClick = { onToggle(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** A single toggleable breaker: dark/raised when open, green/pressed-in when closed. */
@Composable
private fun BreakerSwitch(number: Int, closed: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(if (closed) NublarColors.StatusGreen else NublarColors.MonitorGray)
            .bevelBorder(raised = !closed)
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "$number",
            color = if (closed) NublarColors.ScreenBlack else NublarColors.LabelCream,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
        )
    }
}

/**
 * Backdrop for the vehicle-status area: a headlight beam and the horizontal
 * roadway stripe. Drawn full-width so the stripe stays visible on both sides of
 * the vehicle image, which covers its center.
 */
@Composable
private fun VehicleRoadway(headlightsOn: Boolean, speedMph: Int, modifier: Modifier = Modifier) {
    // Scroll phase (in stripe-dot spacings) for the roadway dots. Advances only
    // while the car is moving, at a rate proportional to its speed, so the dots
    // read as the road streaming past; at 0 mph they freeze in place.
    var dotPhase by remember { mutableStateOf(0f) }
    LaunchedEffect(speedMph) {
        if (speedMph <= 0) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            dotPhase = (dotPhase + (now - last) / 1_000_000_000f * speedMph * 0.1125f) % 1f
            last = now
        }
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2f

        // Headlight beams (front of the vehicle points left): two cones fanning
        // out from the front corners — one up-left, one down-left — like the
        // film UI, overlapping near the bumper. Only drawn while the tracked
        // car's headlights are on.
        if (headlightsOn) {
            val beamColor = Color.White.copy(alpha = 0.30f)
            val beamStartX = w * 0.30f
            val upperBeam = Path().apply {
                moveTo(beamStartX, cy - h * 0.10f)
                lineTo(w * 0.02f, cy - h * 0.42f)
                lineTo(w * 0.02f, cy - h * 0.06f)
                lineTo(beamStartX, cy - h * 0.02f)
                close()
            }
            val lowerBeam = Path().apply {
                moveTo(beamStartX, cy + h * 0.10f)
                lineTo(w * 0.02f, cy + h * 0.42f)
                lineTo(w * 0.02f, cy + h * 0.06f)
                lineTo(beamStartX, cy + h * 0.02f)
                close()
            }
            drawPath(upperBeam, color = beamColor)
            drawPath(lowerBeam, color = beamColor)
        }

        // Roadway stripe across the full width: 2x the original thickness,
        // shifted halfway toward yellow from the cream base.
        val stripeWidth = h * 0.069f
        val stripeColor = lerp(NublarColors.LabelCream, Color.Yellow, 0.7f).copy(alpha = 0.6f)
        drawLine(
            stripeColor,
            Offset(0f, cy),
            Offset(w, cy),
            strokeWidth = stripeWidth,
        )

        // White dots riding the stripe, scrolling left -> right with the car's
        // speed (dotPhase advances only while moving).
        val spacing = w * 0.11f
        val dotRadius = stripeWidth * 0.30f
        var x = (dotPhase % 1f) * spacing
        while (x < w) {
            drawCircle(Color.White, radius = dotRadius, center = Offset(x, cy))
            x += spacing
        }
    }
}

/** Warning markers overlaid ON TOP of the vehicle image. */
@Composable
private fun VehicleWarnings(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        drawCircle(NublarColors.WarningRed, radius = h * 0.03f, center = Offset(w * 0.94f, cy - h * 0.20f))
        drawCircle(NublarColors.WarningRed, radius = h * 0.03f, center = Offset(w * 0.94f, cy + h * 0.20f))
    }
}

private data class QuadrantEntry(val quadrant: String, val paddock: String, val failed: Boolean)

@Composable
private fun QuadrantLog(
    modifier: Modifier = Modifier,
    selectedPaddockId: String? = null,
    // Maps a glitch row's paddock name to a defined paddock id (null = no match).
    paddockIdForName: (String) -> String? = { null },
    onSelectPaddock: (String?) -> Unit = {},
) {
    val entries = listOf(
        QuadrantEntry("qp 81", "Gallimimus Paddock", failed = true),
        QuadrantEntry("qp 82", "Reserve Paddock", failed = true),
        QuadrantEntry("qp 83", "Reserve Paddock", failed = false),
        QuadrantEntry("qp 84", "Brachiosaurus Paddock", failed = false),
        QuadrantEntry("qp 85", "Triceratops Paddock", failed = false),
        QuadrantEntry("qp 86", "Tyrannosaurus Paddock", failed = false),
        QuadrantEntry("qp 87", "Raptor Paddock", failed = false),
        QuadrantEntry("qp 88", "Aviary Sector", failed = false),
    )
    Column(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
            .bevelBorder(raised = false, width = 2.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        entries.forEach { entry ->
            // Only rows whose paddock name matches a defined paddock are
            // tappable; others (e.g. "Aviary Sector") render as plain text.
            val matchedId = paddockIdForName(entry.paddock)
            val isSelected = matchedId != null && matchedId == selectedPaddockId
            val rowModifier = if (matchedId != null) {
                Modifier
                    .fillMaxWidth()
                    .then(if (isSelected) Modifier.background(NublarColors.HighlightYellow) else Modifier)
                    .clickable { onSelectPaddock(if (isSelected) null else matchedId) }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .padding(vertical = 1.dp)
            } else {
                Modifier.fillMaxWidth()
            }
            Row(modifier = rowModifier, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .background(if (entry.failed) NublarColors.WarningRed else NublarColors.StatusGreen)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (entry.failed) "FAILED" else "CLEAR",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Quadrant: ${entry.quadrant} ${entry.paddock}",
                    // Matched (linked) rows read as a link; unmatched stay plain black.
                    color = if (matchedId != null) NublarColors.MapBlue else Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
