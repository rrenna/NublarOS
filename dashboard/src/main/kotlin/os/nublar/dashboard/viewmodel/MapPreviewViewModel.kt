package os.nublar.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import os.nublar.dashboard.AppPreferences
import os.nublar.dashboard.data.BundledIslaNublarRepository
import os.nublar.dashboard.data.IslaNublarRepository
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.FractionalPoint
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.PaddockShape
import os.nublar.dashboard.ui.map.PaddockVertex
import os.nublar.dashboard.ui.map.facilitiesToJson
import os.nublar.dashboard.ui.map.toJson

/**
 * ViewModel for the standalone Island Map Preview harness: layer filters
 * (persisted across launches), the editable paddock/facility collections,
 * selection state (mutually exclusive between a paddock and a facility),
 * edit/pan toggles, and zoom.
 */
class MapPreviewViewModel(
    repository: IslaNublarRepository = BundledIslaNublarRepository(),
    // Persistence is injected (defaulting to AppPreferences) so unit tests
    // can exercise the ViewModel without touching the user's real prefs.
    restoreLayers: () -> Set<String>? = { AppPreferences.mapPreviewLayers },
    private val persistLayers: (Set<String>) -> Unit = { AppPreferences.mapPreviewLayers = it },
) {
    var activeLayers by mutableStateOf(
        // Restore persisted layer filters; fall back to all-on for a first run.
        restoreLayers()
            ?.mapNotNull { name -> MapLayer.entries.firstOrNull { it.name == name } }
            ?.toSet()
            ?: MapLayer.entries.toSet(),
    )
        private set

    var collection by mutableStateOf(repository.paddocks())
        private set

    var facilities by mutableStateOf(repository.facilities())
        private set

    val dinosaurs = repository.dinosaurs()
    val vehicles = repository.vehicles()
    val staff = repository.staff()

    var selectedPaddockId by mutableStateOf<String?>(null)
        private set

    /** Paddocks the user has hidden via the list's eye toggle. */
    var hiddenPaddockIds by mutableStateOf<Set<String>>(emptySet())
        private set

    /**
     * Paddocks to hand the map. Hidden ones are filtered out entirely rather
     * than skipped at draw time, so they're also excluded from hit-testing —
     * a hidden paddock can't be clicked, dragged, or swallow a tap.
     */
    val visiblePaddocks: List<PaddockShape>
        get() = collection.paddocks.filter { it.id !in hiddenPaddockIds }

    var selectedVertexIndex by mutableStateOf<Int?>(null)
        private set

    var selectedFacilityId by mutableStateOf<String?>(null)
        private set

    var editMode by mutableStateOf(false)
        private set

    var panEnabled by mutableStateOf(true)
        private set

    var zoom by mutableStateOf(1f)
        private set

    val selectedPaddock: PaddockShape?
        get() = collection.paddocks.firstOrNull { it.id == selectedPaddockId }

    val selectedFacility: FacilityMarker?
        get() = facilities.firstOrNull { it.id == selectedFacilityId }

    fun toggleLayer(layer: MapLayer) {
        activeLayers = if (layer in activeLayers) activeLayers - layer else activeLayers + layer
        persistLayers(activeLayers.map { it.name }.toSet())
    }

    fun toggleEditMode() {
        editMode = !editMode
    }

    /** Hiding the selected paddock also clears its selection — it's no longer on the map. */
    fun togglePaddockVisibility(id: String) {
        hiddenPaddockIds = if (id in hiddenPaddockIds) hiddenPaddockIds - id else hiddenPaddockIds + id
        if (id in hiddenPaddockIds && selectedPaddockId == id) selectPaddock(null)
    }

    fun togglePan() {
        panEnabled = !panEnabled
    }

    fun zoomIn() {
        zoom = (zoom * 1.25f).coerceAtMost(3f)
    }

    fun zoomOut() {
        zoom = (zoom / 1.25f).coerceAtLeast(0.5f)
    }

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

    fun selectVertex(index: Int?) {
        selectedVertexIndex = index
    }

    /**
     * Steps the selected node around the selected paddock's ring, wrapping at
     * both ends. With no node selected yet, stepping forward starts at the
     * first node and backward at the last.
     */
    fun stepSelectedVertex(delta: Int) {
        val count = selectedPaddock?.vertices?.size ?: return
        if (count == 0) return
        val current = selectedVertexIndex
        selectedVertexIndex = if (current == null) {
            if (delta >= 0) 0 else count - 1
        } else {
            ((current + delta) % count + count) % count
        }
    }

    fun moveVertex(paddockId: String, vertexIndex: Int, newPos: FractionalPoint) {
        collection = collection.copy(
            paddocks = collection.paddocks.map { shape ->
                if (shape.id != paddockId) return@map shape
                shape.copy(
                    vertices = shape.vertices.mapIndexed { i, v ->
                        if (i == vertexIndex) PaddockVertex(newPos.x, newPos.y) else v
                    },
                )
            },
        )
    }

    /**
     * Toggles the selected paddock's armed status. Flipping armed -> unarmed on
     * a fenced paddock triggers the map's disarm animation (flash orange, then
     * the fence fades away).
     */
    fun toggleSelectedArmed() {
        val id = selectedPaddockId ?: return
        collection = collection.copy(
            paddocks = collection.paddocks.map { shape ->
                if (shape.id == id) shape.copy(armed = !shape.armed) else shape
            },
        )
    }

    fun moveFacility(facilityId: String, newPos: FractionalPoint) {
        facilities = facilities.map { f ->
            if (f.id == facilityId) f.copy(position = newPos) else f
        }
    }

    /**
     * Inserts a new vertex at the midpoint of the edge following the selected
     * node (or the last edge if no node is selected), then selects it so it
     * can be dragged into place.
     */
    fun addVertexToSelectedPaddock() {
        val id = selectedPaddockId ?: return
        val shape = selectedPaddock ?: return
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

    /** JSON for the editor's Copy-JSON chip: facilities when one is selected, else paddocks. */
    fun copyJsonText(): String =
        if (selectedFacilityId != null) facilities.facilitiesToJson() else collection.toJson()
}
