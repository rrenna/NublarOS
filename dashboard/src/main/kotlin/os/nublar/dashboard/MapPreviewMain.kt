package os.nublar.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
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
import os.nublar.dashboard.ui.map.FractionalPoint
import os.nublar.dashboard.ui.map.IslandMap
import os.nublar.dashboard.ui.map.MapViewport
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.PaddockVertex
import os.nublar.dashboard.ui.map.SampleMapData
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
            var activeLayers by remember { mutableStateOf(MapLayer.entries.toSet()) }
            var collection by remember { mutableStateOf(loadPaddockCollection()) }
            var selectedPaddockId by remember { mutableStateOf<String?>(null) }
            var selectedVertexIndex by remember { mutableStateOf<Int?>(null) }
            var editMode by remember { mutableStateOf(false) }
            var panEnabled by remember { mutableStateOf(true) }

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
                        label = "COPY JSON",
                        active = false,
                        onClick = {
                            val json = collection.toJson()
                            Toolkit.getDefaultToolkit().systemClipboard
                                .setContents(StringSelection(json), null)
                        },
                    )
                    val selectedLabel = collection.paddocks.firstOrNull { it.id == selectedPaddockId }?.label
                    Text(
                        text = when {
                            selectedLabel != null && selectedVertexIndex != null ->
                                "SELECTED: $selectedLabel — node ${selectedVertexIndex!! + 1} (arrow keys to nudge)"
                            selectedLabel != null -> "SELECTED: $selectedLabel (edit mode: click a node)"
                            else -> "SELECTED: (none — click a paddock)"
                        },
                        color = NublarColors.LabelCream,
                        style = NublarType.SystemText,
                    )
                }

                MapViewport(
                    contentWidth = 1400.dp,
                    contentHeight = 1400.dp,
                    panEnabled = panEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(NublarColors.DarkFrame)
                        .padding(4.dp),
                ) {
                    IslandMap(
                        modifier = Modifier.size(1400.dp, 1400.dp),
                        activeLayers = activeLayers,
                        facilities = SampleMapData.facilities,
                        dinosaurs = SampleMapData.dinosaurs,
                        vehicles = SampleMapData.vehicles,
                        staff = SampleMapData.staff,
                        paddockShapes = collection.paddocks,
                        selectedPaddockId = selectedPaddockId,
                        selectedVertexIndex = selectedVertexIndex,
                        editMode = editMode,
                        onPaddockSelected = {
                            selectedPaddockId = it
                            selectedVertexIndex = null
                        },
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
                    )
                }

                Text(
                    "Editable paddocks: ${collection.paddocks.size}   " +
                        "Dinosaurs: ${SampleMapData.dinosaurs.size}   " +
                        "Facilities: ${SampleMapData.facilities.size}",
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
