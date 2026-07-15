package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarType

/**
 * Classic beveled 3D border — light edge on top/left, dark edge on
 * bottom/right (or reversed for an inset/recessed look) — matching the
 * chunky System-7/SGI-style buttons and panels in the reference screen.
 */
internal fun Modifier.bevelBorder(
    width: androidx.compose.ui.unit.Dp = 1.5.dp,
    raised: Boolean = true,
    lightColor: Color = Color(0xFFC8CDD3),
    darkColor: Color = Color(0xFF2A2E33),
): Modifier = this.drawBehind {
    val strokePx = width.toPx()
    val topLeftColor = if (raised) lightColor else darkColor
    val bottomRightColor = if (raised) darkColor else lightColor
    drawLine(topLeftColor, Offset(0f, strokePx / 2f), Offset(size.width, strokePx / 2f), strokePx)
    drawLine(topLeftColor, Offset(strokePx / 2f, 0f), Offset(strokePx / 2f, size.height), strokePx)
    drawLine(
        bottomRightColor,
        Offset(0f, size.height - strokePx / 2f),
        Offset(size.width, size.height - strokePx / 2f),
        strokePx,
    )
    drawLine(
        bottomRightColor,
        Offset(size.width - strokePx / 2f, 0f),
        Offset(size.width - strokePx / 2f, size.height),
        strokePx,
    )
}

/**
 * Close recreation of the film's control-room "SYSTEM SECURED" plan-view
 * screen. See README "Legal and Asset Guidelines" for the fair-use/parody
 * basis for this direct recreation. The floor plan itself is an original
 * drawing in the same visual style (curved corridor, room blocks, tank
 * shapes) rather than a traced copy of the film's actual vector artwork.
 */
@Composable
fun ControlRoomPlanView(onClose: () -> Unit, onSwitchScreen: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NublarColors.MonitorGray)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val density = LocalDensity.current
                val dividerWidth = 10.dp
                val minPaneWidth = 220.dp
                val totalWidthPx = with(density) { maxWidth.toPx() }
                val dividerWidthPx = with(density) { dividerWidth.toPx() }
                val minPaneWidthPx = with(density) { minPaneWidth.toPx() }
                var leftWidthPx by remember(totalWidthPx) {
                    mutableStateOf((totalWidthPx - dividerWidthPx) * 0.535f)
                }
                val maxLeftWidthPx = (totalWidthPx - dividerWidthPx - minPaneWidthPx).coerceAtLeast(minPaneWidthPx)
                leftWidthPx = leftWidthPx.coerceIn(minPaneWidthPx, maxLeftWidthPx)

                Row(modifier = Modifier.fillMaxSize()) {
                    FloorPlanPanel(
                        modifier = Modifier.width(with(density) { leftWidthPx.toDp() }).fillMaxHeight(),
                    )
                    Box(
                        modifier = Modifier
                            .width(dividerWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 3.dp)
                            .background(NublarColors.DarkFrame)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    leftWidthPx = (leftWidthPx + delta).coerceIn(minPaneWidthPx, maxLeftWidthPx)
                                },
                            ),
                    )
                    RightColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClose = onClose,
                    )
                }
            }
            BottomBar(screenLabel = "Isla Nublar, Costa Rica", onScreenClick = onSwitchScreen)
        }
    }
}

@Composable
private fun FloorPlanPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF3F8F58))
                .padding(10.dp),
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                SidewaysSecuredLabel(modifier = Modifier.fillMaxHeight().width(22.dp))
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    SystemSecuredBanner()
                    val horizontalScroll = rememberScrollState()
                    val verticalScroll = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color.Black)
                            .horizontalScroll(horizontalScroll)
                            .verticalScroll(verticalScroll),
                    ) {
                        // Rendered larger than the viewport so there's
                        // actually room to pan around the plan, like
                        // panning a real drawing under a fixed screen.
                        FloorPlanCanvas(modifier = Modifier.size(1600.dp, 1600.dp))
                    }
                    SystemSecuredBanner()
                }
                SidewaysSecuredLabel(modifier = Modifier.fillMaxHeight().width(22.dp))
            }
        }
        LevelBar()
    }
}

