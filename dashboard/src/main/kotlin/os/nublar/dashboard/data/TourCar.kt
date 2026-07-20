package os.nublar.dashboard.data

/**
 * A park tour vehicle (the EXP fleet). Backs the Vehicle Status panel: the UI
 * renders these values — speed, headlights, responding — rather than hardcoded
 * copy, so show-sync events (or future telemetry) can drive the panel by
 * updating the model.
 */
data class TourCar(
    val id: String,
    val label: String,
    val speedMph: Int,
    val headlightsOn: Boolean,
    /** False shows the red "Not Responding" flag in the status panel. */
    val responding: Boolean = true,
)

/**
 * The default EXP fleet at show start (EXP 4 stalled mid-tour with its lights
 * on, matching the film frame). Guaranteed non-empty — the Vehicle Status
 * panel always has a car to track.
 */
val DEFAULT_TOUR_CARS: List<TourCar> = listOf(
    TourCar("exp-4", "EXP 4", speedMph = 12, headlightsOn = true, responding = false),
    TourCar("exp-5", "EXP 5", speedMph = 0, headlightsOn = true),
    TourCar("exp-6", "EXP 6", speedMph = 0, headlightsOn = false),
    TourCar("exp-7", "EXP 7", speedMph = 0, headlightsOn = false),
)
