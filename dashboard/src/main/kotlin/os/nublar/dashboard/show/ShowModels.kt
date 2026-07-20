package os.nublar.dashboard.show

import os.nublar.dashboard.data.GateStatus
import os.nublar.dashboard.viewmodel.Screen

/**
 * A high-level action a timed show event performs. New capabilities land here
 * as new subtypes; [ShowController] fires them and the app wires each to a real
 * operation (navigate, highlight, fail a fence, …).
 */
sealed interface ShowAction {
    /** Highlight a specific networked machine on the splash screen (null clears). */
    data class HighlightMachine(val machine: String?) : ShowAction

    /** Switch the active screen — e.g. flip Nedry's computer between its modes. */
    data class ShowScreen(val screen: Screen) : ShowAction

    /** Select a paddock on the Island Map (null clears the selection). */
    data class SelectPaddock(val paddockId: String?) : ShowAction

    /** Fail (disarm) a paddock's fence, playing the disarm animation. */
    data class FailFence(val paddockId: String) : ShowAction

    /** Re-arm every paddock fence (reset). */
    data object RearmAllFences : ShowAction

    /**
     * Update the raptor pen's loading bay. Null fields leave that part of the
     * bay unchanged; [clearAlert] wipes the alert banner.
     */
    data class UpdateRaptorBay(
        val gate: GateStatus? = null,
        val loaderOffsetMeters: Int? = null,
        val lockEngaged: Boolean? = null,
        val alert: String? = null,
        val clearAlert: Boolean = false,
    ) : ShowAction
}

/** One event on the show timeline: [action] fires when playback passes [atSeconds]. */
data class TimedEvent(
    val atSeconds: Double,
    val label: String,
    val action: ShowAction,
)

/** Formats a number of seconds as m:ss (or h:mm:ss past an hour). */
fun formatTimecode(seconds: Double): String {
    val total = seconds.toLong().coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/**
 * Show timeline for Jurassic Park (1993). Timestamps are APPROXIMATE, measured
 * from the start of playback including studio logos — nudge them against your
 * cut/stream (the scrubber makes re-alignment easy).
 *
 * Opening scene: the raptor transfer at the pen. The gate is raised, the raptor
 * strikes and shoves the loader back off the gate, the loading lock lets go,
 * and the gatekeeper is attacked ("WORKER DOWN").
 */
val JURASSIC_PARK_TIMELINE: List<TimedEvent> = listOf(
    // Scene setup: bring up the paddock map, focus the raptor pen.
    TimedEvent(75.0, "Raptor transfer begins", ShowAction.ShowScreen(Screen.IslandMap)),
    TimedEvent(77.0, "Focus raptor pen", ShowAction.SelectPaddock("raptor-paddock")),
    // The loader is pushed against the gate and locked down.
    TimedEvent(150.0, "Loader approaching — lock open", ShowAction.UpdateRaptorBay(lockEngaged = false)),
    TimedEvent(168.0, "Loader docked — lock engaging", ShowAction.UpdateRaptorBay(lockEngaged = true)),
    // The transfer goes wrong.
    TimedEvent(195.0, "Loading gate raised", ShowAction.UpdateRaptorBay(gate = GateStatus.Opening)),
    TimedEvent(202.0, "Loading gate open", ShowAction.UpdateRaptorBay(gate = GateStatus.Open)),
    TimedEvent(210.0, "Raptor strikes — loader shoved", ShowAction.UpdateRaptorBay(loaderOffsetMeters = 2)),
    TimedEvent(218.0, "Loader pushed off gate — lock fails", ShowAction.UpdateRaptorBay(loaderOffsetMeters = 4, lockEngaged = false)),
    TimedEvent(228.0, "Gatekeeper attacked", ShowAction.UpdateRaptorBay(alert = "WORKER DOWN")),
    // Scene ends; stand the bay back down and release the focus.
    TimedEvent(330.0, "Bay secured", ShowAction.UpdateRaptorBay(gate = GateStatus.Closed, loaderOffsetMeters = 0, lockEngaged = true, clearAlert = true)),
    TimedEvent(332.0, "Clear pen focus", ShowAction.SelectPaddock(null)),
)

/** Jurassic Park runtime (2h07m), in seconds. */
const val MOVIE_DURATION_SECONDS: Double = 7620.0
