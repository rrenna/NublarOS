package os.nublar.dashboard.show

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

/**
 * Drives show playback for movie-sync: a manual transport clock (play / pause /
 * seek) over a [timeline] of [TimedEvent]s. As the clock advances past an
 * event's time, [onAction] fires that event's action. Seeking never fires
 * events — only forward playback across a timestamp does, so scrubbing to align
 * with the movie is safe.
 *
 * State is Compose snapshot state; the wall-clock driver lives in [ShowClock].
 */
class ShowController(
    val timeline: List<TimedEvent>,
    val durationSeconds: Double,
    private val onAction: (ShowAction) -> Unit,
) {
    var elapsedSeconds by mutableStateOf(0.0)
        private set

    var playing by mutableStateOf(false)
        private set

    /** 0..1 progress through the show. */
    val progress: Float
        get() = if (durationSeconds <= 0.0) 0f else (elapsedSeconds / durationSeconds).toFloat().coerceIn(0f, 1f)

    fun play() {
        if (elapsedSeconds >= durationSeconds) elapsedSeconds = 0.0
        playing = true
    }

    fun pause() {
        playing = false
    }

    fun togglePlay() {
        if (playing) pause() else play()
    }

    fun reset() {
        playing = false
        elapsedSeconds = 0.0
    }

    /** Jump to [seconds] WITHOUT firing any events (used to align with the film). */
    fun seek(seconds: Double) {
        elapsedSeconds = seconds.coerceIn(0.0, durationSeconds)
    }

    /** Advance the clock by [dtSeconds], firing any events crossed in (prev, next]. */
    fun advance(dtSeconds: Double) {
        if (dtSeconds <= 0.0) return
        val prev = elapsedSeconds
        val next = (prev + dtSeconds).coerceAtMost(durationSeconds)
        elapsedSeconds = next
        timeline.forEach { event ->
            if (event.atSeconds > prev && event.atSeconds <= next) onAction(event.action)
        }
        if (next >= durationSeconds) playing = false
    }
}

/**
 * Wall-clock driver: while [controller] is playing, advances it by the real
 * elapsed time each frame. Place once at the app root so events fire regardless
 * of which screen is visible.
 */
@Composable
fun ShowClock(controller: ShowController) {
    if (!controller.playing) return
    androidx.compose.runtime.LaunchedEffect(controller.playing) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            controller.advance((now - last) / 1_000_000_000.0)
            last = now
        }
    }
}
