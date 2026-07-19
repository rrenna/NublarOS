package os.nublar.stormtrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * EarthWatch weather computer — an original recreation, in the film's IRIX/Motif
 * visual language, of the park's storm-tracking workstation. Two live viewports
 * (the 3D storm "Animation" and a top-down "Island" satellite view) over a dense
 * control deck of dials, sliders, sector selection, and status readouts.
 *
 * First pass: the full layout and widget set are in place; most controls are
 * presentational for now (static positions), to be made interactive next.
 */

// ---- Motif palette (local to this screen's distinct look) --------------------
private val EwPanel = Color(0xFFBFC2B6)      // warm light gray panel field
private val EwPanelDark = Color(0xFFA9AC9F)
private val EwBevelLight = Color(0xFFEEEFE7)
private val EwBevelDark = Color(0xFF6C6D63)
private val EwText = Color(0xFF23241F)
private val EwTextDim = Color(0xFF5C5D54)
private val EwDialFace = Color(0xFF8FD2EF)    // light cyan dial face
private val EwDialLine = Color(0xFF2C6E8E)
private val EwSelectGreen = Color(0xFF87E39A)
private val EwHeaderBlue = Color(0xFF8FA0E8)
private val EwArrowRed = Color(0xFFD65A3A)
private val EwArrowPink = Color(0xFFE58AD6)
private val EwSea = Color(0xFF2E8BD8)
private val EwLand = Color(0xFF6FA85A)
private val EwSideSky = Color(0xFF12386E)

/** Motif 3D bevel: raised = light top-left / dark bottom-right; sunken = reversed. */
private fun Modifier.motifBevel(raised: Boolean = true, width: Dp = 2.dp): Modifier = drawBehind {
    val s = width.toPx()
    val tl = if (raised) EwBevelLight else EwBevelDark
    val br = if (raised) EwBevelDark else EwBevelLight
    drawRect(tl, Offset(0f, 0f), Size(size.width, s))
    drawRect(tl, Offset(0f, 0f), Size(s, size.height))
    drawRect(br, Offset(0f, size.height - s), Size(size.width, s))
    drawRect(br, Offset(size.width - s, 0f), Size(s, size.height))
}

@Composable
fun EarthWatchView(
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {},
    viewModel: EarthWatchViewModel = remember { EarthWatchViewModel() },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Top: the two live viewports (10% shorter than before).
        Row(
            modifier = Modifier.weight(0.468f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            WindowFrame("Animation", modifier = Modifier.weight(1f).fillMaxHeight()) {
                StormVideoView(modifier = Modifier.fillMaxSize())
            }
            WindowFrame("Island", modifier = Modifier.weight(1f).fillMaxHeight()) {
                IslandView(modifier = Modifier.fillMaxSize())
            }
        }

        // Bottom: the control deck.
        Column(
            modifier = Modifier
                .weight(0.532f)
                .fillMaxWidth()
                .background(EwPanel)
                .motifBevel(raised = true)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ControlDeck(modifier = Modifier.weight(1f).fillMaxWidth(), onExit = onExit, viewModel = viewModel)
        }
    }
}

// ---- Window frame (Motif title bar) -----------------------------------------
@Composable
private fun WindowFrame(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.background(EwPanel).motifBevel(raised = true).padding(2.dp)) {
        // Title bar.
        Row(
            modifier = Modifier.fillMaxWidth().background(EwPanel).motifBevel(raised = true).padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(18.dp, 14.dp).background(EwPanelDark).motifBevel(raised = true))
            Spacer(Modifier.width(6.dp))
            Text(title, color = EwText, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(14.dp).background(EwPanel).motifBevel(raised = true))
            Spacer(Modifier.width(3.dp))
            Box(Modifier.size(14.dp).background(EwPanel).motifBevel(raised = true))
        }
        Spacer(Modifier.height(2.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth().motifBevel(raised = false)) { content() }
    }
}

