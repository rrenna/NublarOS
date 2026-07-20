package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.dashboard.show.ShowController
import os.nublar.dashboard.show.TimedEvent
import os.nublar.dashboard.show.formatTimecode
import os.nublar.dashboard.show.launchMovie
import os.nublar.designsystem.NublarColors

/**
 * Show-sync control screen: a manual transport clock synced by hand to the movie
 * being watched. Play when the film starts, scrub to re-align, and the timeline
 * events fire as the clock passes them.
 */
@Composable
fun ShowSyncView(controller: ShowController, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NublarColors.MonitorGray)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            Text("SHOW SYNC", color = NublarColors.LabelCream, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            // Timecode readout.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    formatTimecode(controller.elapsedSeconds),
                    color = if (controller.playing) NublarColors.StatusGreen else NublarColors.LabelCream,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                )
                Text("/ ${formatTimecode(controller.durationSeconds)}", color = NublarColors.LabelCream, fontSize = 16.sp)
            }

            Scrubber(controller)

            // Transport controls. LAUNCH FILM opens the movie in the macOS TV
            // app (deep link) and starts the sync clock in the same press;
            // scrub afterward to fine-align if playback took a moment to start.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChunkyButton(
                    "LAUNCH FILM",
                    modifier = Modifier.width(120.dp),
                    onClick = {
                        launchMovie()
                        controller.play()
                    },
                )
                ChunkyButton(
                    if (controller.playing) "PAUSE" else "PLAY",
                    modifier = Modifier.width(120.dp),
                    highlight = controller.playing,
                    onClick = { controller.togglePlay() },
                )
                ChunkyButton("RESET", modifier = Modifier.width(120.dp), onClick = { controller.reset() })
                Spacer(Modifier.weight(1f))
                ChunkyButton("DONE", modifier = Modifier.width(120.dp), onClick = onClose)
            }

            Spacer(Modifier.height(4.dp))
            Text("TIMELINE", color = NublarColors.LabelCream, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            EventList(controller, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

/** A clickable/seekable progress bar with a fill and event tick marks. */
@Composable
private fun Scrubber(controller: ShowController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(NublarColors.DarkFrame)
            .bevelBorder(raised = false, width = 2.dp)
            .pointerInput(controller.durationSeconds) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    controller.seek(fraction * controller.durationSeconds)
                }
            },
    ) {
        // Progress fill.
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(controller.progress)
                .background(NublarColors.MapBlue),
        )
        // Event ticks.
        Box(Modifier.fillMaxSize()) {
            controller.timeline.forEach { event ->
                val frac = (event.atSeconds / controller.durationSeconds).coerceIn(0.0, 1.0)
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .padding(vertical = 3.dp)
                        .offsetFraction(frac.toFloat())
                        .background(NublarColors.HighlightYellow),
                )
            }
        }
        // Playhead.
        Box(
            Modifier
                .fillMaxHeight()
                .width(3.dp)
                .offsetFraction(controller.progress)
                .background(NublarColors.LabelCream),
        )
    }
}

/** Positions a thin marker at [fraction] of the parent width. */
private fun Modifier.offsetFraction(fraction: Float): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val x = ((constraints.maxWidth - placeable.width) * fraction).toInt()
        layout(constraints.maxWidth, placeable.height) { placeable.place(x, 0) }
    }

@Composable
private fun EventList(controller: ShowController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.DarkFrame)
            .bevelBorder(raised = false, width = 2.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        controller.timeline.sortedBy { it.atSeconds }.forEach { event ->
            EventRow(event, past = controller.elapsedSeconds >= event.atSeconds)
        }
    }
}

@Composable
private fun EventRow(event: TimedEvent, past: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (past) Modifier.border(1.dp, NublarColors.StatusGreen.copy(alpha = 0.5f)) else Modifier)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            formatTimecode(event.atSeconds),
            color = if (past) NublarColors.StatusGreen else NublarColors.HighlightYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.width(56.dp),
        )
        Text(
            event.label,
            color = if (past) NublarColors.LabelCream else NublarColors.LabelCream.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )
    }
}
