package os.nublar.dashboard.data

/** The Visitor Center's operational status. */
enum class VisitorCenterStatus(val label: String) {
    Operational("OPERATIONAL"),
    Lockdown("LOCKDOWN"),
    PowerFailure("POWER FAILURE"),
}

/**
 * The Visitor Center facility's current state — mirrors [EastDockState] /
 * [HelipadState] as the model backing the Visitor Center status panel: its
 * operational status, and whether staff/guests are currently on site.
 */
data class VisitorCenterState(
    val status: VisitorCenterStatus = VisitorCenterStatus.Operational,
    val occupied: Boolean = true,
)