@Composable
private fun SystemSecuredBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF3F8F58))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "SYSTEM SECURED",
            color = NublarColors.LabelCream,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }
}

/**
 * Rotates its single child 90°, measuring it against the *height* of the
 * available space rather than the (narrow) width — otherwise a rotated
 * Text is constrained to the pre-rotation box width and clips/wraps before
 * ever being rotated into its long axis.
 */
@Composable
private fun VerticalText(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val childConstraints = constraints.copy(
            minWidth = 0,
            maxWidth = constraints.maxHeight,
            minHeight = 0,
            maxHeight = constraints.maxWidth,
        )
        val placeable = measurables.first().measure(childConstraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width - placeable.height) / 2,
                y = -(placeable.height - placeable.width) / 2,
            )
        }
    }
}

@Composable
private fun SidewaysSecuredLabel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        VerticalText {
            Text(
                text = "SYSTEM SECURED",
                color = NublarColors.LabelCream,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.rotate(90f),
            )
        }
    }
}

/**
 * Original stylized floor-plan drawing: two large curved chambers (thick
 * wall outlines) linked by a diagonal corridor/bridge with stair hatching,
 * a rounded tank alcove, scattered furniture blocks, and keyhole "SECURED"
 * markers sitting tight at actual wall gaps — same structural language as
 * the reference (big rounded rooms + detail, not a grid of small rooms on
 * long corridor stubs), redrawn from scratch rather than traced geometry.
 */
