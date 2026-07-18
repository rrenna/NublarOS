package os.nublar.dashboard.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/** The ocean blue of island.png, sampled from the artwork, so the pane around the island blends in. */
val OceanBlue = Color(0xFF0570ED)

/**
 * A viewport that shows a fixed-size ([contentWidth] x [contentHeight]) map
 * and lets the user navigate it by **right-click-and-drag** panning plus the
 * mouse wheel. Toggleable via [panEnabled] (a future mouse-mode button can
 * flip it).
 *
 * This replaces stacking a pan gesture on top of `horizontalScroll` /
 * `verticalScroll`: those two fought the pan handler and made dragging shake.
 * Here the offset is owned directly, clamped to the content bounds, and the
 * pan gesture lives on the STATIONARY outer node — so pointer coordinates
 * never shift while the content moves, and there is no second scroll machine
 * to arbitrate with. Right-drag events are consumed in the Initial pass so
 * they never reach the map's own (left-click) selection/edit gestures.
 */
@Composable
fun MapViewport(
    contentWidth: Dp,
    contentHeight: Dp,
    modifier: Modifier = Modifier,
    panEnabled: Boolean = true,
    background: Color = OceanBlue,
    content: @Composable () -> Unit,
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .background(background)
            .pointerInput(panEnabled, contentWidth, contentHeight) {
                val contentW = contentWidth.toPx()
                val contentH = contentHeight.toPx()
                // Allow a bit of overscroll past the top/left edge (10% of the
                // map size) so the map isn't pinned flush into that corner.
                val overscrollX = contentW * 0.10f
                val overscrollY = contentH * 0.10f
                fun clamp(o: Offset): Offset {
                    val minX = (size.width - contentW).coerceAtMost(0f)
                    val minY = (size.height - contentH).coerceAtMost(0f)
                    return Offset(o.x.coerceIn(minX, overscrollX), o.y.coerceIn(minY, overscrollY))
                }
                awaitPointerEventScope {
                    var last: Offset? = null
                    while (true) {
                        // Initial pass: handle before the map's own gestures,
                        // and consume so right-drag / wheel never reach them.
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        when {
                            event.type == PointerEventType.Scroll -> {
                                val scroll = event.changes.fold(Offset.Zero) { acc, c -> acc + c.scrollDelta }
                                offset = clamp(offset - scroll * 50f)
                                event.changes.forEach { it.consume() }
                            }
                            panEnabled && event.buttons.isSecondaryPressed -> {
                                val position = event.changes.firstOrNull()?.position
                                if (position != null) {
                                    last?.let { prev -> offset = clamp(offset + (position - prev)) }
                                    last = position
                                    event.changes.forEach { it.consume() }
                                }
                            }
                            else -> last = null
                        }
                    }
                }
            },
    ) {
        // requiredSize forces the content to its true fixed size regardless
        // of the viewport's (possibly non-square) constraints — otherwise the
        // island bitmap stretches to the window's aspect ratio. Overflow is
        // clipped by clipToBounds above.
        Box(
            modifier = Modifier
                .offset {
                    // Re-clamp at render time so a content-size change (e.g. the
                    // preview's zoom buttons shrinking the map) can't leave the
                    // stored offset pointing at empty space off-screen.
                    val contentW = contentWidth.toPx()
                    val contentH = contentHeight.toPx()
                    val minX = (constraints.maxWidth - contentW).coerceAtMost(0f)
                    val minY = (constraints.maxHeight - contentH).coerceAtMost(0f)
                    IntOffset(
                        offset.x.coerceIn(minX, contentW * 0.10f).roundToInt(),
                        offset.y.coerceIn(minY, contentH * 0.10f).roundToInt(),
                    )
                }
                .requiredSize(contentWidth, contentHeight),
        ) {
            content()
        }
    }
}