/** Loads a bundled PNG resource into an ImageBitmap. */
private fun loadBitmap(resource: String): ImageBitmap {
    val bytes = object {}.javaClass.classLoader.getResourceAsStream(resource)!!.readBytes()
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

/** Ocean blue sampled from island.png, so letterboxed empty space reads as water. */
private val IslandOcean = Color(0xFF0570ED)

// ---- Top-down island viewport ------------------------------------------------
@Composable
private fun IslandView(modifier: Modifier = Modifier) {
    val island = remember { loadBitmap("island.png") }
    // Fit (not crop) so the whole island is visible, zoomed out; the ocean-blue
    // background fills any letterboxed space so it looks like open water.
    Image(
        bitmap = island,
        contentDescription = "Isla Nublar",
        modifier = modifier.background(IslandOcean),
        contentScale = ContentScale.Fit,
    )
}

// ---- Control deck ------------------------------------------------------------
@Composable
private fun ControlDeck(
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {},
    viewModel: EarthWatchViewModel,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Column 1: out-the-window dials + display sliders.
        Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TitledSection("Out the Window View", Modifier.weight(1f).fillMaxWidth()) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LabeledDial("Vertical View", Modifier.weight(1f).fillMaxHeight()) { VerticalViewDial(Modifier.fillMaxSize()) }
                    LabeledDial("Horizontal View", Modifier.weight(1f).fillMaxHeight()) { HorizontalViewDial(Modifier.fillMaxSize()) }
                }
            }
            TitledSection("Display Parameters", Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    LabeledSlider("Vertical Stretch", "7", 0.5f)
                    LabeledSlider("Resolution", "10", 0.65f)
                    LabeledSlider("Field of View", null, 0.55f)
                    LabeledSlider("Visibility", null, 0.15f)
                }
            }
        }

        // Column 2: vertical direction gauge + altitude scale.
        Column(Modifier.weight(0.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TitledSection("Vertical Direction", Modifier.weight(1f).fillMaxWidth()) {
                VerticalScaleGauge(Modifier.fillMaxSize().padding(4.dp))
            }
            TitledSection("Altitude (km)", Modifier.weight(1f).fillMaxWidth()) {
                AltitudeScale(Modifier.fillMaxSize().padding(4.dp))
            }
        }

        // Column 3 (center): status banner, horizontal compass + buttons, side view.
        Column(Modifier.weight(1.5f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            StatusBanner()
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TitledSection("Horizontal Direction", Modifier.weight(1f).fillMaxHeight()) {
                    Column(Modifier.fillMaxSize().padding(4.dp)) {
                        Compass(Modifier.weight(1f).fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        ToggleButton("Fixed View", true)
                        Spacer(Modifier.height(2.dp))
                        ToggleButton("Fixed Position", true)
                        Spacer(Modifier.height(2.dp))
                        MotifButton("Save as Defaults", Modifier.fillMaxWidth())
                    }
                }
                TitledSection("Sectors", Modifier.weight(1f).fillMaxHeight()) {
                    SectorPanel(Modifier.fillMaxSize().padding(4.dp), onExit = onExit)
                }
            }
            TitledSection("Side View", Modifier.height(96.dp).fillMaxWidth()) {
                SideViewProfile(Modifier.fillMaxSize())
            }
        }

        // Column 4 (right): data parameters, top-view scale, clock.
        Column(Modifier.weight(1.1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TitledSection("Data Parameters", Modifier.weight(1.3f).fillMaxWidth()) {
                DataParameters(Modifier.fillMaxSize().padding(4.dp), viewModel = viewModel)
            }
            TitledSection("Top View Scale", Modifier.weight(0.6f).fillMaxWidth()) {
                Row(Modifier.fillMaxSize().padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MotifButton("Reset")
                    ValueBox("28", Modifier.width(40.dp))
                    SliderTrack(0.4f, Modifier.weight(1f).height(16.dp))
                }
            }
            TitledSection("", Modifier.weight(0.8f).fillMaxWidth()) {
                ClockPanel(Modifier.fillMaxSize().padding(6.dp))
            }
        }
    }
}

// ---- Section / building blocks ----------------------------------------------
@Composable
private fun TitledSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (title.isNotEmpty()) {
            Text(title, color = EwText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(EwPanel).motifBevel(raised = false)) { content() }
    }
}

