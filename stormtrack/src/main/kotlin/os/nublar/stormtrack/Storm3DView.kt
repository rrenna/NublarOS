package os.nublar.stormtrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarType

private const val RENDER_WIDTH = 640
private const val RENDER_HEIGHT = 480

/**
 * Displays the software-rendered 3D storm scene. Frames are rasterized on a
 * background thread and handed to Compose as [ImageBitmap]s; the low render
 * resolution is upscaled unfiltered for the retro workstation look.
 */
@Composable
fun Storm3DView(modifier: Modifier = Modifier) {
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }
    var modelSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            val renderer = SoftwareRenderer3D(RENDER_WIDTH, RENDER_HEIGHT)
            val startNanos = System.nanoTime()
            while (isActive) {
                val timeSeconds = (System.nanoTime() - startNanos) / 1_000_000_000f
                val pixels = renderer.render(timeSeconds)
                val bitmap = bgraToImageBitmap(RENDER_WIDTH, RENDER_HEIGHT, pixels)
                // Compose snapshot state is thread-safe to write from any thread;
                // it schedules recomposition on the UI. No Main-dispatcher hop is
                // needed (Compose Desktop provides no kotlinx Main dispatcher).
                frame = bitmap
                modelSeconds = timeSeconds.toLong()
                delay(33L) // ~30 fps — plenty for a control-room display
            }
        }
    }

    Box(modifier = modifier.background(NublarColors.ScreenBlack)) {
        frame?.let { bitmap ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = bitmap,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                    filterQuality = FilterQuality.None,
                )
            }
        }

        Text(
            text = "STORM TRACKING — CYCLONE MODEL",
            color = NublarColors.LabelCream,
            style = NublarType.Header,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        )
        Text(
            text = if (frame == null) "MODEL TIME —: INITIALIZING…" else "MODEL TIME T+%03ds".format(modelSeconds),
            color = NublarColors.StatusGreen,
            fontSize = 12.sp,
            style = NublarType.SystemText,
            modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
        )
    }
}

/** Converts BGRA rows (top-left origin) into a Compose [ImageBitmap]. */
private fun bgraToImageBitmap(width: Int, height: Int, pixels: ByteArray): ImageBitmap {
    val image = Image.makeRaster(
        ImageInfo(width, height, ColorType.BGRA_8888, ColorAlphaType.OPAQUE),
        pixels,
        width * 4,
    )
    return image.toComposeImageBitmap()
}
