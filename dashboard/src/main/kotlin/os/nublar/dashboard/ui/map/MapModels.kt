package os.nublar.dashboard.ui.map

/** Point in fractional (0..1) coordinates relative to the map canvas. */
data class FractionalPoint(val x: Float, val y: Float)

/** Toggleable overlay layers the map can render, independent of each other. */
enum class MapLayer { Paddocks, Facilities, Dinosaurs, Vehicles, Staff }

/** Whether a fence segment is fully visible in the source, or partly clipped/implied. */
enum class FenceStatus { Visible, VisiblePartial }

/**
 * One traced red fence centerline, as an OPEN polyline (not a closed
 * polygon). Per isla_nublar_fence_network_redo.md: the fences form a shared
 * network, so segments must never be auto-closed or have arbitrary endpoints
 * joined — that invents boundaries the source map doesn't show.
 */
data class FenceSegment(
    val id: String,
    val label: String,
    val points: List<FractionalPoint>,
    val status: FenceStatus = FenceStatus.Visible,
    val confidence: String = "high",
)

/** How reliably a derived paddock region is bounded by the visible fence network. */
enum class PaddockBoundaryQuality { GoodCandidate, Approximate }

/**
 * A derived paddock "recipe" — which fence segments are intended to bound a
 * species' region. Deliberately NOT a polygon: it references shared fence
 * segments so regions can be tooltip-labelled and later filled by clipping,
 * without duplicating or inventing closure edges.
 */
data class PaddockRecipe(
    val id: String,
    val label: String,
    val species: List<String>,
    val usesFenceSegments: List<String>,
    val quality: PaddockBoundaryQuality,
)

/**
 * The landmass bounding box of dashboard/src/main/resources/island.png, in
 * canvas-fraction (0..1) coordinates. Measured directly from the artwork
 * (green land vs. blue water) rather than assumed, so island-relative POI
 * data lines up with this specific piece of art. Re-measure if the artwork
 * is ever replaced.
 */
object IslandBounds {
    const val LEFT = 0.158f
    const val TOP = 0.069f
    const val RIGHT = 0.864f
    const val BOTTOM = 0.939f
    const val WIDTH = RIGHT - LEFT
    const val HEIGHT = BOTTOM - TOP
}

/**
 * Registration warp that fits the source fan-map's island-relative
 * coordinates onto this project's differently-shaped island artwork.
 *
 * The two islands aren't the same shape, so a uniform bounding-box map
 * pushes the source's bulbous east coast off our (more concave) coastline
 * into the ocean. The source dataset ships landmark control points for
 * exactly this; here each control point pairs its source island-normalized
 * position with an estimated target position on OUR island, and arbitrary
 * points are warped by inverse-distance-weighted blending of those
 * displacements (a lightweight stand-in for the thin-plate spline the
 * dataset recommends). Target values are a first-pass eyeball fit and are
 * the knobs to tune when the overlay doesn't sit on the coastline.
 */
private object MapRegistration {
    // source (fan island-norm) to target (our island-norm).
    private val controls: List<Pair<Pair<Float, Float>, Pair<Float, Float>>> = listOf(
        (0.360f to 0.154f) to (0.360f to 0.170f),   // visitor center junction (west-central)
        (0.640f to 0.045f) to (0.600f to 0.100f),   // north cove
        (0.913f to 0.156f) to (0.740f to 0.200f),   // northeast tip (pull east coast in)
        (0.727f to 0.242f) to (0.660f to 0.280f),   // T. rex center
        (0.536f to 0.599f) to (0.520f to 0.560f),   // helipad
        (0.884f to 0.714f) to (0.720f to 0.590f),   // east dock (pull SE coast in)
        (0.803f to 0.803f) to (0.640f to 0.730f),   // south spur tip
        (0.763f to 0.733f) to (0.680f to 0.650f),   // southeast bay
    )

    fun warp(x: Float, y: Float): Pair<Float, Float> {
        var weightSum = 0f
        var dx = 0f
        var dy = 0f
        for ((src, tgt) in controls) {
            val distSq = (x - src.first) * (x - src.first) + (y - src.second) * (y - src.second) + 1e-4f
            val w = 1f / distSq
            weightSum += w
            dx += w * (tgt.first - src.first)
            dy += w * (tgt.second - src.second)
        }
        return (x + dx / weightSum) to (y + dy / weightSum)
    }
}