@Composable
private fun LabeledDial(label: String, modifier: Modifier = Modifier, dial: @Composable () -> Unit) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = EwText, fontSize = 11.sp)
        Box(Modifier.weight(1f).fillMaxWidth().padding(2.dp)) { dial() }
    }
}

@Composable
private fun MotifButton(
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color = EwText,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .background(EwPanel)
            .motifBevel(raised = true)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = textColor, fontSize = 12.sp)
    }
}

@Composable
private fun ToggleButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().background(EwPanel).motifBevel(raised = true).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).background(if (on) EwBevelDark else EwPanelDark).motifBevel(raised = false))
        Spacer(Modifier.width(6.dp))
        Text(label, color = EwText, fontSize = 12.sp)
    }
}

@Composable
private fun ValueBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFDDDDD4)).motifBevel(raised = false).padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text, color = EwText, fontSize = 12.sp)
    }
}

@Composable
private fun SliderTrack(fraction: Float, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(EwPanelDark).motifBevel(raised = false)) {
        // Raised thumb positioned along the track.
        Box(
            Modifier.fillMaxHeight().width(16.dp)
                .padding(horizontal = 1.dp)
                .background(EwPanel).motifBevel(raised = true),
        )
    }
}

@Composable
private fun LabeledSlider(label: String, value: String?, fraction: Float) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = EwText, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (value != null) ValueBox(value, Modifier.width(34.dp))
            SliderTrack(fraction, Modifier.weight(1f).height(16.dp))
        }
    }
}

// ---- Status banner -----------------------------------------------------------
@Composable
private fun StatusBanner() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Current EarthWatch Status", color = EwText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier.fillMaxWidth().height(24.dp)
                .background(Color(0xFFAFB6EA)).motifBevel(raised = false),
        )
    }
}

// ---- Dials -------------------------------------------------------------------
/** Quarter-arc pitch dial ("Up / Ahead / Down"). */
@Composable
private fun VerticalViewDial(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(EwPanel)) {
        // Size the radius to fit the cell: the ±80° sweep spans ~2r vertically
        // and bulges ~0.17r left of center, so fit against both dimensions.
        val margin = size.minDimension * 0.08f
        val rByHeight = size.height / 2f - margin
        val rByWidth = (size.width - 2f * margin) / 1.17f
        val r = minOf(rByHeight, rByWidth).coerceAtLeast(1f)
        val c = Offset(margin + r * 0.17f, size.height / 2f)
        // Face: quarter disc.
        val face = Path().apply {
            moveTo(c.x, c.y)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(c.x - r, c.y - r, c.x + r, c.y + r),
                startAngleDegrees = -80f, sweepAngleDegrees = 160f, forceMoveTo = false,
            )
            close()
        }
        drawPath(face, EwDialFace)
        drawPath(face, EwDialLine, style = Stroke(width = 1.5f))
        // Needle pointing "Ahead".
        drawLine(EwArrowRed, c, Offset(c.x + r * 0.75f, c.y - r * 0.05f), strokeWidth = 3f)
    }
}

/** Round yaw/attitude dial ("Forward / Left / Right / Backward") with an up arrow. */
@Composable
private fun HorizontalViewDial(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(EwPanel)) {
        val r = size.minDimension * 0.44f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(EwDialFace, r, c)
        drawCircle(EwDialLine, r, c, style = Stroke(width = 1.5f))
        // Tick marks around.
        for (i in 0 until 12) {
            val a = i * (Math.PI * 2 / 12)
            val o1 = Offset(c.x + (r * 0.85f * cos(a)).toFloat(), c.y + (r * 0.85f * sin(a)).toFloat())
            val o2 = Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat())
            drawLine(EwDialLine, o1, o2, strokeWidth = 1f)
        }
        // Up arrow (forward).
        drawLine(EwArrowRed, Offset(c.x, c.y + r * 0.5f), Offset(c.x, c.y - r * 0.6f), strokeWidth = 3f)
        val head = Path().apply {
            moveTo(c.x, c.y - r * 0.7f)
            lineTo(c.x - r * 0.12f, c.y - r * 0.45f)
            lineTo(c.x + r * 0.12f, c.y - r * 0.45f)
            close()
        }
        drawPath(head, EwArrowRed)
    }
}

