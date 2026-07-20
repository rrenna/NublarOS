package os.nublar.dashboard.viewmodel

import os.nublar.dashboard.data.DEFAULT_TOUR_CARS
import os.nublar.dashboard.data.IslaNublarRepository
import os.nublar.dashboard.data.LogEntry
import os.nublar.dashboard.ui.map.DinosaurMarker
import os.nublar.dashboard.ui.map.FacilityKind
import os.nublar.dashboard.ui.map.FenceSegment
import os.nublar.dashboard.ui.map.PaddockRecipe
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.FractionalPoint
import os.nublar.dashboard.ui.map.MapLayer
import os.nublar.dashboard.ui.map.PaddockCollection
import os.nublar.dashboard.ui.map.PaddockShape
import os.nublar.dashboard.ui.map.PaddockVertex
import os.nublar.dashboard.ui.map.StaffMarker
import os.nublar.dashboard.ui.map.VehicleMarker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Deterministic in-memory repository so assertions don't depend on the bundled JSON. */
private class FakeRepository : IslaNublarRepository {
    override fun paddocks() = PaddockCollection(
        paddocks = listOf(
            PaddockShape(
                id = "p1", label = "Paddock One", species = listOf("Velociraptor"), carnivore = true,
                vertices = listOf(
                    PaddockVertex(0.1f, 0.1f), PaddockVertex(0.3f, 0.1f), PaddockVertex(0.2f, 0.3f),
                ),
            ),
            PaddockShape(
                id = "p2", label = "Paddock Two", species = emptyList(), carnivore = false,
                vertices = listOf(
                    PaddockVertex(0.5f, 0.5f), PaddockVertex(0.7f, 0.5f), PaddockVertex(0.6f, 0.7f),
                ),
            ),
        ),
    )

    override fun facilities() = listOf(
        FacilityMarker("f1", "Facility One", FacilityKind.Helipad, FractionalPoint(0.4f, 0.4f)),
    )

    override fun vehicles() = emptyList<VehicleMarker>()
    override fun dinosaurs() = emptyList<DinosaurMarker>()
    override fun staff() = emptyList<StaffMarker>()
    override fun fences() = emptyList<FenceSegment>()
    override fun paddockRecipes() = emptyList<PaddockRecipe>()
    override fun glitchesLog() = emptyList<LogEntry>()
    override fun tourCars() = DEFAULT_TOUR_CARS
}

private fun viewModel(
    restoreLayers: () -> Set<String>? = { null },
    persistLayers: (Set<String>) -> Unit = {},
) = MapPreviewViewModel(FakeRepository(), restoreLayers, persistLayers)

class MapPreviewViewModelTest {

    @Test
    fun `selection is mutually exclusive between paddock and facility`() {
        val vm = viewModel()

        vm.selectPaddock("p1")
        vm.selectVertex(1)
        assertEquals("p1", vm.selectedPaddockId)
        assertEquals(1, vm.selectedVertexIndex)

        vm.selectFacility("f1")
        assertEquals("f1", vm.selectedFacilityId)
        assertNull(vm.selectedPaddockId, "facility selection must clear the paddock")
        assertNull(vm.selectedVertexIndex, "facility selection must clear the vertex")

        vm.selectPaddock("p2")
        assertEquals("p2", vm.selectedPaddockId)
        assertNull(vm.selectedFacilityId, "paddock selection must clear the facility")
    }

    @Test
    fun `selected paddock and facility resolve to their models`() {
        val vm = viewModel()
        vm.selectPaddock("p1")
        assertEquals("Paddock One", vm.selectedPaddock?.label)
        vm.selectFacility("f1")
        assertEquals("Facility One", vm.selectedFacility?.label)
    }

    @Test
    fun `hidden paddocks are withheld from the map but stay in the collection`() {
        val vm = viewModel()

        vm.togglePaddockVisibility("p1")

        assertEquals(setOf("p1"), vm.hiddenPaddockIds)
        assertEquals(listOf("p2"), vm.visiblePaddocks.map { it.id }, "hidden paddock must not reach the map")
        assertEquals(2, vm.collection.paddocks.size, "hiding must not remove it from the data")

        vm.togglePaddockVisibility("p1")
        assertEquals(listOf("p1", "p2"), vm.visiblePaddocks.map { it.id })
    }

    @Test
    fun `hiding the selected paddock clears the selection`() {
        val vm = viewModel()
        vm.selectPaddock("p1")
        vm.selectVertex(0)

        vm.togglePaddockVisibility("p1")

        assertNull(vm.selectedPaddockId, "a hidden paddock must not stay selected")
        assertNull(vm.selectedVertexIndex)
    }

    @Test
    fun `hiding an unselected paddock leaves the selection alone`() {
        val vm = viewModel()
        vm.selectPaddock("p2")

        vm.togglePaddockVisibility("p1")

        assertEquals("p2", vm.selectedPaddockId)
    }

