package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.dashboard.ui.map.IslandMap
import os.nublar.dashboard.ui.map.MapViewport
import os.nublar.dashboard.ui.map.loadPaddockCollection
import os.nublar.designsystem.NublarColors

/**
 * Close recreation of the film's "Raptor Paddock" island-map / vehicle
 * status screen. See README "Legal and Asset Guidelines" for the
 * fair-use/parody basis for this direct recreation. Redrawn from scratch —
 * an original terrain/paddock-boundary drawing and vehicle diagram in the
 * same visual style, not a traced copy or extracted film frame.
 */
@Composable
fun IslandMapView(
    onClose: () -> Unit,
    onSwitchScreen: () -> Unit = {},
    splitFraction: Float = 0.535f,
    onSplitFractionChange: (Float) -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NublarColors.MonitorGray)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DraggableSplitRow(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                splitFraction = splitFraction,
                onSplitFractionChange = onSplitFractionChange,
                left = { m -> PaddockMapPanel(modifier = m) },
                right = { m -> IslandRightColumn(modifier = m, onClose = onClose) },
            )
            BottomBar(screenLabel = "Animal Paddocks", onScreenClick = onSwitchScreen)
        }
    }
}

@Composable
private fun PaddockMapPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        val paddocks = remember { loadPaddockCollection().paddocks }
        MapViewport(
            contentWidth = 1200.dp,
            contentHeight = 1200.dp,
            panEnabled = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(NublarColors.DarkFrame)
                .padding(4.dp),
        ) {
            IslandMap(
                modifier = Modifier.size(1200.dp, 1200.dp),
                paddockShapes = paddocks,
            )
        }
        PaddockLevelBar()
    }
}


@Composable
private fun PaddockLevelBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NublarColors.HighlightYellow)
            .border(1.dp, Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PaddockLevelBadge()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Raptor Paddock",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 19.sp,
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth().background(Color.Black).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Quadrant: tecotsky9087",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 12.sp,
                )
            }
        }
        PaddockLevelBadge()
    }
}

@Composable
private fun PaddockLevelBadge() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text("LEVEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.width(56.dp).background(Color.Black).padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("G", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun IslandRightColumn(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TabRow(label = "VEHICLE", tabs = listOf("TOUR", "POWER", "TIME"))
        VehicleStatusPanel(modifier = Modifier.weight(1f).fillMaxWidth())
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
        TabRow(label = "GLITCHES", tabs = listOf("MAPS", "SYSTEM", "EMERG."))
        QuadrantLog(modifier = Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun VehicleStatusPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("vehicle status", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
            Row(
                modifier = Modifier
                    .background(Color.Black)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("headlights on", color = Color.White, fontStyle = FontStyle.Italic, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(NublarColors.StatusGreen),
                )
            }
        }

        VehicleDiagram(modifier = Modifier.weight(1f).fillMaxWidth())

        Text("13 mph", color = NublarColors.LabelCream, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        val expEntries = listOf(
            "EXP 4" to NublarColors.StatusGreen,
            "EXP 5" to NublarColors.StatusGreen,
            "EXP 6" to NublarColors.WarningRed,
            "EXP 7" to NublarColors.MapBlue,
        )
        expEntries.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.background(Color.Black).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            VehicleSilhouette(kind = VehicleKind.Van, modifier = Modifier.weight(1f).height(40.dp))
            VehicleSilhouette(kind = VehicleKind.Suv, modifier = Modifier.weight(1f).height(40.dp))
        }
    }
}

@Composable
private fun VehicleDiagram(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2f

        // Headlight cones.
        val cone = Path().apply {
            moveTo(w * 0.02f, cy - h * 0.10f)
            lineTo(w * 0.45f, cy - h * 0.04f)
            lineTo(w * 0.45f, cy + h * 0.04f)
            lineTo(w * 0.02f, cy + h * 0.10f)
            close()
        }
        drawPath(cone, color = NublarColors.HighlightYellow.copy(alpha = 0.5f))

        // Vehicle body, top-down.
        drawRoundRect(
            color = Color(0xFF9AA3AC),
            topLeft = Offset(w * 0.45f, cy - h * 0.16f),
            size = Size(w * 0.42f, h * 0.32f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f),
            style = Stroke(width = h * 0.02f),
        )
        drawLine(
            Color(0xFF9AA3AC),
            Offset(w * 0.60f, cy - h * 0.16f),
            Offset(w * 0.60f, cy + h * 0.16f),
            strokeWidth = h * 0.015f,
        )
        drawLine(
            Color(0xFF9AA3AC),
            Offset(w * 0.75f, cy - h * 0.16f),
            Offset(w * 0.75f, cy + h * 0.16f),
            strokeWidth = h * 0.015f,
        )
        drawCircle(NublarColors.WarningRed, radius = h * 0.02f, center = Offset(w * 0.90f, cy - h * 0.14f))
        drawCircle(NublarColors.WarningRed, radius = h * 0.02f, center = Offset(w * 0.90f, cy + h * 0.14f))
    }
}

private enum class VehicleKind { Van, Suv }

@Composable
private fun VehicleSilhouette(kind: VehicleKind, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = h * 0.06f, cap = StrokeCap.Round)
        when (kind) {
            VehicleKind.Van -> {
                drawRoundRect(
                    color = NublarColors.LabelCream,
                    topLeft = Offset(w * 0.08f, h * 0.15f),
                    size = Size(w * 0.84f, h * 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f),
                    style = stroke,
                )
                drawLine(NublarColors.LabelCream, Offset(w * 0.30f, h * 0.15f), Offset(w * 0.30f, h * 0.70f), h * 0.03f)
            }
            VehicleKind.Suv -> {
                val path = Path().apply {
                    moveTo(w * 0.10f, h * 0.70f)
                    lineTo(w * 0.10f, h * 0.35f)
                    lineTo(w * 0.30f, h * 0.15f)
                    lineTo(w * 0.75f, h * 0.15f)
                    lineTo(w * 0.92f, h * 0.35f)
                    lineTo(w * 0.92f, h * 0.70f)
                    close()
                }
                drawPath(path, color = NublarColors.LabelCream, style = stroke)
            }
        }
        drawCircle(NublarColors.DarkFrame, radius = h * 0.08f, center = Offset(w * 0.25f, h * 0.75f))
        drawCircle(NublarColors.DarkFrame, radius = h * 0.08f, center = Offset(w * 0.75f, h * 0.75f))
    }
}

private data class QuadrantEntry(val quadrant: String, val paddock: String, val failed: Boolean)

@Composable
private fun QuadrantLog(modifier: Modifier = Modifier) {
    val entries = listOf(
        QuadrantEntry("qp 81", "Gallimimus Paddock", failed = true),
        QuadrantEntry("qp 82", "Reserve Paddock", failed = true),
        QuadrantEntry("qp 83", "Reserve Paddock", failed = false),
        QuadrantEntry("qp 84", "Brachiosaurus Paddock", failed = false),
        QuadrantEntry("qp 85", "Triceratops Paddock", failed = false),
        QuadrantEntry("qp 86", "Tyrannosaurus Paddock", failed = false),
        QuadrantEntry("qp 87", "Raptor Paddock", failed = false),
        QuadrantEntry("qp 88", "Aviary Sector", failed = false),
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
                        .background(if (entry.failed) NublarColors.WarningRed else NublarColors.StatusGreen)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (entry.failed) "FAILED" else "CLEAR",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Quadrant: ${entry.quadrant} ${entry.paddock}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
