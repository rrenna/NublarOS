package os.nublar.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.IslandMap
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.MapViewport
import os.nublar.dashboard.ui.map.PaddockEnclosure
import os.nublar.dashboard.ui.map.PaddockShape
import os.nublar.dashboard.viewmodel.MapPreviewViewModel
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarTheme
import os.nublar.designsystem.NublarType
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Standalone debugging harness for the Island Map component — isolated
 * from the rest of Nedryland Monitor so map layers/geometry/icon work can
 * be iterated on without going through the full dashboard flow.
 *
 * Includes a paddock editor: select a paddock, toggle EDIT, drag its vertex
 * handles, then COPY JSON to overwrite
 * dashboard/src/main/resources/data/isla-nublar/paddocks.json.
 *
 * All state and behavior live in [MapPreviewViewModel]; this file is pure UI.
 *
 * Run with: ./gradlew :dashboard:runMapPreview
 */
fun main() {
    // Build the ViewModel (which loads bundled JSON) on a background thread
    // so the first Compose frame isn't blocked by classpath I/O.
    val viewModel = runBlocking {
        withContext(Dispatchers.IO) { MapPreviewViewModel() }
    }

    application {
        val windowState = rememberWindowState(width = 1000.dp, height = 940.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "ISLAND MAP PREVIEW",
            state = windowState,
        ) {
            NublarTheme {
                Column(
                modifier = Modifier.fillMaxSize().background(NublarColors.MonitorGray).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("ISLAND MAP — LAYER PREVIEW", style = NublarType.Header)

                // Layer toggles.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MapLayer.entries.forEach { layer ->
                        Chip(
                            label = layer.name.uppercase(),
                            active = layer in viewModel.activeLayers,
                            onClick = { viewModel.toggleLayer(layer) },
                        )
                    }
                }

                // Editor controls.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Chip(
                        label = if (viewModel.editMode) "EDIT: ON" else "EDIT: OFF",
                        active = viewModel.editMode,
                        onClick = { viewModel.toggleEditMode() },
                    )
                    Chip(
                        label = if (viewModel.panEnabled) "PAN: ON" else "PAN: OFF",
                        active = viewModel.panEnabled,
                        onClick = { viewModel.togglePan() },
                    )
                    Chip(
                        // Copies facilities when a facility is selected, else paddocks.
                        label = if (viewModel.selectedFacilityId != null) "COPY FACILITIES JSON" else "COPY PADDOCKS JSON",
                        active = false,
                        onClick = {
                            Toolkit.getDefaultToolkit().systemClipboard
                                .setContents(StringSelection(viewModel.copyJsonText()), null)
                        },
                    )
                    // Arm/disarm the selected paddock to see the fence disarm
                    // animation (disarming a fence flashes it orange, then fades).
                    val armed = viewModel.selectedPaddock?.armed
                    if (armed != null) {
                        Chip(
                            label = if (armed) "FENCE: ARMED" else "FENCE: UNARMED",
                            active = armed,
                            onClick = { viewModel.toggleSelectedArmed() },
                        )
                    }
                    val paddockLabel = viewModel.selectedPaddock?.label
                    val facilityLabel = viewModel.selectedFacility?.label
                    Text(
                        text = when {
                            facilityLabel != null -> "SELECTED: $facilityLabel (edit mode: drag or arrow keys to move)"
                            paddockLabel != null && viewModel.selectedVertexIndex != null ->
                                "SELECTED: $paddockLabel — node ${viewModel.selectedVertexIndex!! + 1} (arrow keys to nudge)"
                            paddockLabel != null ->
                                "SELECTED: $paddockLabel (edit mode: arrows move paddock, click a node to edit it)"
                            else -> "SELECTED: (none — click a paddock or facility)"
                        },
                        color = NublarColors.LabelCream,
                        style = NublarType.SystemText,
                    )
                }

                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val mapSize = 1400.dp * viewModel.zoom
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        MapViewport(
                            contentWidth = mapSize,
                            contentHeight = mapSize,
                            panEnabled = viewModel.panEnabled,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NublarColors.DarkFrame)
                                .padding(4.dp),
                        ) {
                            IslandMap(
                                modifier = Modifier.size(mapSize, mapSize),
                                activeLayers = viewModel.activeLayers,
                                zoom = viewModel.zoom,
                                facilities = viewModel.facilities,
                                dinosaurs = viewModel.dinosaurs,
                                vehicles = viewModel.vehicles,
                                staff = viewModel.staff,
                                // Hidden paddocks are filtered out here, so they're
                                // neither drawn nor hit-tested (can't swallow taps).
                                paddockShapes = viewModel.visiblePaddocks,
                                selectedPaddockId = viewModel.selectedPaddockId,
                                selectedVertexIndex = viewModel.selectedVertexIndex,
                                selectedFacilityId = viewModel.selectedFacilityId,
                                editMode = viewModel.editMode,
                                onPaddockSelected = { viewModel.selectPaddock(it) },
                                onVertexSelected = { viewModel.selectVertex(it) },
                                onVertexMoved = { paddockId, vertexIndex, newPos ->
                                    viewModel.moveVertex(paddockId, vertexIndex, newPos)
                                },
                                onFacilitySelected = { viewModel.selectFacility(it) },
                                onFacilityMoved = { facilityId, newPos ->
                                    viewModel.moveFacility(facilityId, newPos)
                                },
                            )
                        }

                        // Zoom controls — preview-only overlay, bottom-left of the map.
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            ZoomButton("+") { viewModel.zoomIn() }
                            ZoomButton("−") { viewModel.zoomOut() }
                        }
                    }

                    Column(
                        modifier = Modifier.width(240.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SelectionList(
                            paddocks = viewModel.collection.paddocks,
                            facilities = viewModel.facilities,
                            selectedPaddockId = viewModel.selectedPaddockId,
                            selectedFacilityId = viewModel.selectedFacilityId,
                            hiddenPaddockIds = viewModel.hiddenPaddockIds,
                            onSelectPaddock = { viewModel.selectPaddock(it) },
                            onSelectFacility = { viewModel.selectFacility(it) },
                            onTogglePaddockVisibility = { viewModel.togglePaddockVisibility(it) },
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        )

                        val selectedPaddock = viewModel.selectedPaddock
                        val selectedFacility = viewModel.selectedFacility
                        when {
                            selectedPaddock != null -> PaddockInfoCard(
                                paddock = selectedPaddock,
                                selectedVertexIndex = viewModel.selectedVertexIndex,
                                onAddVertex = { viewModel.addVertexToSelectedPaddock() },
                                onStepVertex = { viewModel.stepSelectedVertex(it) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            selectedFacility != null -> FacilityInfoCard(
                                facility = selectedFacility,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                Text(
                    "Editable paddocks: ${viewModel.collection.paddocks.size}   " +
                        "Editable facilities: ${viewModel.facilities.size}   " +
                        "Dinosaurs: ${viewModel.dinosaurs.size}",
                    color = NublarColors.LabelCream,
                    style = NublarType.SystemText,
                )
            }
            }
        }
    }
}

/** Square zoom in/out button overlaid on the preview map's bottom-left corner. */
@androidx.compose.runtime.Composable
private fun ZoomButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(NublarColors.InsetPanel)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = NublarColors.LabelCream, style = NublarType.Header)
    }
}