    @Test
    fun `stepping the vertex walks the ring and wraps at both ends`() {
        val vm = viewModel()
        vm.selectPaddock("p1")   // 3 vertices

        // From no selection: forward starts at the first node.
        vm.stepSelectedVertex(1)
        assertEquals(0, vm.selectedVertexIndex)

        vm.stepSelectedVertex(1)
        assertEquals(1, vm.selectedVertexIndex)

        // Forward past the end wraps to the start.
        vm.stepSelectedVertex(1)
        assertEquals(2, vm.selectedVertexIndex)
        vm.stepSelectedVertex(1)
        assertEquals(0, vm.selectedVertexIndex)

        // Backward past the start wraps to the end.
        vm.stepSelectedVertex(-1)
        assertEquals(2, vm.selectedVertexIndex)
    }

    @Test
    fun `stepping backward from no selection starts at the last node`() {
        val vm = viewModel()
        vm.selectPaddock("p1")
        vm.stepSelectedVertex(-1)
        assertEquals(2, vm.selectedVertexIndex)
    }

    @Test
    fun `stepping with no paddock selected does nothing`() {
        val vm = viewModel()
        vm.stepSelectedVertex(1)
        assertNull(vm.selectedVertexIndex)
    }

    @Test
    fun `zoom clamps to its bounds`() {
        val vm = viewModel()
        repeat(20) { vm.zoomIn() }
        assertEquals(3f, vm.zoom)
        repeat(20) { vm.zoomOut() }
        assertEquals(0.5f, vm.zoom)
    }

    @Test
    fun `moveVertex updates only the targeted vertex`() {
        val vm = viewModel()
        vm.moveVertex("p1", 1, FractionalPoint(0.35f, 0.15f))

        val p1 = vm.collection.paddocks.first { it.id == "p1" }
        assertEquals(PaddockVertex(0.35f, 0.15f), p1.vertices[1])
        assertEquals(PaddockVertex(0.1f, 0.1f), p1.vertices[0], "other vertices must not move")
        assertEquals(
            FakeRepository().paddocks().paddocks.first { it.id == "p2" },
            vm.collection.paddocks.first { it.id == "p2" },
            "other paddocks must not change",
        )
    }

    @Test
    fun `moveFacility updates the facility position`() {
        val vm = viewModel()
        vm.moveFacility("f1", FractionalPoint(0.9f, 0.9f))
        assertEquals(FractionalPoint(0.9f, 0.9f), vm.facilities.first { it.id == "f1" }.position)
    }

    @Test
    fun `addVertex with no node selected inserts on the closing edge and selects it`() {
        val vm = viewModel()
        vm.selectPaddock("p1")

        vm.addVertexToSelectedPaddock()

        val p1 = vm.collection.paddocks.first { it.id == "p1" }
        assertEquals(4, p1.vertices.size)
        // Midpoint of last vertex (0.2, 0.3) -> first vertex (0.1, 0.1).
        assertEquals(PaddockVertex(0.15f, 0.2f), p1.vertices[3])
        assertEquals(3, vm.selectedVertexIndex, "the new node should be selected")
    }

    @Test
    fun `addVertex after a selected node splits that edge`() {
        val vm = viewModel()
        vm.selectPaddock("p1")
        vm.selectVertex(0)

        vm.addVertexToSelectedPaddock()

        val p1 = vm.collection.paddocks.first { it.id == "p1" }
        assertEquals(4, p1.vertices.size)
        // Midpoint of vertex 0 (0.1, 0.1) -> vertex 1 (0.3, 0.1), inserted at index 1.
        assertEquals(PaddockVertex(0.2f, 0.1f), p1.vertices[1])
        assertEquals(1, vm.selectedVertexIndex)
    }

    @Test
    fun `copyJsonText serializes paddocks by default and facilities when one is selected`() {
        val vm = viewModel()
        assertTrue("\"paddocks\"" in vm.copyJsonText())
        vm.selectFacility("f1")
        assertTrue("\"facilities\"" in vm.copyJsonText())
    }

    @Test
    fun `layer filters restore from persistence including all-off`() {
        val restored = viewModel(restoreLayers = { setOf(MapLayer.Paddocks.name, MapLayer.Staff.name) })
        assertEquals(setOf(MapLayer.Paddocks, MapLayer.Staff), restored.activeLayers)

        val allOff = viewModel(restoreLayers = { emptySet() })
        assertEquals(emptySet(), allOff.activeLayers, "persisted all-off must not become all-on")

        val firstRun = viewModel(restoreLayers = { null })
        assertEquals(MapLayer.entries.toSet(), firstRun.activeLayers)
    }

    @Test
    fun `unknown persisted layer names are dropped`() {
        // e.g. a MapLayer that was renamed between versions.
        val vm = viewModel(restoreLayers = { setOf(MapLayer.Vehicles.name, "NoSuchLayer") })
        assertEquals(setOf(MapLayer.Vehicles), vm.activeLayers)
    }

    @Test
    fun `toggleLayer flips the layer and persists the result`() {
        var persisted: Set<String>? = null
        val vm = viewModel(persistLayers = { persisted = it })

        vm.toggleLayer(MapLayer.Staff)

        assertTrue(MapLayer.Staff !in vm.activeLayers)
        assertEquals(vm.activeLayers.map { it.name }.toSet(), persisted)

        vm.toggleLayer(MapLayer.Staff)
        assertTrue(MapLayer.Staff in vm.activeLayers)
    }
}
