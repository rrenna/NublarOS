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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarType
import java.util.concurrent.Executors

/** Dedicated GL thread: all OpenGL calls for the offscreen renderer happen here. */
private val glDispatcher: CoroutineDispatcher =
    Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "stormtrack-gl").apply { isDaemon = true }
    }.asCoroutineDispatcher()

private const val RENDER_WIDTH = 1024
private const val RENDER_HEIGHT = 768

/**
 * Embeds the offscreen-rendered 3D storm scene in Compose. The renderer runs
 * on [glDispatcher]; each finished frame is converted to an [ImageBitmap] and
 * handed to Compose for display.
 */
@Composable
fun Storm3DView(modifier: Modifier = Modifier) {
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        withContext(glDispatcher) {
            GlContext.ensureInitialized()
            GlContext.makeCurrent(RENDER_WIDTH, RENDER_HEIGHT)
            val renderer = StormRenderer(RENDER_WIDTH, RENDER_HEIGHT)
            val startNanos = System.nanoTime()
            try {
                while (true) {
                    val timeSeconds = (System.nanoTime() - startNanos) / 1_000_000_000f
                    val pixels = renderer.renderFrame(timeSeconds)
                    val bitmap = bgraToImageBitmap(RENDER_WIDTH, RENDER_HEIGHT, pixels)
                    withContext(Dispatchers.Main) { frame = bitmap }
                    delay(16L) // ~60 fps target
                }
            } finally {
                renderer.destroy()
            }
        }
    }

    Box(modifier = modifier.background(NublarColors.ScreenBlack)) {
        frame?.let { bitmap ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawImage(
                    image = bitmap,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt()),
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
            text = if (frame == null) "MODEL TIME —: INITIALIZING…" else "MODEL TIME T+${(System.nanoTime() / 1_000_000_000L) % 1000}s",
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
