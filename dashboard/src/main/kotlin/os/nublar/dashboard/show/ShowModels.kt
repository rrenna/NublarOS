package os.nublar.dashboard.show

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

    /** Fail (disarm) a paddock's fence, playing the disarm animation. */
    data class FailFence(val paddockId: String) : ShowAction

    /** Re-arm every paddock fence (reset). */
    data object RearmAllFences : ShowAction
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
 * A sample timeline for development, keyed to a short demo rather than a real
 * film. Replace with movie-accurate timestamps once syncing to an actual watch.
 */
val DEMO_TIMELINE: List<TimedEvent> = listOf(
    TimedEvent(5.0, "Highlight Nedry's machine", ShowAction.HighlightMachine("Dennis Nedry")),
    TimedEvent(12.0, "Nedry → Control Room", ShowAction.ShowScreen(Screen.ControlRoomPlanView)),
    TimedEvent(20.0, "Nedry → Animal Paddocks", ShowAction.ShowScreen(Screen.IslandMap)),
    TimedEvent(28.0, "T. rex fence fails", ShowAction.FailFence("tyrannosaurus-paddock")),
    TimedEvent(38.0, "Raptor pen: n/a (building)", ShowAction.FailFence("segisaurus-proceratosaurus-paddock")),
    TimedEvent(48.0, "Nedry → Jurassic Park System", ShowAction.ShowScreen(Screen.JurassicParkSystem)),
    TimedEvent(58.0, "Clear machine highlight", ShowAction.HighlightMachine(null)),
)

/** Default show length (seconds) — a bit past the last demo event. */
const val DEMO_DURATION_SECONDS: Double = 75.0
