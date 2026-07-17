package os.nublar.dashboard.ui.map

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A hand-authored, editable paddock outline, serializable to/from JSON.
 *
 * Vertices are in canvas-fraction (0..1) coordinates relative to the map
 * canvas — i.e. drawn directly on this project's island artwork, NOT the
 * source fan map's island space, so the visual editor round-trips exactly
 * (what you drag is what gets saved) with no registration warp involved.
 * This is distinct from the fan-map-derived [FenceSegment] network.
 */
@Serializable
data class PaddockShape(
    val id: String,
    val label: String,
    val species: List<String> = emptyList(),
    /** Drives the paddock's species-icon disc color: red for carnivore, green for herbivore. */
    val carnivore: Boolean = true,
    val vertices: List<PaddockVertex>,
    val closed: Boolean = true,
) {
    fun toFractionalPoints(): List<FractionalPoint> = vertices.map { FractionalPoint(it.x, it.y) }
}

@Serializable
data class PaddockVertex(val x: Float, val y: Float)

@Serializable
data class PaddockCollection(val paddocks: List<PaddockShape> = emptyList())

/**
 * A facility's definition as loaded from / saved to JSON. Coordinates are
 * canvas-fraction (0..1 relative to the map canvas) — drawn directly on this
 * project's island artwork, same space as the paddocks, so the visual editor
 * round-trips exactly (no registration warp involved).
 */
@Serializable
data class FacilityDefinition(
    val id: String,
    val label: String,
    val kind: FacilityKind,
    val x: Float,
    val y: Float,
)

@Serializable
data class FacilityCollection(val facilities: List<FacilityDefinition> = emptyList())

/**
 * A vehicle's initial placement as loaded from / saved to JSON. Coordinates
 * are canvas-fraction (0..1 relative to the map canvas) — drawn directly on
 * this project's island artwork, same space as paddocks and facilities, so
 * the editor round-trips exactly (no registration warp involved).
 */
@Serializable
data class VehicleDefinition(
    val id: String,
    val x: Float,
    val y: Float,
    val headingDegrees: Float = 0f,
)

@Serializable
data class VehicleCollection(val vehicles: List<VehicleDefinition> = emptyList())

/** JSON with indentation, so a Copy-JSON round-trip stays human-editable. */
val PaddockJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/** Root of the bundled Isla Nublar data files (paddocks, facilities, …), on the classpath. */
const val ISLA_NUBLAR_DATA_DIR = "data/isla-nublar"

/** Loads [ISLA_NUBLAR_DATA_DIR]/paddocks.json; returns an empty collection if absent. */
fun loadPaddockCollection(): PaddockCollection {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/paddocks.json")
        ?: return PaddockCollection()
    val text = stream.bufferedReader().use { it.readText() }
    return PaddockJson.decodeFromString(text)
}

/** Serializes a paddock collection back to pretty JSON (for the editor's Copy-JSON action). */
fun PaddockCollection.toJson(): String = PaddockJson.encodeToString(this)

/**
 * Loads [ISLA_NUBLAR_DATA_DIR]/facilities.json. Coordinates are canvas-fraction
 * (used directly, no warp). Returns an empty list if absent.
 */
fun loadFacilities(): List<FacilityMarker> {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/facilities.json")
        ?: return emptyList()
    val text = stream.bufferedReader().use { it.readText() }
    val collection: FacilityCollection = PaddockJson.decodeFromString(text)
    return collection.facilities.map {
        FacilityMarker(it.id, it.label, it.kind, FractionalPoint(it.x, it.y))
    }
}

/** Serializes facility markers back to pretty JSON (for the editor's Copy-JSON action). */
fun List<FacilityMarker>.facilitiesToJson(): String {
    val collection = FacilityCollection(
        facilities = map { FacilityDefinition(it.id, it.label, it.kind, it.position.x, it.position.y) },
    )
    return PaddockJson.encodeToString(collection)
}

/**
 * Loads [ISLA_NUBLAR_DATA_DIR]/vehicles.json. Coordinates are canvas-fraction
 * (used directly, no warp). Returns an empty list if absent.
 */
fun loadVehicles(): List<VehicleMarker> {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/vehicles.json")
        ?: return emptyList()
    val text = stream.bufferedReader().use { it.readText() }
    val collection: VehicleCollection = PaddockJson.decodeFromString(text)
    return collection.vehicles.map {
        VehicleMarker(it.id, FractionalPoint(it.x, it.y), it.headingDegrees)
    }
}

/** Serializes vehicle markers back to pretty JSON (for editor Copy-JSON / round-trips). */
fun List<VehicleMarker>.vehiclesToJson(): String {
    val collection = VehicleCollection(
        vehicles = map { VehicleDefinition(it.id, it.position.x, it.position.y, it.headingDegrees) },
    )
    return PaddockJson.encodeToString(collection)
}