/**
 * Converts island-local normalized coordinates (0..1 within the source
 * island) to canvas-fraction coordinates on this project's artwork, applying
 * the [MapRegistration] warp so overlays track our coastline rather than the
 * source map's differently-shaped one.
 */
fun islandPoint(xNormalized: Float, yNormalized: Float): FractionalPoint {
    val (wx, wy) = MapRegistration.warp(xNormalized, yNormalized)
    return FractionalPoint(
        x = IslandBounds.LEFT + wx * IslandBounds.WIDTH,
        y = IslandBounds.TOP + wy * IslandBounds.HEIGHT,
    )
}

/** Shorthand for building a paddock boundary from island-local normalized (x, y) pairs. */
private fun boundaryOf(vararg points: Pair<Float, Float>): List<FractionalPoint> =
    points.map { (x, y) -> islandPoint(x, y) }

@kotlinx.serialization.Serializable
enum class FacilityKind { VisitorCenter, Helipad, Dock }

data class FacilityMarker(
    val id: String,
    val label: String,
    val kind: FacilityKind,
    val position: FractionalPoint,
)

/**
 * Original, generic dinosaur silhouette categories (large biped predator,
 * small pack predator, frilled quadruped, long-neck quadruped, ostrich-like
 * runner) — not derived from any specific copyrighted character design.
 * Named species (see SampleMapData) are each assigned to the closest
 * generic silhouette category for rendering.
 */
enum class DinosaurSpecies(val displayName: String, val abbreviation: String, val carnivore: Boolean) {
    LargePredator("Large predator", "TR", carnivore = true),
    PackPredator("Pack predator", "RA", carnivore = true),
    FrilledGrazer("Frilled grazer", "TC", carnivore = false),
    LongNeckGrazer("Long-neck grazer", "BR", carnivore = false),
    SwiftRunner("Swift runner", "GA", carnivore = false),
}

data class DinosaurMarker(
    val species: DinosaurSpecies,
    val position: FractionalPoint,
    val label: String? = null,
    val confidence: String = "high",
)

data class VehicleMarker(
    val id: String,
    val position: FractionalPoint,
    val headingDegrees: Float = 0f,
)

data class StaffMarker(
    val id: String,
    val position: FractionalPoint,
)

/**
 * Sample data used by both the real Raptor Paddock screen and the
 * standalone map preview. Facility and dinosaur POIs are transcribed from
 * a fan-measured coordinate dataset (isla_nublar_poi_data.md) of a
 * different fan-made island map, then re-projected onto this project's own
 * island artwork via [islandPoint] (same island-relative layout, different
 * underlying image) — see [IslandBounds].
 */
