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
 * A facility's definition as loaded from JSON. Coordinates are
 * island-normalized (0..1 within the source island) and fed through the
 * registration warp ([islandPoint]) at load — the same space as the
 * fan-map-derived POI data, distinct from the paddocks' canvas-fraction space.
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
 * Loads [ISLA_NUBLAR_DATA_DIR]/facilities.json and projects each facility onto
 * this project's island artwork via [islandPoint]. Returns an empty list if absent.
 */
fun loadFacilities(): List<FacilityMarker> {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/facilities.json")
        ?: return emptyList()
    val text = stream.bufferedReader().use { it.readText() }
    val collection: FacilityCollection = PaddockJson.decodeFromString(text)
    return collection.facilities.map {
        FacilityMarker(it.id, it.label, it.kind, islandPoint(it.x, it.y))
    }
}
