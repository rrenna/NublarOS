package os.nublar.dashboard.ui.map

import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaddockShapeTest {

    /**
     * The bundled Isla Nublar paddock data must parse into valid closed polygons
     * and survive a JSON encode/decode round-trip unchanged — this guards the
     * data files (a bad hand-edit to paddocks.json fails here) and the
     * [PaddockShape] serialization contract used by the map editor's Copy-JSON.
     */
    @Test
    fun `bundled paddocks parse and round-trip through JSON`() {
        val collection = loadPaddockCollection()

        assertTrue(collection.paddocks.isNotEmpty(), "expected paddocks.json to load at least one paddock")
        collection.paddocks.forEach { shape ->
            assertTrue(shape.id.isNotBlank(), "a paddock has a blank id")
            // Only fenced paddocks trace an outline; buildings are point markers.
            if (!shape.isBuilding) {
                assertTrue(shape.vertices.size >= 3, "${shape.id} is not a polygon (< 3 vertices)")
            }
        }

        val reparsed = PaddockJson.decodeFromString<PaddockCollection>(collection.toJson())
        assertEquals(collection, reparsed, "paddock collection changed across a JSON round-trip")
    }

    /**
     * Enclosure drives rendering (a Building is a solid block, a Fenced area is
     * an outline) and defaults to Fenced when the JSON omits it — so paddocks
     * authored before the field existed keep their fence lines.
     */
    @Test
    fun `enclosure parses and defaults to Fenced when absent`() {
        val byId = loadPaddockCollection().paddocks.associateBy { it.id }

        assertEquals(PaddockEnclosure.Building, byId.getValue("raptor-paddock").enclosure)
        assertEquals(PaddockEnclosure.Building, byId.getValue("herrerasaurus-paddock").enclosure)
        assertEquals(PaddockEnclosure.Fenced, byId.getValue("tyrannosaurus-paddock").enclosure)

        val noField = PaddockJson.decodeFromString<PaddockShape>(
            """{"id":"x","label":"X","vertices":[{"x":0.1,"y":0.1}]}""",
        )
        assertEquals(PaddockEnclosure.Fenced, noField.enclosure)
    }

    /** Buildings are point markers: a single anchor vertex, no fence to trace. */
    @Test
    fun `building paddocks are anchored by a single vertex`() {
        loadPaddockCollection().paddocks.filter { it.isBuilding }.forEach { shape ->
            assertEquals(1, shape.vertices.size, "${shape.id} should have one anchor vertex")
        }
    }
}
