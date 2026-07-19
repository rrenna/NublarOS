package os.nublar.dashboard.ui.map

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
    /** How the animals are contained — an open fenced area, or an enclosed structure. */
    val enclosure: PaddockEnclosure = PaddockEnclosure.Fenced,
    /**
     * Optional fence status shown under the paddock name on the map (e.g.
     * "UNARMED", "ARMED"). Null hides the status line. Free-text so the JSON can
     * carry whatever the scenario needs. Only shown when [displayMode] is [PaddockDisplayMode.Name].
     */
    val fenceState: String? = null,
    /** What the paddock renders at its center: its species icon(s), or its name + fence status. */
    val displayMode: PaddockDisplayMode = PaddockDisplayMode.SpeciesIcon,
    /**
     * Whether this paddock's fence is armed (electrified/active). An armed fence
     * is drawn; when it transitions armed -> unarmed the map plays a disarm
     * animation (flash orange, then fade away), after which the fence is hidden.
     * Buildings have no fence, so this has no visual effect on them.
     */
    val armed: Boolean = true,
) {
    fun toFractionalPoints(): List<FractionalPoint> = vertices.map { FractionalPoint(it.x, it.y) }

    /** True when this paddock is a point marker rather than a traced outline. */
    val isBuilding: Boolean get() = enclosure == PaddockEnclosure.Building
}

/**
 * How a paddock contains its animals. [Fenced] is an open-air enclosure traced
 * as a fence outline through every vertex; [Building] is a roofed structure
 * (e.g. the raptor pen) with no fence to draw — it's a single-point marker,
 * shown as just its species icon and moved by dragging that icon.
 */
@Serializable
enum class PaddockEnclosure { Fenced, Building }

/**
 * What a paddock draws at its center on the map: [SpeciesIcon] shows the
 * species icon disc(s) (default); [Name] shows the paddock name with its
 * optional fence status underneath instead of the icons.
 */
@Serializable
enum class PaddockDisplayMode { SpeciesIcon, Name }

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

/**
 * Loads [ISLA_NUBLAR_DATA_DIR]/paddocks.json; returns an empty collection if
 * absent or malformed (these files are hand-edited via the map editor, so a
 * bad edit must not crash the app during composition).
 */
fun loadPaddockCollection(): PaddockCollection {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/paddocks.json")
        ?: return PaddockCollection()
    val text = stream.bufferedReader().use { it.readText() }
    return try {
        PaddockJson.decodeFromString(text)
    } catch (e: SerializationException) {
        System.err.println("Ignoring malformed paddocks.json: ${e.message}")
        PaddockCollection()
    }
}

/** Serializes a paddock collection back to pretty JSON (for the editor's Copy-JSON action). */
fun PaddockCollection.toJson(): String = PaddockJson.encodeToString(this)

/**
 * Loads [ISLA_NUBLAR_DATA_DIR]/facilities.json. Coordinates are canvas-fraction
 * (used directly, no warp). Returns an empty list if absent or malformed.
 */
fun loadFacilities(): List<FacilityMarker> {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/facilities.json")
        ?: return emptyList()
    val text = stream.bufferedReader().use { it.readText() }
    val collection: FacilityCollection = try {
        PaddockJson.decodeFromString(text)
    } catch (e: SerializationException) {
        System.err.println("Ignoring malformed facilities.json: ${e.message}")
        return emptyList()
    }
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
 * (used directly, no warp). Returns an empty list if absent or malformed.
 */
fun loadVehicles(): List<VehicleMarker> {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/vehicles.json")
        ?: return emptyList()
    val text = stream.bufferedReader().use { it.readText() }
    val collection: VehicleCollection = try {
        PaddockJson.decodeFromString(text)
    } catch (e: SerializationException) {
        System.err.println("Ignoring malformed vehicles.json: ${e.message}")
        return emptyList()
    }
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

/**
 * A tracked animal's placement as loaded from JSON. Coordinates are
 * canvas-fraction, same space as paddocks/facilities/vehicles.
 */
@Serializable
data class DinosaurDefinition(
    val species: DinosaurSpecies,
    val x: Float,
    val y: Float,
    val label: String? = null,
    val confidence: String = "high",
)

@Serializable
data class DinosaurCollection(val dinosaurs: List<DinosaurDefinition> = emptyList())

/** A staff member's placement as loaded from JSON. Canvas-fraction coordinates. */
@Serializable
data class StaffDefinition(val id: String, val x: Float, val y: Float)

@Serializable
data class StaffCollection(val staff: List<StaffDefinition> = emptyList())

/** Fence polylines ([FenceSegment] serializes directly; points are canvas-fraction). */
@Serializable
data class FenceCollection(val fences: List<FenceSegment> = emptyList())

/** Derived paddock recipes ([PaddockRecipe] serializes directly). */
@Serializable
data class PaddockRecipeCollection(val recipes: List<PaddockRecipe> = emptyList())

/** Loads [ISLA_NUBLAR_DATA_DIR]/dinosaurs.json; empty if absent. */
fun loadDinosaurs(): List<DinosaurMarker> = loadCollection<DinosaurCollection>("dinosaurs.json")
    ?.dinosaurs
    ?.map { DinosaurMarker(it.species, FractionalPoint(it.x, it.y), it.label, it.confidence) }
    ?: emptyList()

/** Loads [ISLA_NUBLAR_DATA_DIR]/staff.json; empty if absent. */
fun loadStaff(): List<StaffMarker> = loadCollection<StaffCollection>("staff.json")
    ?.staff
    ?.map { StaffMarker(it.id, FractionalPoint(it.x, it.y)) }
    ?: emptyList()

/** Loads [ISLA_NUBLAR_DATA_DIR]/fences.json; empty if absent. */
fun loadFences(): List<FenceSegment> =
    loadCollection<FenceCollection>("fences.json")?.fences ?: emptyList()

/** Loads [ISLA_NUBLAR_DATA_DIR]/paddock-recipes.json; empty if absent. */
fun loadPaddockRecipes(): List<PaddockRecipe> =
    loadCollection<PaddockRecipeCollection>("paddock-recipes.json")?.recipes ?: emptyList()

/** Reads and decodes a JSON resource under [ISLA_NUBLAR_DATA_DIR], or null if absent or malformed. */
internal inline fun <reified T> loadCollection(fileName: String): T? {
    val stream = object {}.javaClass.classLoader.getResourceAsStream("$ISLA_NUBLAR_DATA_DIR/$fileName")
        ?: return null
    val text = stream.bufferedReader().use { it.readText() }
    return try {
        PaddockJson.decodeFromString<T>(text)
    } catch (e: SerializationException) {
        System.err.println("Ignoring malformed $fileName: ${e.message}")
        null
    }
}
