package os.nublar.dashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/** Loads a bundled PNG resource into an ImageBitmap (transparency preserved). */
private fun loadImageBitmap(resource: String): ImageBitmap {
    val bytes = object {}.javaClass.classLoader.getResourceAsStream(resource)!!.readBytes()
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

/**
 * Reusable top-down vehicle image.
 *
 * Renders a transparent PNG [assetName] aspect-fit (never cropped, stretched,
 * recolored, tinted, masked, or given a background) and horizontally centered
 * within its container. It's capped to [maxWidthRatio] of the available width
 * and to the container height, so it stays inside the area and scales
 * responsively as the container resizes. With [showShadow] on, a very subtle
 * dark drop shadow is drawn beneath it (a low-alpha, slightly offset silhouette
 * of the same image — the vehicle itself is never tinted).
 *
 * @param assetName bundled image resource name (classpath).
 * @param contentDescription accessibility label.
 * @param maxWidthRatio fraction of the container width the vehicle may occupy.
 * @param showShadow whether to draw the subtle shadow beneath the vehicle.
 */
@Composable
fun VehicleImage(
    assetName: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    maxWidthRatio: Float = 0.61f,
    showShadow: Boolean = false,
) {
    val bitmap = remember(assetName) { loadImageBitmap(assetName) }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Constrain to maxWidthRatio of the width and full height; ContentScale.Fit
        // then aspect-fits within that box (width- or height-limited, whichever
        // binds first), keeping the vehicle centered and never overlapping.
        Box(
            modifier = Modifier.fillMaxWidth(maxWidthRatio).fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            if (showShadow) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().offset(y = 3.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.20f,
                    colorFilter = ColorFilter.tint(Color.Black),
                )
            }
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

/** Panel colors: near-black (sampled from the combined asset) + thin blue-grey border. */
private val VehiclePanelBlack = Color(0xFF000000)
private val VehiclePanelBorder = Color(0xFF33445C)

/**
 * Lower vehicle-information panel: a single combined graphic (front + side
 * Explorer line art on its own black background) rendered aspect-fit to fill the
 * panel height, with the "Vehicle type: / Ford explorer" description overlaid in
 * the gap between the two illustrations.
 *
 * The image's built-in black matches the panel background, so it blends with no
 * seam; the text is positioned as a fraction of the FITTED image so it tracks
 * the vehicles at any size (and shrinks its font first on narrow layouts).
 *
 * [overlay] renders on top of the image (its receiver is the panel's [BoxScope],
 * so callers can align content, e.g. a left-aligned readout, over the graphic).
 */
@Composable
fun VehicleInfoPanel(
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val bitmap = remember { loadImageBitmap("vehicle_explorer_views.png") }
    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()
    Box(
        modifier = modifier
            .background(VehiclePanelBlack)
            .border(1.dp, VehiclePanelBorder)
            .clipToBounds(),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val cwPx = with(density) { maxWidth.toPx() }
            val chPx = with(density) { maxHeight.toPx() }
            // Fitted (contain) image rect, so the whole graphic stays visible.
            val scale = min(cwPx / imgW, chPx / imgH)
            val fittedW = imgW * scale
            val fittedH = imgH * scale
            val leftPx = (cwPx - fittedW) / 2f
            val topPx = (chPx - fittedH) / 2f

            Image(
                bitmap = bitmap,
                contentDescription = "Front and side views of a Ford Explorer",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )

            // Text overlaid in the empty gap between the front and side views
            // (~37% across, mid-height), relative to the fitted image.
            val textX = with(density) { (leftPx + fittedW * 0.365f).toDp() }
            val textY = with(density) { (topPx + fittedH * 0.44f).toDp() }
            val fontSize = (maxWidth.value * 0.03f).coerceIn(8f, 11f).sp
            Column(modifier = Modifier.offset(x = textX, y = textY)) {
                Text(
                    "Vehicle type:",
                    color = Color(0xFFBFC6CE),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.05f,
                )
                Text(
                    "Ford explorer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.05f,
                )
            }
        }
        // Caller-supplied content rendered on top of the image.
        overlay()
    }
}