@Composable
private fun FloorPlanCanvas(modifier: Modifier = Modifier) {
    val lineColor = Color.White
    val securedColor = Color(0xFF54D875)
    val textMeasurer = rememberTextMeasurer()
    val markerLabelStyle = TextStyle(
        color = securedColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun pt(x: Float, y: Float) = Offset(w * x, h * y)

        // Grid, matching the reference's clearly-visible blueprint grid.
        val gridColor = Color.White.copy(alpha = 0.35f)
        var gx = 0f
        while (gx < w) {
            drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
            gx += w / 22f
        }
        var gy = 0f
        while (gy < h) {
            drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
            gy += h / 22f
        }

        val wallWidth = w * 0.008f
        val wallStroke = Stroke(width = wallWidth)
        val furnitureStroke = Stroke(width = w * 0.004f)

        // Chamber A: large curved room, upper-left.
        val chamberA = Path().apply {
            moveTo(pt(0.06f, 0.42f).x, pt(0.06f, 0.42f).y)
            quadraticBezierTo(pt(0.00f, 0.15f).x, pt(0.00f, 0.15f).y, pt(0.20f, 0.04f).x, pt(0.20f, 0.04f).y)
            quadraticBezierTo(pt(0.45f, -0.03f).x, pt(0.45f, -0.03f).y, pt(0.63f, 0.09f).x, pt(0.63f, 0.09f).y)
            quadraticBezierTo(pt(0.73f, 0.17f).x, pt(0.73f, 0.17f).y, pt(0.68f, 0.32f).x, pt(0.68f, 0.32f).y)
            lineTo(pt(0.50f, 0.42f).x, pt(0.50f, 0.42f).y)
            quadraticBezierTo(pt(0.35f, 0.53f).x, pt(0.35f, 0.53f).y, pt(0.20f, 0.51f).x, pt(0.20f, 0.51f).y)
            quadraticBezierTo(pt(0.08f, 0.49f).x, pt(0.08f, 0.49f).y, pt(0.06f, 0.42f).x, pt(0.06f, 0.42f).y)
            close()
        }
        drawPath(chamberA, color = lineColor, style = wallStroke)

        // Chamber B: large curved room, lower-left.
        val chamberB = Path().apply {
            moveTo(pt(0.05f, 0.55f).x, pt(0.05f, 0.55f).y)
            quadraticBezierTo(pt(0.01f, 0.75f).x, pt(0.01f, 0.75f).y, pt(0.12f, 0.90f).x, pt(0.12f, 0.90f).y)
            quadraticBezierTo(pt(0.22f, 1.00f).x, pt(0.22f, 1.00f).y, pt(0.40f, 0.96f).x, pt(0.40f, 0.96f).y)
            quadraticBezierTo(pt(0.55f, 0.92f).x, pt(0.55f, 0.92f).y, pt(0.55f, 0.79f).x, pt(0.55f, 0.79f).y)
            lineTo(pt(0.42f, 0.62f).x, pt(0.42f, 0.62f).y)
            quadraticBezierTo(pt(0.30f, 0.55f).x, pt(0.30f, 0.55f).y, pt(0.18f, 0.56f).x, pt(0.18f, 0.56f).y)
            quadraticBezierTo(pt(0.10f, 0.56f).x, pt(0.10f, 0.56f).y, pt(0.05f, 0.55f).x, pt(0.05f, 0.55f).y)
            close()
        }
        drawPath(chamberB, color = lineColor, style = wallStroke)

        // Diagonal corridor/bridge strip connecting chamber A to the truss
        // structure, top-right — a parallelogram with stair hatching.
        val corridorNear1 = pt(0.55f, 0.33f)
        val corridorFar1 = pt(0.85f, 0.15f)
        val corridorNear2 = pt(0.61f, 0.43f)
        val corridorFar2 = pt(0.91f, 0.25f)
        drawLine(lineColor, corridorNear1, corridorFar1, strokeWidth = wallWidth)
        drawLine(lineColor, corridorNear2, corridorFar2, strokeWidth = wallWidth)
        drawLine(lineColor, corridorNear1, corridorNear2, strokeWidth = wallWidth)
        run {
            val steps = 6
            for (i in 1 until steps) {
                val t = i / steps.toFloat()
                val a = Offset(
                    corridorNear1.x + (corridorFar1.x - corridorNear1.x) * t,
                    corridorNear1.y + (corridorFar1.y - corridorNear1.y) * t,
                )
                val b = Offset(
                    corridorNear2.x + (corridorFar2.x - corridorNear2.x) * t,
                    corridorNear2.y + (corridorFar2.y - corridorNear2.y) * t,
                )
                drawLine(lineColor, a, b, strokeWidth = w * 0.0025f)
            }
        }

        // Truss/bridge structure at the corridor's far end.
        val truss = Path().apply {
            moveTo(pt(0.82f, 0.10f).x, pt(0.82f, 0.10f).y)
            lineTo(pt(0.97f, 0.14f).x, pt(0.97f, 0.14f).y)
            lineTo(pt(0.95f, 0.28f).x, pt(0.95f, 0.28f).y)
            lineTo(pt(0.80f, 0.24f).x, pt(0.80f, 0.24f).y)
            close()
        }
        drawPath(truss, color = lineColor, style = wallStroke)
        run {
            var t = 0.83f
            while (t < 0.95f) {
                drawLine(
                    lineColor,
                    pt(t, 0.11f),
                    pt(t - 0.02f, 0.27f),
                    strokeWidth = w * 0.002f,
                )
                t += 0.025f
            }
        }

        // Rounded tank alcove, lower-right.
        val alcove = Path().apply {
            moveTo(pt(0.55f, 0.55f).x, pt(0.55f, 0.55f).y)
            quadraticBezierTo(pt(0.75f, 0.49f).x, pt(0.75f, 0.49f).y, pt(0.90f, 0.59f).x, pt(0.90f, 0.59f).y)
            quadraticBezierTo(pt(0.99f, 0.67f).x, pt(0.99f, 0.67f).y, pt(0.92f, 0.80f).x, pt(0.92f, 0.80f).y)
            quadraticBezierTo(pt(0.84f, 0.91f).x, pt(0.84f, 0.91f).y, pt(0.66f, 0.88f).x, pt(0.66f, 0.88f).y)
            lineTo(pt(0.55f, 0.78f).x, pt(0.55f, 0.78f).y)
            close()
        }
        drawPath(alcove, color = lineColor, style = wallStroke)
        drawCircle(lineColor, radius = w * 0.02f, center = pt(0.74f, 0.68f), style = furnitureStroke)
        drawCircle(lineColor, radius = w * 0.02f, center = pt(0.80f, 0.76f), style = furnitureStroke)

        // Connect the alcove into the corridor/chamber network.
        drawLine(lineColor, pt(0.61f, 0.43f), pt(0.55f, 0.55f), strokeWidth = wallWidth)

        // Furniture blocks scattered inside the chambers — original detail,
        // not traced from any specific reference layout.
        val furniture = listOf(
            pt(0.13f, 0.11f) to androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.03f),
            pt(0.20f, 0.10f) to androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.03f),
            pt(0.30f, 0.19f) to androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.04f),
            pt(0.21f, 0.28f) to androidx.compose.ui.geometry.Size(w * 0.07f, h * 0.035f),
            pt(0.38f, 0.31f) to androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.03f),
            pt(0.30f, 0.75f) to androidx.compose.ui.geometry.Size(w * 0.06f, h * 0.03f),
            pt(0.14f, 0.68f) to androidx.compose.ui.geometry.Size(w * 0.05f, h * 0.035f),
        )
        furniture.forEach { (topLeft, fSize) ->
            drawRect(lineColor, topLeft = topLeft, size = fSize, style = furnitureStroke)
        }

        // Keyhole "SECURED" markers, sitting tight at wall gaps with a
        // short arrow and a small text label, per the reference.
        data class Marker(val gap: Offset, val arrowDelta: Offset, val labelOffset: Offset)
        val markers = listOf(
            Marker(pt(0.63f, 0.20f), Offset(-w * 0.03f, -w * 0.01f), Offset(w * 0.015f, -h * 0.02f)),
            Marker(pt(0.71f, 0.27f), Offset(-w * 0.025f, w * 0.015f), Offset(w * 0.015f, -h * 0.02f)),
            Marker(pt(0.55f, 0.34f), Offset(0f, w * 0.025f), Offset(-w * 0.06f, -h * 0.02f)),
            Marker(pt(0.72f, 0.45f), Offset(w * 0.02f, -w * 0.02f), Offset(w * 0.015f, h * 0.015f)),
            Marker(pt(0.17f, 0.56f), Offset(-w * 0.03f, 0f), Offset(-w * 0.03f, h * 0.03f)),
            Marker(pt(0.47f, 0.47f), Offset(-w * 0.02f, w * 0.02f), Offset(w * 0.015f, h * 0.015f)),
            Marker(pt(0.57f, 0.53f), Offset(-w * 0.025f, -w * 0.015f), Offset(-w * 0.07f, h * 0.02f)),
            Marker(pt(0.38f, 0.64f), Offset(w * 0.02f, w * 0.02f), Offset(w * 0.015f, h * 0.015f)),
        )
        markers.forEach { (center, arrowDelta, labelOffset) ->
            drawLine(securedColor, center, center + arrowDelta, strokeWidth = w * 0.003f, cap = StrokeCap.Round)
            drawCircle(securedColor, radius = w * 0.010f, center = center, style = Stroke(width = w * 0.0035f))
            drawRect(
                securedColor,
                topLeft = Offset(center.x - w * 0.0035f, center.y + w * 0.005f),
                size = androidx.compose.ui.geometry.Size(w * 0.007f, w * 0.010f),
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "SECURED",
                topLeft = center + labelOffset,
                style = markerLabelStyle,
            )
        }
    }
}

