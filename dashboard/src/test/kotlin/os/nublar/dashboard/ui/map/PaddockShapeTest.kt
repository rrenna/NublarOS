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
            assertTrue(shape.vertices.size >= 3, "${shape.id} is not a polygon (< 3 vertices)")
        }

        val reparsed = PaddockJson.decodeFromString<PaddockCollection>(collection.toJson())
        assertEquals(collection, reparsed, "paddock collection changed across a JSON round-trip")
    }
}