/** Round compass with S/W/N/E letters and a red north needle. */
@Composable
private fun Compass(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.44f
        val c = Offset(size.width / 2f, size.height / 2f)
        drawCircle(EwDialFace, r, c)
        drawCircle(EwDialLine, r, c, style = Stroke(width = 2f))
        for (i in 0 until 24) {
            val a = i * (Math.PI * 2 / 24)
            val inner = if (i % 6 == 0) 0.78f else 0.88f
            drawLine(
                EwDialLine,
                Offset(c.x + (r * inner * cos(a)).toFloat(), c.y + (r * inner * sin(a)).toFloat()),
                Offset(c.x + (r * cos(a)).toFloat(), c.y + (r * sin(a)).toFloat()),
                strokeWidth = 1f,
            )
        }
        // North needle.
        drawLine(EwArrowRed, Offset(c.x, c.y + r * 0.55f), Offset(c.x, c.y - r * 0.65f), strokeWidth = 3f)
        val head = Path().apply {
            moveTo(c.x, c.y - r * 0.72f)
            lineTo(c.x - r * 0.1f, c.y - r * 0.5f)
            lineTo(c.x + r * 0.1f, c.y - r * 0.5f)
            close()
        }
        drawPath(head, EwArrowRed)
    }
}

// ---- Vertical scale gauge ----------------------------------------------------
@Composable
private fun VerticalScaleGauge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(EwPanel).motifBevel(raised = false)) {
        Canvas(Modifier.fillMaxSize().padding(4.dp)) {
            val cx = size.width * 0.5f
            val top = size.height * 0.06f
            val bot = size.height * 0.94f
            drawLine(EwText, Offset(cx, top), Offset(cx, bot), strokeWidth = 2f)
            val labels = listOf(90, 60, 30, 0, 30, 60, 90)
            labels.forEachIndexed { i, _ ->
                val y = top + (bot - top) * i / (labels.size - 1)
                drawLine(EwText, Offset(cx - 6, y), Offset(cx + 6, y), strokeWidth = 2f)
            }
            // Indicator near -30.
            val yInd = top + (bot - top) * 4.4f / 6f
            drawLine(Color(0xFF3B57C8), Offset(cx - 8, yInd), Offset(cx + 8, yInd), strokeWidth = 3f)
        }
    }
}

// ---- Altitude scale ----------------------------------------------------------
@Composable
private fun AltitudeScale(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(Modifier.width(40.dp).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ValueBox("66", Modifier.fillMaxWidth())
            Box(Modifier.weight(1f).width(16.dp).background(EwPanelDark).motifBevel(raised = false)) {
                Box(Modifier.fillMaxWidth().height(20.dp).background(EwPanel).motifBevel(raised = true))
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("Scale", color = EwTextDim, fontSize = 11.sp)
            val rows = listOf(
                "1 km" to null, "5 km" to null, "10 km" to null,
                "100 km" to EwSelectGreen, "500 km" to EwBevelDark,
                "1000 km" to EwBevelDark, "5000 km" to EwSelectGreen,
            )
            rows.forEach { (label, swatch) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(swatch ?: EwPanelDark).motifBevel(raised = false))
                    Spacer(Modifier.width(6.dp))
                    Text(label, color = if (swatch == null) EwTextDim else EwText, fontSize = 11.sp)
                }
            }
        }
    }
}