@Composable
private fun LevelBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NublarColors.HighlightYellow)
            .border(1.dp, Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LevelBadge()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Control Room / Plan View",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 19.sp,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "drawing number - nublar-plan-01",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }
        LevelBadge()
    }
}

@Composable
private fun LevelBadge() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
            modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text("LEVEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.width(56.dp).background(Color.Black).padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("2", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RightColumn(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TabRow(label = "VEHICLE", tabs = listOf("TOUR", "POWER", "TIME"))
        StatusVideoPanel(modifier = Modifier.weight(1f).fillMaxWidth())
        TransportControls(onClose = onClose)
        TabRow(label = "GLITCHES", tabs = listOf("MAPS", "SYSTEM", "EMERG."))
        GlitchesLog(modifier = Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
internal fun TabRow(label: String, tabs: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NublarColors.InsetPanel)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = NublarColors.LabelCream,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(end = 4.dp),
        )
        tabs.forEach { tab ->
            Box(
                modifier = Modifier
                    .background(NublarColors.MonitorGray)
                    .bevelBorder(raised = true)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(tab, color = NublarColors.DarkFrame, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Four-quadrant black/yellow pinwheel disc — the classic "test marker" icon style. */
@Composable
private fun RebootPinwheelIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawArc(Color.Black, startAngle = -90f, sweepAngle = 90f, useCenter = true, topLeft = center - Offset(radius, radius), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f))
        drawArc(NublarColors.HighlightYellow, startAngle = 0f, sweepAngle = 90f, useCenter = true, topLeft = center - Offset(radius, radius), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f))
        drawArc(Color.Black, startAngle = 90f, sweepAngle = 90f, useCenter = true, topLeft = center - Offset(radius, radius), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f))
        drawArc(NublarColors.HighlightYellow, startAngle = 180f, sweepAngle = 90f, useCenter = true, topLeft = center - Offset(radius, radius), size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f))
    }
}