object SampleMapData {
    /**
     * Red fence centerlines transcribed from isla_nublar_fence_network_redo.md
     * — a second-pass tracing of a different fan-made island map's fence
     * lines as a shared OPEN-polyline network, re-projected onto this
     * project's own island artwork via [islandPoint]/[boundaryOf] (same
     * island-relative layout, different underlying image and renderer). Some
     * east-perimeter points intentionally read slightly past the measured
     * island bounds (island X > 1.0); that's the source tracing, left as-is
     * pending a control-point warp onto this artwork.
     */
    val fenceSegments = listOf(
        FenceSegment(
            "outer_north", "Outer north perimeter", status = FenceStatus.Visible, confidence = "medium",
            points = boundaryOf(
                0.348331f to 0.191194f, 0.406386f to 0.149479f, 0.489115f to 0.129780f,
                0.566038f to 0.106605f, 0.615385f to 0.082271f, 0.663280f to 0.074160f,
                0.706821f to 0.083430f, 0.759071f to 0.111240f, 0.786647f to 0.106605f,
                0.820029f to 0.125145f, 0.851959f to 0.159907f, 0.912917f to 0.156431f,
            ),
        ),
        FenceSegment(
            "outer_east", "Outer east perimeter", status = FenceStatus.Visible, confidence = "medium",
            points = boundaryOf(
                0.912917f to 0.156431f, 0.912917f to 0.227115f, 0.920174f to 0.289687f,
                0.962264f to 0.327926f, 1.020319f to 0.338355f, 1.072569f to 0.344148f,
                1.072569f to 0.450753f, 1.030479f to 0.487833f, 0.985486f to 0.523754f,
                0.950653f to 0.563152f, 0.927431f to 0.629200f, 0.920174f to 0.698725f,
            ),
        ),
        FenceSegment(
            "outer_south", "Outer south perimeter", status = FenceStatus.Visible, confidence = "medium",
            points = boundaryOf(
                0.920174f to 0.698725f, 0.875181f to 0.762457f, 0.802612f to 0.803013f,
                0.693759f to 0.779838f, 0.618287f to 0.742758f, 0.574746f to 0.703360f,
            ),
        ),
        FenceSegment(
            "outer_west", "Outer west / southwest perimeter", status = FenceStatus.VisiblePartial, confidence = "medium",
            points = boundaryOf(
                0.574746f to 0.703360f, 0.573295f to 0.519119f, 0.500726f to 0.476246f,
                0.431060f to 0.446118f, 0.404935f to 0.374276f, 0.348331f to 0.305910f,
                0.348331f to 0.191194f,
            ),
        ),
        FenceSegment(
            "upper_west_span", "Upper west interior span",
            points = boundaryOf(0.348331f to 0.191194f, 0.535559f to 0.198146f, 0.734398f to 0.242178f),
        ),
        FenceSegment(
            "north_divider", "North divider",
            points = boundaryOf(0.606676f to 0.136732f, 0.663280f to 0.253766f, 0.734398f to 0.242178f),
        ),
        FenceSegment(
            "north_inner_run", "North inner run",
            points = boundaryOf(
                0.734398f to 0.242178f, 0.763425f to 0.219003f, 0.764877f to 0.183082f,
                0.796807f to 0.164542f, 0.911466f to 0.184241f,
            ),
        ),
        FenceSegment(
            "west_center_vertical", "West-center vertical divider",
            points = boundaryOf(0.563135f to 0.428737f, 0.563135f to 0.307068f, 0.671988f to 0.290846f),
        ),
        FenceSegment(
            "center_north_diagonal", "Center-to-north diagonal",
            points = boundaryOf(0.563135f to 0.307068f, 0.671988f to 0.290846f, 0.734398f to 0.242178f),
        ),
        FenceSegment(
            "south_diagonal", "South diagonal",
            points = boundaryOf(0.563135f to 0.428737f, 0.833091f to 0.458864f),
        ),
        FenceSegment(
            "trex_outline_west_south", "T. rex outline west/south edge",
            points = boundaryOf(
                0.671988f to 0.290846f, 0.635704f to 0.322132f, 0.637155f to 0.428737f, 0.833091f to 0.458864f,
            ),
        ),
        FenceSegment(
            "trex_outline_north_east", "T. rex outline north/east edge",
            points = boundaryOf(
                0.671988f to 0.290846f, 0.734398f to 0.242178f, 0.763425f to 0.219003f,
                0.764877f to 0.183082f, 0.796807f to 0.164542f, 0.911466f to 0.184241f,
                0.910015f to 0.336037f,
            ),
        ),
        FenceSegment(
            "east_mid_horizontal", "East mid horizontal divider",
            points = boundaryOf(0.910015f to 0.336037f, 1.072569f to 0.344148f),
        ),
        FenceSegment(
            "east_mid_divider", "East mid divider",
            points = boundaryOf(0.833091f to 0.458864f, 0.910015f to 0.336037f),
        ),
        FenceSegment(
            "east_lower_left", "East-lower left divider",
            points = boundaryOf(0.833091f to 0.458864f, 0.833091f to 0.564311f),
        ),
        FenceSegment(
            "east_lower_bottom", "East-lower bottom divider",
            points = boundaryOf(0.833091f to 0.564311f, 0.982583f to 0.597914f),
        ),
        FenceSegment(
            "helipad_spur", "Helipad south spur",
            points = boundaryOf(0.573295f to 0.519119f, 0.574746f to 0.703360f),
        ),
    )