@androidx.compose.runtime.Composable
private fun Chip(label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(if (active) NublarColors.StatusGreen else NublarColors.InsetPanel)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (active) NublarColors.ScreenBlack else NublarColors.DarkFrame),
        )
        Text(
            text = "  $label",
            color = NublarColors.LabelCream,
            style = NublarType.SystemText,
        )
    }
}

/**
 * Right-hand list of selectable map items (paddocks + facilities). The selected
 * row highlights (two-way bound with the map: selecting on the map highlights
 * here, and clicking a row selects the item on the map).
 */
@androidx.compose.runtime.Composable
private fun SelectionList(
    paddocks: List<PaddockShape>,
    facilities: List<FacilityMarker>,
    selectedPaddockId: String?,
    selectedFacilityId: String?,
    hiddenPaddockIds: Set<String>,
    onSelectPaddock: (String?) -> Unit,
    onSelectFacility: (String?) -> Unit,
    onTogglePaddockVisibility: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(NublarColors.DarkFrame)
            .padding(6.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text("PADDOCKS", color = NublarColors.LabelCream, style = NublarType.Header)
        paddocks.forEach { p ->
            val hidden = p.id in hiddenPaddockIds
            SelectionRow(
                label = p.label,
                selected = p.id == selectedPaddockId,
                dimmed = hidden,
                onClick = { onSelectPaddock(p.id) },
                trailing = {
                    EyeToggle(hidden = hidden, onClick = { onTogglePaddockVisibility(p.id) })
                },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text("FACILITIES", color = NublarColors.LabelCream, style = NublarType.Header)
        facilities.forEach { f ->
            SelectionRow(f.label, selected = f.id == selectedFacilityId) { onSelectFacility(f.id) }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SelectionRow(
    label: String,
    selected: Boolean,
    dimmed: Boolean = false,
    trailing: (@androidx.compose.runtime.Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) NublarColors.StatusGreen else NublarColors.InsetPanel)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = when {
                selected -> NublarColors.ScreenBlack
                dimmed -> NublarColors.LabelCream.copy(alpha = 0.4f)
                else -> NublarColors.LabelCream
            },
            style = NublarType.SystemText,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/**
 * Show/hide toggle drawn from scratch: an eye outline with a pupil, struck
 * through with a slash when hidden. Its own clickable sits above the row's,
 * so toggling visibility doesn't also select the paddock.
 */
@androidx.compose.runtime.Composable
private fun EyeToggle(hidden: Boolean, onClick: () -> Unit) {
    val tint = if (hidden) NublarColors.LabelCream.copy(alpha = 0.45f) else NublarColors.LabelCream
    Canvas(
        modifier = Modifier
            .size(20.dp)
            .clickable(onClick = onClick),
    ) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f)
        // Almond eye: two arcs mirrored about the horizontal midline.
        val path = Path().apply {
            moveTo(w * 0.12f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.16f, w * 0.88f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.84f, w * 0.12f, h * 0.5f)
            close()
        }
        drawPath(path, color = tint, style = stroke)
        drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.5f))
        if (hidden) {
            drawLine(
                color = tint,
                start = Offset(w * 0.16f, h * 0.82f),
                end = Offset(w * 0.84f, h * 0.18f),
                strokeWidth = w * 0.09f,
            )
        }
    }
}

/** Info card for the selected paddock, with an action to add a vertex. */
@androidx.compose.runtime.Composable
private fun PaddockInfoCard(
    paddock: PaddockShape,
    selectedVertexIndex: Int?,
    onAddVertex: () -> Unit,
    onStepVertex: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(title = paddock.label, modifier = modifier) {
        InfoRow("ID", paddock.id)
        InfoRow("Type", if (paddock.carnivore) "Carnivore" else "Herbivore")
        InfoRow("Enclosure", if (paddock.enclosure == PaddockEnclosure.Building) "Building" else "Fenced")
        InfoRow("Species", paddock.species.joinToString(", ").ifEmpty { "—" })
        // A building is a single-point marker: no fence nodes to step through
        // or add — you drag its icon instead.
        if (paddock.enclosure == PaddockEnclosure.Building) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "No fences — drag the icon (in EDIT mode) or use the arrow keys to move it.",
                color = NublarColors.LabelCream,
                style = NublarType.SystemText,
            )
            return@InfoCard
        }
        InfoRow("Nodes", paddock.vertices.size.toString())
        // Selected-node readout with a prev/next stepper that walks the ring.
        InfoRow("Selected node", selectedVertexIndex?.let { "#${it + 1}" } ?: "(none)") {
            StepButton("<") { onStepVertex(-1) }
            Spacer(Modifier.width(4.dp))
            StepButton(">") { onStepVertex(1) }
        }
        Spacer(Modifier.height(6.dp))
        Chip(label = "+ ADD VERTEX", active = false, onClick = onAddVertex)
        Text(
            text = "Inserts a node after the selected one (or the last edge), " +
                "then selects it — enable EDIT and drag it into place.",
            color = NublarColors.LabelCream,
            style = NublarType.SystemText,
        )
    }
}