@Composable
private fun StatusVideoPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(NublarColors.StatusGreen)
                .border(4.dp, Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            RebootPinwheelIcon(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .background(Color.White)
                .border(4.dp, Color.Black)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "REBOOTING SYSTEM...",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
            Text(
                "VOLUME --- NEDRYLAND JP",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            )
        }
    }
}

@Composable
private fun TransportControls(onClose: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ChunkyButton("HOLD", modifier = Modifier.weight(1f))
            ChunkyButton("QUIT", modifier = Modifier.weight(1f), onClick = onClose)
            ChunkyButton("NEW", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ChunkyButton("NEXT", modifier = Modifier.weight(1f))
            ChunkyButton("◄◄", modifier = Modifier.weight(1f))
            ChunkyButton("►►", modifier = Modifier.weight(1f))
            ChunkyButton("►", modifier = Modifier.weight(1f), highlight = true)
            ChunkyButton("■", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
internal fun ChunkyButton(
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .background(if (highlight) NublarColors.StatusGreen else NublarColors.MonitorGray)
            .bevelBorder(raised = true)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = NublarColors.DarkFrame, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

internal data class LogEntry(val text: String, val status: String = "CLEAR")

@Composable
private fun GlitchesLog(modifier: Modifier = Modifier) {
    val entries = listOf(
        LogEntry("Ldg - Volume - JP"),
        LogEntry("Boot Successful - CLEAR"),
        LogEntry("Format Gabber - Chaires"),
        LogEntry("Operator - Andres Ramirez dkhd"),
        LogEntry("Vidnet - Camera VC net01"),
        LogEntry("Communications = Active"),
        LogEntry("Grid Status - Nominal"),
        LogEntry("Perimeter Fences - Armed"),
        LogEntry("Auxiliary Power - Standby"),
        LogEntry("Tour Route B - Clear"),
        LogEntry("Environmental Control - Nominal"),
        LogEntry("Mini Array Capacity - 62%"),
    )
    Column(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
            .bevelBorder(raised = false, width = 2.dp)
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .background(NublarColors.StatusGreen)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        entry.status,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "- ${entry.text}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            }
        }
    }
}

@Composable
internal fun BottomBar(screenLabel: String, onScreenClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .background(NublarColors.MonitorGray)
                .bevelBorder(raised = true)
                .clickable(onClick = onScreenClick)
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    "SCREEN",
                    color = NublarColors.LabelCream,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 13.sp,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(NublarColors.LabelCream)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    screenLabel,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 14.sp,
                )
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .border(1.dp, Color.Black)
                .bevelBorder(raised = false, width = 1.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    "SYS ICONS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 13.sp,
                )
            }
            SysIcon("Macintosh HD", SysIconKind.HardDrive)
            SysIcon("MiniArray 1 Gi..", SysIconKind.MiniArray)
            SysIcon("MiniArray 1 Gi..", SysIconKind.MiniArray)
            SysIcon("Trash", SysIconKind.Trash)
        }
    }
}

