package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.designsystem.NublarFonts
import kotlin.math.roundToInt

/** Formats seconds as the stopwatch's HH:MM:SS.t (tenths) display. */
private fun formatStopwatch(seconds: Double): String {
    val tenths = (seconds * 10).toLong().coerceAtLeast(0)
    val h = tenths / 36000
    val m = (tenths / 600) % 60
    val s = (tenths / 10) % 60
    val t = tenths % 10
    return "%02d:%02d:%02d.%d".format(h, m, s, t)
}

/**
 * Dennis Nedry's desktop "StopWatch" window — an original recreation in the
 * System-7 Macintosh style: a small draggable window with a running time and
 * Reset / Go / Stop controls. Functional (Go counts up, Stop pauses, Reset
 * zeroes). Rendered as an overlay; [onClose] dismisses it.
 */
@Composable
fun NedryStopwatchWindow(onClose: () -> Unit, modifier: Modifier = Modifier) {
    var elapsed by remember { mutableStateOf(0.0) }
    var running by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            elapsed += (now - last) / 1_000_000_000.0
            last = now
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .shadow(10.dp)
            .width(280.dp)
            .background(Color.White)
            .border(1.dp, Color.Black),
    ) {
        Column {
            // Title bar: classic Mac striped bar, close box on the left, centered title.
            TitleBar(
                title = "StopWatch",
                onClose = onClose,
                onDrag = { offset += it },
            )
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TimeDisplay(elapsed, modifier = Modifier.weight(1f))
                    MacButton("Reset", shortcut = "⌘R") {
                        running = false
                        elapsed = 0.0
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacButton("Go", shortcut = "⌘G", default = true, modifier = Modifier.weight(1f)) {
                        running = true
                    }
                    MacButton("Stop", shortcut = "⌘S", modifier = Modifier.weight(1f)) {
                        running = false
                    }
                }
            }
        }
    }
}

@Composable
private fun TitleBar(title: String, onClose: () -> Unit, onDrag: (Offset) -> Unit) {
    Box(
        modifier = Modifier
            .height(20.dp)
            .background(Color(0xFFDDDDDD))
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    onDrag(drag)
                }
            }
            // Classic Mac title-bar "racing stripes".
            .drawBehind {
                val gap = size.height / 7f
                for (i in 1..6) {
                    val y = gap * i
                    drawLine(Color(0xFF888888), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Close box on the left.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .size(11.dp)
                .background(Color.White)
                .border(1.dp, Color.Black)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickableNoRipple(onClose),
        )
        // Title, on a white plate so the stripes don't run through it.
        Box(modifier = Modifier.background(Color(0xFFDDDDDD)).padding(horizontal = 8.dp)) {
            Text(title, color = Color.Black, fontFamily = NublarFonts.Ui, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TimeDisplay(elapsed: Double, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            formatStopwatch(elapsed),
            color = Color.Black,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun MacButton(
    label: String,
    shortcut: String,
    modifier: Modifier = Modifier,
    default: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF2F2F2))
            .border(if (default) 2.dp else 1.dp, Color.Black, RoundedCornerShape(6.dp))
            .pointerHoverIcon(PointerIcon.Hand)
            .clickableNoRipple(onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        Text(shortcut, color = Color(0xFF666666), fontSize = 11.sp)
    }
}

/** Clickable without the material ripple (keeps the flat retro look). */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.pointerInput(onClick) {
        detectTapGestures(onTap = { onClick() })
    }
