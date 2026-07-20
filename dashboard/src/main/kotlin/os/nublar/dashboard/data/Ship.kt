package os.nublar.dashboard.data

/** A supply/transport vessel that can occupy the East Dock. */
data class Ship(
    val id: String,
    val name: String,
    val origin: String = "Mainland Supply Run",
)

/** The East Dock's berth occupancy. */
enum class DockStatus(val label: String) {
    Empty("EMPTY"),
    Docking("DOCKING"),
    Docked("DOCKED"),
    Departing("DEPARTING"),
}

/**
 * The East Dock's current state — the model backing the East Dock status
 * panel: which ship (if any) is present, and the berth's docking status. Mirrors
 * [LoadingBayState] as a small, focused facility model rather than raw
 * ViewModel state, so it can be unit tested and reused independently of the UI.
 */
data class EastDockState(
    val status: DockStatus = DockStatus.Empty,
    val ship: Ship? = null,
) {
    /** No ship is present and the berth is idle. */
    val isEmpty: Boolean get() = status == DockStatus.Empty && ship == null
}
