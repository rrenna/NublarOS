package os.nublar.stormtrack

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/** Frames-per-second the storm clip was extracted at (see the ffmpeg fps= filter). */
private const val STORM_FPS = 15
private const val STORM_FRAME_DIR = "storm-frames"

/**
 * Loads the encoded (JPEG) bytes of every extracted storm frame from resources.
 * Frames are named frame_001.jpg, frame_002.jpg, …; loading stops at the first
 * gap. Only the compact encoded bytes are held (a few MB total) — each frame is
 * decoded on demand during playback, so memory stays low.
 */
private fun loadStormFrameBytes(): List<ByteArray> {
    val classLoader = object {}.javaClass.classLoader
    val frames = mutableListOf<ByteArray>()
    var i = 1
    while (true) {
        val name = "%s/frame_%03d.jpg".format(STORM_FRAME_DIR, i)
        val stream = classLoader.getResourceAsStream(name) ?: break
        frames.add(stream.use { it.readBytes() })
        i++
    }
    return frames
}

/**
 * Plays the bundled storm animation (pre-extracted video frames) on a loop.
 * Decoding one frame at a time on a background thread avoids holding every
 * frame's bitmap in memory at once; Compose snapshot state is thread-safe to
 * write, so the decoded frame is published straight to state.
 */
@Composable
fun StormVideoView(modifier: Modifier = Modifier) {
    val frames = remember { loadStormFrameBytes() }
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(frames) {
        if (frames.isEmpty()) return@LaunchedEffect
        val frameMs = 1000L / STORM_FPS
        var index = 0
        while (isActive) {
            frame = withContext(Dispatchers.Default) {
                org.jetbrains.skia.Image.makeFromEncoded(frames[index]).toComposeImageBitmap()
            }
            index = (index + 1) % frames.size
            delay(frameMs)
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        frame?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = "Storm animation",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