// ---- Sectors -----------------------------------------------------------------
@Composable
private fun SectorPanel(modifier: Modifier = Modifier, onExit: () -> Unit = {}) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth().background(EwHeaderBlue).motifBevel(raised = true).padding(4.dp), contentAlignment = Alignment.Center) {
            Text("Island 1", color = EwText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column(Modifier.weight(1f).fillMaxWidth().background(Color(0xFFD7D8CE)).motifBevel(raised = false).padding(4.dp)) {
            val sectors = listOf("USA", "Island 1", "Island 2", "Island 3", "Island 4", "Island 5", "San Francisco")
            sectors.forEach { s ->
                val sel = s == "Island 1"
                Box(Modifier.fillMaxWidth().background(if (sel) EwSelectGreen else Color.Transparent).padding(vertical = 1.dp)) {
                    Text(s, color = EwText, fontSize = 12.sp)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MotifButton("Open", Modifier.weight(1f))
            MotifButton("EXIT", Modifier.weight(1f), textColor = EwArrowRed, onClick = onExit)
        }
    }
}

// ---- Data parameters ---------------------------------------------------------
@Composable
private fun DataParameters(modifier: Modifier = Modifier, viewModel: EarthWatchViewModel) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                DataToggle("Base Map", on = true, enabled = true)
                DataToggle("Data Line", on = false, enabled = false)
                DataToggle("Journal", on = false, enabled = false)
                DataToggle("Mult-Connect", on = false, enabled = false)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                DataToggle("Sky Configuration", on = true, enabled = true)
                DataToggle("Input Mode", on = false, enabled = false)
                DataToggle(
                    "Interactive",
                    on = viewModel.interactive,
                    enabled = true,
                    onClick = { viewModel.toggleInteractive() },
                )
                DataToggle("Display Graph", on = false, enabled = false)
            }
        }
        Spacer(Modifier.height(2.dp))
        val lines = listOf(
            "Satellite: OFF  Time: 04:03, 06:05",
            "Radar: OFF, Time 04:00, 05:05",
            "No weather map available",
            "No bands available",
            "No graphics available",
            "Flag: OFF",
        )
        lines.forEach { Text(it, color = EwTextDim, fontStyle = FontStyle.Italic, fontSize = 11.sp) }
    }
}

@Composable
private fun DataToggle(label: String, on: Boolean, enabled: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth()
            .background(EwPanel)
            .motifBevel(raised = true)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).background(if (on) EwSelectGreen else EwPanelDark).motifBevel(raised = false))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (enabled) EwText else EwTextDim,
            fontWeight = if (enabled) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
        )
    }
}

// ---- Side view (terrain profile) --------------------------------------------
@Composable
private fun SideViewProfile(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(EwSideSky).motifBevel(raised = false)) {
        Canvas(Modifier.fillMaxSize()) {
            // Terrain silhouette.
            val terrain = Path().apply {
                moveTo(0f, size.height)
                lineTo(0f, size.height * 0.82f)
                cubicTo(size.width * 0.2f, size.height * 0.6f, size.width * 0.3f, size.height * 0.95f, size.width * 0.45f, size.height * 0.8f)
                cubicTo(size.width * 0.62f, size.height * 0.55f, size.width * 0.78f, size.height * 0.5f, size.width, size.height * 0.72f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(terrain, EwLand)
            // Altitude ticks on the left.
            for (i in 1..4) {
                val y = size.height * (i / 5f)
                drawLine(Color.White, Offset(0f, y), Offset(6f, y), strokeWidth = 1f)
            }
            // Pink marker arrow.
            val mx = size.width * 0.55f
            val my = size.height * 0.4f
            drawLine(EwArrowPink, Offset(mx - 8, my - 8), Offset(mx + 8, my + 8), strokeWidth = 2f)
            drawLine(EwArrowPink, Offset(mx + 8, my - 8), Offset(mx - 8, my + 8), strokeWidth = 2f)
        }
        Column(Modifier.padding(3.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("-80 km", "-60 km", "-40 km", "-20 km").forEach {
                Text(it, color = Color.White, fontSize = 9.sp)
            }
        }
    }
}

// ---- Clock -------------------------------------------------------------------
@Composable
private fun ClockPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Tue  CH: H  14:48 . 309  SEC", color = EwText, fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(7) {
                Box(Modifier.size(20.dp, 22.dp).background(Color(0xFFDDDDD4)).motifBevel(raised = false), contentAlignment = Alignment.Center) {
                    Text(".", color = EwText, fontSize = 11.sp)
                }
            }
        }
        MotifButton("Reset")
    }
}
