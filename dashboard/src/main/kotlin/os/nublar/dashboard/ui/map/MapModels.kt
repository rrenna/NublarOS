package os.nublar.dashboard.ui.map

import kotlinx.serialization.Serializable

/** Point in fractional (0..1) coordinates relative to the map canvas. */
@Serializable
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
@Serializable
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
@Serializable
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


@kotlinx.serialization.Serializable
enum class FacilityKind { VisitorCenter, Helipad, Dock, MaintenanceShed }

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
 * Named species (see data/isla-nublar/dinosaurs.json) are each assigned to
 * the closest generic silhouette category for rendering.
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
