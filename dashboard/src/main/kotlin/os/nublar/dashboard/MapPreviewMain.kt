package os.nublar.dashboard

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.FractionalPoint
import os.nublar.dashboard.ui.map.IslandMap
import os.nublar.dashboard.ui.map.MapViewport
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.PaddockShape
import os.nublar.dashboard.ui.map.PaddockVertex
import os.nublar.dashboard.ui.map.SampleMapData
import os.nublar.dashboard.ui.map.facilitiesToJson
import os.nublar.dashboard.ui.map.loadPaddockCollection
import os.nublar.dashboard.ui.map.toJson
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
 * handles, then COPY JSON to overwrite dashboard/src/main/resources/paddocks.json.
 *
 * Run with: ./gradlew :dashboard:runMapPreview
 */
fun main() = application {
    val windowState = rememberWindowState(width = 1000.dp, height = 940.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "ISLAND MAP PREVIEW",
        state = windowState,
    ) {
        NublarTheme {
            var activeLayers by remember {
                mutableStateOf(
                    // Restore persisted layer filters; fall back to all-on for a first run.
                    AppPreferences.mapPreviewLayers
                        ?.mapNotNull { name -> MapLayer.entries.firstOrNull { it.name == name } }
                        ?.toSet()
                        ?: MapLayer.entries.toSet(),
                )
            }
            LaunchedEffect(activeLayers) {
                AppPreferences.mapPreviewLayers = activeLayers.map { it.name }.toSet()
            }
            var collection by remember { mutableStateOf(loadPaddockCollection()) }
            var facilities by remember { mutableStateOf(SampleMapData.facilities) }
            var selectedPaddockId by remember { mutableStateOf<String?>(null) }
            var selectedVertexIndex by remember { mutableStateOf<Int?>(null) }
            var selectedFacilityId by remember { mutableStateOf<String?>(null) }
            var editMode by remember { mutableStateOf(false) }
            var panEnabled by remember { mutableStateOf(true) }

            // Selection is mutually exclusive between a paddock and a facility.
            fun selectPaddock(id: String?) {
                selectedPaddockId = id
                selectedVertexIndex = null
                if (id != null) selectedFacilityId = null
            }
            fun selectFacility(id: String?) {
                selectedFacilityId = id
                if (id != null) {
                    selectedPaddockId = null
                    selectedVertexIndex = null
                }
            }

            // Inserts a new vertex at the midpoint of the edge following the
            // selected node (or the last edge if no node is selected), then
            // selects it so it can be dragged into place.
            fun addVertexToSelectedPaddock() {
                val id = selectedPaddockId ?: return
                val shape = collection.paddocks.firstOrNull { it.id == id } ?: return
                val verts = shape.vertices
                if (verts.size < 2) return
                val i = selectedVertexIndex ?: verts.lastIndex
                val a = verts[i]
                val b = verts[(i + 1) % verts.size]
                val mid = PaddockVertex((a.x + b.x) / 2f, (a.y + b.y) / 2f)
                collection = collection.copy(
                    paddocks = collection.paddocks.map { s ->
                        if (s.id == id) s.copy(vertices = s.vertices.toMutableList().apply { add(i + 1, mid) })
                        else s
                    },
                )
                selectedVertexIndex = i + 1
            }

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
                        val enabled = layer in activeLayers
                        Chip(
                            label = layer.name.uppercase(),
                            active = enabled,
                            onClick = {
                                activeLayers = if (enabled) activeLayers - layer else activeLayers + layer
                            },
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
                        label = if (editMode) "EDIT: ON" else "EDIT: OFF",
                        active = editMode,
                        onClick = { editMode = !editMode },
                    )
                    Chip(
                        label = if (panEnabled) "PAN: ON" else "PAN: OFF",
                        active = panEnabled,
                        onClick = { panEnabled = !panEnabled },
                    )
                    Chip(
                        // Copies facilities when a facility is selected, else paddocks.
                        label = if (selectedFacilityId != null) "COPY FACILITIES JSON" else "COPY PADDOCKS JSON",
                        active = false,
                        onClick = {
                            val json = if (selectedFacilityId != null) {
                                facilities.facilitiesToJson()
                            } else {
                                collection.toJson()
                            }
                            Toolkit.getDefaultToolkit().systemClipboard
                                .setContents(StringSelection(json), null)
                        },
                    )
                    val paddockLabel = collection.paddocks.firstOrNull { it.id == selectedPaddockId }?.label
                    val facilityLabel = facilities.firstOrNull { it.id == selectedFacilityId }?.label
                    Text(
                        text = when {
                            facilityLabel != null -> "SELECTED: $facilityLabel (edit mode: drag or arrow keys to move)"
                            paddockLabel != null && selectedVertexIndex != null ->
                                "SELECTED: $paddockLabel — node ${selectedVertexIndex!! + 1} (arrow keys to nudge)"
                            paddockLabel != null -> "SELECTED: $paddockLabel (edit mode: click a node)"
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
                    MapViewport(
                        contentWidth = 1400.dp,
                        contentHeight = 1400.dp,
                        panEnabled = panEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(NublarColors.DarkFrame)
                            .padding(4.dp),
                    ) {
                        IslandMap(
                            modifier = Modifier.size(1400.dp, 1400.dp),
                            activeLayers = activeLayers,
                            facilities = facilities,
                            dinosaurs = SampleMapData.dinosaurs,
                            vehicles = SampleMapData.vehicles,
                            staff = SampleMapData.staff,
                            paddockShapes = collection.paddocks,
                            selectedPaddockId = selectedPaddockId,
                            selectedVertexIndex = selectedVertexIndex,
                            selectedFacilityId = selectedFacilityId,
                            editMode = editMode,
                            onPaddockSelected = { selectPaddock(it) },
                            onVertexSelected = { selectedVertexIndex = it },
                            onVertexMoved = { paddockId, vertexIndex, newPos ->
                                collection = collection.copy(
                                    paddocks = collection.paddocks.map { shape ->
                                        if (shape.id != paddockId) return@map shape
                                        shape.copy(
                                            vertices = shape.vertices.mapIndexed { i, v ->
                                                if (i == vertexIndex) newPos.toVertex() else v
                                            },
                                        )
                                    },
                                )
                            },
                            onFacilitySelected = { selectFacility(it) },
                            onFacilityMoved = { facilityId, newPos ->
                                facilities = facilities.map { f ->
                                    if (f.id == facilityId) f.copy(position = newPos) else f
                                }
                            },
                        )
                    }

                    Column(
                        modifier = Modifier.width(240.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SelectionList(
                            paddocks = collection.paddocks,
                            facilities = facilities,
                            selectedPaddockId = selectedPaddockId,
                            selectedFacilityId = selectedFacilityId,
                            onSelectPaddock = { selectPaddock(it) },
                            onSelectFacility = { selectFacility(it) },
                            modifier = Modifier.fillMaxWidth().weight(1f),
                        )

                        val selectedPaddock = collection.paddocks.firstOrNull { it.id == selectedPaddockId }
                        val selectedFacility = facilities.firstOrNull { it.id == selectedFacilityId }
                        when {
                            selectedPaddock != null -> PaddockInfoCard(
                                paddock = selectedPaddock,
                                selectedVertexIndex = selectedVertexIndex,
                                onAddVertex = { addVertexToSelectedPaddock() },
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
                    "Editable paddocks: ${collection.paddocks.size}   " +
                        "Editable facilities: ${facilities.size}   " +
                        "Dinosaurs: ${SampleMapData.dinosaurs.size}",
                    color = NublarColors.LabelCream,
                    style = NublarType.SystemText,
                )
            }
        }
    }
}

private fun FractionalPoint.toVertex() = PaddockVertex(x, y)

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
    onSelectPaddock: (String?) -> Unit,
    onSelectFacility: (String?) -> Unit,
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
            SelectionRow(p.label, selected = p.id == selectedPaddockId) { onSelectPaddock(p.id) }
        }
        Spacer(Modifier.height(10.dp))
        Text("FACILITIES", color = NublarColors.LabelCream, style = NublarType.Header)
        facilities.forEach { f ->
            SelectionRow(f.label, selected = f.id == selectedFacilityId) { onSelectFacility(f.id) }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) NublarColors.StatusGreen else NublarColors.InsetPanel)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            color = if (selected) NublarColors.ScreenBlack else NublarColors.LabelCream,
            style = NublarType.SystemText,
        )
    }
}

/** Info card for the selected paddock, with an action to add a vertex. */
@androidx.compose.runtime.Composable
private fun PaddockInfoCard(
    paddock: PaddockShape,
    selectedVertexIndex: Int?,
    onAddVertex: () -> Unit,
    modifier: Modifier = Modifier,
) {
    InfoCard(title = paddock.label, modifier = modifier) {
        InfoRow("ID", paddock.id)
        InfoRow("Type", if (paddock.carnivore) "Carnivore" else "Herbivore")
        InfoRow("Species", paddock.species.joinToString(", ").ifEmpty { "—" })
        InfoRow("Nodes", paddock.vertices.size.toString())
        InfoRow(
            "Selected node",
            selectedVertexIndex?.let { "#${it + 1}" } ?: "(none)",
        )
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

/** A single label/value line inside an [InfoCard]. */
@androidx.compose.runtime.Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    }
}