    /**
     * Derived paddock regions — which fence segments bound each species'
     * area. Not polygons (see [PaddockRecipe]); used for hover labels and,
     * later, clipped fills. The two markers the earlier dataset flagged as
     * `fence_not_shown` (Dilophosaurus, Velociraptor) have no recipe here —
     * they appear only as Dinosaur-layer markers.
     */
    val paddockRecipes = listOf(
        PaddockRecipe(
            "nw_shared_herbivore", "Shared northwest herbivore area",
            listOf("Parasaurolophus", "Brachiosaurus"),
            listOf("outer_north", "upper_west_span", "north_divider"),
            PaddockBoundaryQuality.Approximate,
        ),
        PaddockRecipe(
            "triceratops_region", "Triceratops region", listOf("Triceratops"),
            listOf("outer_north", "north_divider", "north_inner_run", "outer_east"),
            PaddockBoundaryQuality.Approximate,
        ),
        PaddockRecipe(
            "segisaurus_strip", "Segisaurus strip", listOf("Segisaurus"),
            listOf("outer_north", "outer_east", "east_mid_horizontal"),
            PaddockBoundaryQuality.Approximate,
        ),
        PaddockRecipe(
            "gallimimus_area", "Gallimimus area", listOf("Gallimimus"),
            listOf("upper_west_span", "west_center_vertical", "south_diagonal"),
            PaddockBoundaryQuality.GoodCandidate,
        ),
        PaddockRecipe(
            "trex_paddock", "Tyrannosaurus paddock", listOf("Tyrannosaurus"),
            listOf("trex_outline_west_south", "trex_outline_north_east"),
            PaddockBoundaryQuality.GoodCandidate,
        ),
        PaddockRecipe(
            "east_middle_area", "East-middle area", listOf("Herrerasaurus"),
            listOf("east_mid_divider", "east_mid_horizontal", "east_lower_left", "outer_east"),
            PaddockBoundaryQuality.Approximate,
        ),
        PaddockRecipe(
            "baryonyx_area", "Baryonyx area", listOf("Baryonyx"),
            listOf("outer_east", "east_lower_left", "east_lower_bottom"),
            PaddockBoundaryQuality.GoodCandidate,
        ),
        PaddockRecipe(
            "south_outer_area", "South outer area", listOf("Proceratosaurus"),
            listOf("outer_south", "outer_west", "helipad_spur"),
            PaddockBoundaryQuality.Approximate,
        ),
    )

    // Loaded from data/isla-nublar/facilities.json (source of truth).
    val facilities = loadFacilities()

    val dinosaurs = listOf(
        DinosaurMarker(
            DinosaurSpecies.LargePredator, islandPoint(0.7649f, 0.2966f),
            "Tyrannosaurus paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.LargePredator, islandPoint(0.8911f, 0.4148f),
            "Baryonyx paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.PackPredator, islandPoint(0.2917f, 0.3662f),
            "Velociraptor area", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.SwiftRunner, islandPoint(0.5356f, 0.3372f),
            "Gallimimus paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.LongNeckGrazer, islandPoint(0.5762f, 0.2375f),
            "Parasaurolophus paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.LongNeckGrazer, islandPoint(0.6386f, 0.2387f),
            "Brachiosaurus paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.FrilledGrazer, islandPoint(0.7475f, 0.1750f),
            "Triceratops paddock", "high",
        ),
        DinosaurMarker(
            DinosaurSpecies.PackPredator, islandPoint(0.2293f, 0.3998f),
            "Dilophosaurus paddock", "medium",
        ),
        DinosaurMarker(
            DinosaurSpecies.PackPredator, islandPoint(0.6894f, 0.6408f),
            "Proceratosaurus paddock", "medium",
        ),
        DinosaurMarker(
            DinosaurSpecies.PackPredator, islandPoint(0.8403f, 0.3720f),
            "Herrerasaurus paddock", "medium",
        ),
        DinosaurMarker(
            DinosaurSpecies.PackPredator, islandPoint(0.8534f, 0.2005f),
            "Segisaurus paddock", "medium",
        ),
        DinosaurMarker(
            DinosaurSpecies.LargePredator, islandPoint(0.5181f, 0.4415f),
            "Metriacanthosaurus paddock", "medium",
        ),
        DinosaurMarker(
            DinosaurSpecies.LargePredator, islandPoint(0.4615f, 0.3720f),
            "Unresolved carnivore", "low",
        ),
    )

    // Loaded from data/isla-nublar/vehicles.json (source of truth).
    val vehicles = loadVehicles()

    val staff = listOf(
        StaffMarker("Operations - A. Ramirez", islandPoint(0.42f, 0.40f)),
        StaffMarker("Field Tech - J. Ortiz", islandPoint(0.60f, 0.55f)),
    )
}
