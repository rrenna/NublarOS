package os.nublar.dashboard.data

import os.nublar.dashboard.ui.map.FractionalPoint

/** A helicopter's identity — today always InGen's own, but modeled separately
 *  from its location/state so the Helipad can reference "which helicopter"
 *  by value (mirrors [Ship] on the East Dock). */
data class Helicopter(
    val id: String,
    val callsign: String,
)

/** The InGen helicopter tracked by the park. */
val INGEN_HELICOPTER: Helicopter = Helicopter(id = "ingen-01", callsign = "InGen One")

/** Where InGen's helicopter currently is. */
enum class HelicopterLocation(val label: String) {
    CostaRica("COSTA RICA"),
    InTransit("IN TRANSIT"),
    OnIsland("ON ISLAND"),
}

/**
 * InGen's helicopter. [position] is its live map coordinate while
 * [HelicopterLocation.InTransit] — flying between the mainland and the island,
 * so it can be plotted on the map — and is meaningless (kept null) at the other
 * two locations, which are fixed places rather than points in transit.
 */
data class HelicopterState(
    val helicopter: Helicopter = INGEN_HELICOPTER,
    val location: HelicopterLocation = HelicopterLocation.CostaRica,
    val position: FractionalPoint? = null,
) {
    /** The position to plot on the map, or null when not airborne over the map. */
    val mapPosition: FractionalPoint? get() = position.takeIf { location == HelicopterLocation.InTransit }
}

/** The Helipad facility's berth status. */
enum class HelipadStatus(val label: String) {
    Empty("EMPTY"),
    Landing("LANDING"),
    Occupied("OCCUPIED"),
    Departing("DEPARTING"),
}

/**
 * The Helipad facility's current state — mirrors [EastDockState] as the model
 * backing the Helipad status panel: which helicopter (if any) is landing or
 * present on the pad, and the pad's status.
 */
data class HelipadState(
    val status: HelipadStatus = HelipadStatus.Empty,
    val helicopter: Helicopter? = null,
) {
    val isEmpty: Boolean get() = status == HelipadStatus.Empty && helicopter == null
}
