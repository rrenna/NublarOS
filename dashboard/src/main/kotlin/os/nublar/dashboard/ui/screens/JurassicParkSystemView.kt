package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.designsystem.NublarColors

/**
 * Third Nedryland Monitor screen: the "Jurassic Park System" desktop view. A
 * close-in-spirit recreation of the film's Macintosh workstation screen — the
 * left pane is an ORIGINAL abstract striped "desktop" (not the copyrighted
 * pin-up from the reference; see README "Legal and Asset Guidelines"), and the
 * right pane reuses the shared Nedryland chrome (VEHICLE tabs, transport keys,
 * GLITCHES) ending in the big EXECUTE prompt. Shown as "Jurassic Park System"
 * in the SCREEN bar.
 */
@Composable
fun JurassicParkSystemView(
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
                left = { m -> DesktopPanel(modifier = m) },
                right = { m -> SystemRightColumn(modifier = m, onClose = onClose) },
            )
            BottomBar(screenLabel = "Jurassic Park System", onScreenClick = onSwitchScreen)
        }
    }
}

/** Loads a bundled PNG resource into an ImageBitmap. */
private fun loadBitmap(resource: String): ImageBitmap {
    val bytes = object {}.javaClass.classLoader.getResourceAsStream(resource)!!.readBytes()
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

/** The map-view "desktop": the bundled zebra background image, filling the pane. */
@Composable
private fun DesktopPanel(modifier: Modifier = Modifier) {
    val background = remember { loadBitmap("zebra.png") }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NublarColors.DarkFrame)
            .padding(4.dp),
    ) {
        Image(
            bitmap = background,
            contentDescription = "Jurassic Park System desktop",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun SystemRightColumn(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TabRow(label = "VEHICLE", tabs = listOf("TOUR", "POWER", "TIME"))
        GamePanel(modifier = Modifier.weight(1f).fillMaxWidth())
        TransportPanel(onClose = onClose)
        TabRow(label = "GLITCHES", tabs = listOf("MAPS", "SYSTEM", "EMERG."))
        ExecutePanel(modifier = Modifier.weight(1f).fillMaxWidth())
    }
}

/** Black "paused game" panel echoing the reference's arcade window. */
@Composable
private fun GamePanel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Starfield / planet scene.
        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.Black)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // A few stars.
                val stars = listOf(
                    0.15f to 0.2f, 0.8f to 0.15f, 0.6f to 0.7f, 0.3f to 0.85f,
                    0.9f to 0.6f, 0.45f to 0.4f, 0.2f to 0.6f,
                )
                stars.forEach { (sx, sy) ->
                    drawCircle(Color.White, radius = size.minDimension * 0.006f, center = Offset(size.width * sx, size.height * sy))
                }
                // Bluish planet blob, center-ish.
                drawCircle(
                    Color(0xFF3A6EA5),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.55f, size.height * 0.5f),
                )
                drawCircle(
                    Color(0xFFB5462F),
                    radius = size.minDimension * 0.05f,
                    center = Offset(size.width * 0.64f, size.height * 0.56f),
                )
            }
        }
        // Score / status column.
        Column(
            modifier = Modifier
                .background(Color.Black)
                .border(1.dp, Color(0xFFB5462F))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("JURASSIC", color = Color(0xFFB5462F), fontWeight = FontWeight.Black, fontSize = 9.sp)
            Text("Score", color = Color.White, fontSize = 9.sp)
            Text("350", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.width(2.dp))
            Box(
                modifier = Modifier.background(Color(0xFFB5462F)).padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text("PAUSED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp)
            }
        }
    }
}

/** HOLD/QUIT/NEW + transport keys, in the shared recessed bevel panel. */
@Composable
private fun TransportPanel(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .background(NublarColors.MonitorGray)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(3.dp),
    ) {
        Row {
            ChunkyButton("HOLD", modifier = Modifier.weight(1f))
            ChunkyButton("QUIT", modifier = Modifier.weight(1f), onClick = onClose)
            ChunkyButton("NEW", modifier = Modifier.weight(1f))
        }
        Row {
            ChunkyButton("NEXT", modifier = Modifier.weight(2f))
            ChunkyButton("◄◄", modifier = Modifier.weight(1f))
            ChunkyButton("►►", modifier = Modifier.weight(1f))
            ChunkyButton("►", modifier = Modifier.weight(1f), highlight = true)
            ChunkyButton("■", modifier = Modifier.weight(1f))
        }
    }
}

/** GLITCHES panel: a couple of CLEAR rows above the big EXECUTE prompt button. */
@Composable
private fun ExecutePanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
            .bevelBorder(raised = false, width = 2.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ClearRow("Communications Follow up")
        ClearRow("Debug Island Overview")
        Spacer(Modifier.width(2.dp))
        // The big EXECUTE ? button — a fixed band (~1/3 of its former
        // fill-the-panel height) rather than stretching to fill.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .background(Color(0xFFDD6B54))
                .bevelBorder(raised = true, width = 3.dp)
                .clickable { /* inert prompt, like the other screens' controls */ }
                .pointerHoverIcon(PointerIcon.Hand),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "EXECUTE ?",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                fontSize = 40.sp,
            )
        }
    }
}

@Composable
private fun ClearRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .background(NublarColors.StatusGreen)
                .padding(horizontal = 6.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "CLEAR",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "- $text",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            fontStyle = FontStyle.Italic,
        )
    }
}