internal enum class SysIconKind { HardDrive, MiniArray, Trash }

@Composable
internal fun SysIcon(label: String, kind: SysIconKind) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(32.dp, 24.dp)) {
            val w = size.width
            val h = size.height
            when (kind) {
                SysIconKind.HardDrive -> {
                    drawRect(
                        Color.White,
                        topLeft = Offset(w * 0.05f, h * 0.15f),
                        size = androidx.compose.ui.geometry.Size(w * 0.90f, h * 0.55f),
                    )
                    drawRect(
                        Color.Black,
                        topLeft = Offset(w * 0.05f, h * 0.15f),
                        size = androidx.compose.ui.geometry.Size(w * 0.90f, h * 0.55f),
                        style = Stroke(width = h * 0.06f),
                    )
                    drawRect(
                        Color.Black,
                        topLeft = Offset(w * 0.10f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.10f),
                    )
                }

                SysIconKind.MiniArray -> {
                    val diskColor = Color(0xFF8A8F96)
                    drawOval(
                        diskColor,
                        topLeft = Offset(w * 0.10f, h * 0.10f),
                        size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.55f),
                    )
                    drawRect(
                        diskColor,
                        topLeft = Offset(w * 0.10f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.30f),
                    )
                    drawOval(
                        diskColor,
                        topLeft = Offset(w * 0.10f, h * 0.35f),
                        size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.30f),
                        style = Stroke(width = h * 0.03f),
                    )
                    // Dithered texture: alternating dots, like a 1-bit fill.
                    var yy = h * 0.16f
                    var row = 0
                    while (yy < h * 0.62f) {
                        var xx = w * 0.14f + if (row % 2 == 0) 0f else w * 0.06f
                        while (xx < w * 0.86f) {
                            drawCircle(Color.White, radius = w * 0.012f, center = Offset(xx, yy))
                            xx += w * 0.12f
                        }
                        yy += h * 0.10f
                        row++
                    }
                    drawOval(
                        Color.Black,
                        topLeft = Offset(w * 0.42f, h * 0.30f),
                        size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.14f),
                        style = Stroke(width = h * 0.025f),
                    )
                }

                SysIconKind.Trash -> {
                    val bodyTop = h * 0.30f
                    drawLine(Color.Black, Offset(w * 0.22f, bodyTop), Offset(w * 0.30f, h * 0.85f), h * 0.035f)
                    drawLine(Color.Black, Offset(w * 0.78f, bodyTop), Offset(w * 0.70f, h * 0.85f), h * 0.035f)
                    drawLine(Color.Black, Offset(w * 0.30f, h * 0.85f), Offset(w * 0.70f, h * 0.85f), h * 0.035f)
                    drawLine(Color.Black, Offset(w * 0.24f, bodyTop), Offset(w * 0.76f, bodyTop), h * 0.05f)
                    drawLine(Color.Black, Offset(w * 0.16f, h * 0.20f), Offset(w * 0.84f, h * 0.20f), h * 0.05f)
                    drawLine(Color.Black, Offset(w * 0.40f, h * 0.10f), Offset(w * 0.60f, h * 0.10f), h * 0.04f)
                    drawLine(Color.Black, Offset(w * 0.38f, h * 0.40f), Offset(w * 0.38f, h * 0.75f), h * 0.025f)
                    drawLine(Color.Black, Offset(w * 0.50f, h * 0.40f), Offset(w * 0.50f, h * 0.75f), h * 0.025f)
                    drawLine(Color.Black, Offset(w * 0.62f, h * 0.40f), Offset(w * 0.62f, h * 0.75f), h * 0.025f)
                }
            }
        }
        Text(label, color = Color.Black, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}
