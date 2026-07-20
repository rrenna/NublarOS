package os.nublar.dashboard.data

/** State of the raptor pen's loading gate. */
enum class GateStatus(val label: String) {
    Closed("CLOSED"),
    Opening("OPENING"),
    Open("OPEN"),
}

/**
 * The raptor pen's loading bay — the gate, the loader (the caged transport
 * unit pushed against it), and the loading lock. Backs the loading-bay status
 * panel shown when the Raptor Paddock is selected; the opening-scene script
 * (show-sync timeline or the Events menu) drives these values to mimic the
 * film's failed transfer.
 */
data class LoadingBayState(
    val gate: GateStatus = GateStatus.Closed,
    /** How far the loader has been pushed back from the gate (0 = docked). */
    val loaderOffsetMeters: Int = 0,
    /** Whether the loading lock is engaged. */
    val lockEngaged: Boolean = true,
    /** Active alert banner text (e.g. "WORKER DOWN"), or null when clear. */
    val alert: String? = null,
)
