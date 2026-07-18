package os.nublar.dashboard.ui.map

import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Facilities and vehicles counterpart to [PaddockShapeTest]: the bundled JSON
 * must parse, and the marker → JSON → marker round-trip used by the editor's
 * Copy-JSON action must be identity.
 */
class MarkerJsonTest {

    @Test
    fun `bundled facilities parse and round-trip through JSON`() {
        val markers = loadFacilities()
        assertTrue(markers.isNotEmpty(), "expected facilities.json to load at least one facility")
        markers.forEach { assertTrue(it.id.isNotBlank(), "a facility has a blank id") }

        val reparsed = PaddockJson.decodeFromString<FacilityCollection>(markers.facilitiesToJson())
            .facilities.map { FacilityMarker(it.id, it.label, it.kind, FractionalPoint(it.x, it.y)) }
        assertEquals(markers, reparsed, "facilities changed across a JSON round-trip")
    }

    @Test
    fun `bundled vehicles parse and round-trip through JSON`() {
        val markers = loadVehicles()
        assertTrue(markers.isNotEmpty(), "expected vehicles.json to load at least one vehicle")
        markers.forEach { assertTrue(it.id.isNotBlank(), "a vehicle has a blank id") }

        val reparsed = PaddockJson.decodeFromString<VehicleCollection>(markers.vehiclesToJson())
            .vehicles.map { VehicleMarker(it.id, FractionalPoint(it.x, it.y), it.headingDegrees) }
        assertEquals(markers, reparsed, "vehicles changed across a JSON round-trip")
    }
}