/** Info card for the selected facility. */
@androidx.compose.runtime.Composable
private fun FacilityInfoCard(facility: FacilityMarker, modifier: Modifier = Modifier) {
    InfoCard(title = facility.label, modifier = modifier) {
        InfoRow("ID", facility.id)
        InfoRow("Kind", facility.kind.name)
        InfoRow("X", "%.4f".format(facility.position.x))
        InfoRow("Y", "%.4f".format(facility.position.y))
    }
}

/** Shared framed container for the selected-item info cards. */
@androidx.compose.runtime.Composable
private fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @androidx.compose.runtime.Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(NublarColors.InsetPanel)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = NublarColors.LabelCream, style = NublarType.Header)
        Box(Modifier.fillMaxWidth().height(1.dp).background(NublarColors.DarkFrame))
        content()
    }
}

/** A single label/value line inside an [InfoCard], with optional trailing controls. */
@androidx.compose.runtime.Composable
private fun InfoRow(
    label: String,
    value: String,
    trailing: (@androidx.compose.runtime.Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$label:",
            color = NublarColors.LabelCream,
            style = NublarType.SystemText,
            modifier = Modifier.width(96.dp),
        )
        Text(
            value,
            color = NublarColors.StatusGreen,
            style = NublarType.SystemText,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** Compact square stepper button (prev/next node) for an [InfoRow]. */
@androidx.compose.runtime.Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(NublarColors.DarkFrame)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = NublarColors.LabelCream, style = NublarType.SystemText)
    }
}
