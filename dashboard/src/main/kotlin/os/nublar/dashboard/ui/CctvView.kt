package os.nublar.dashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import os.nublar.dashboard.ui.screens.bevelBorder
import os.nublar.designsystem.NublarColors
import java.io.File

/**
 * Local CCTV footage lives OUTSIDE the repo (never committed): the app plays a
 * looping frame sequence from this folder on the user's machine. The clip is
 * mostly static camera views, so a low frame rate is plenty. Populate it from a
 * downloaded clip with, e.g.:
 *
 *   mkdir -p ~/.nublaros/cctv
 *   ffmpeg -i <your-video> -vf "fps=2,scale=640:-2" -q:v 5 ~/.nublaros/cctv/frame_%04d.jpg
 *
 * Footage source (see docs/inspirations.md): "Jurassic Park Systems CCTV" —
 * https://www.youtube.com/watch?v=Xl4bDROhwd0 . Credit its creator.
 */
private val CCTV_DIR = File(System.getProperty("user.home"), ".nublaros/cctv")
private const val CCTV_FPS = 2

/** Short on-screen credit for the CCTV footage. */
private const val CCTV_CREDIT = "Footage: “Jurassic Park Systems CCTV” (YouTube)"

/** Sorted list of frame files in [CCTV_DIR] (empty if the folder is absent). */
private fun cctvFrameFiles(): List<File> =
    CCTV_DIR.listFiles { f ->
        f.isFile && f.name.substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png")
    }?.sortedBy { it.name }.orEmpty()

/**
 * Plays the local CCTV frame sequence on a loop. Decodes one frame at a time on
 * a background thread (low memory); shows a "NO SIGNAL" placeholder with setup
 * hints when no frames are present.
 */
@Composable
fun CctvView(modifier: Modifier = Modifier) {
    val frames = remember { cctvFrameFiles() }
    var frame by remember { mutableStateOf<ImageBitmap?>(null) }

    if (frames.isNotEmpty()) {
        LaunchedEffect(frames) {
            val frameMs = 1000L / CCTV_FPS
            var i = 0
            while (isActive) {
                frame = withContext(Dispatchers.Default) {
                    org.jetbrains.skia.Image.makeFromEncoded(frames[i].readBytes()).toComposeImageBitmap()
                }
                i = (i + 1) % frames.size
                delay(frameMs)
            }
        }
    }

    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        val current = frame
        if (current != null) {
            Image(
                bitmap = current,
                contentDescription = "Security camera feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("NO SIGNAL", color = NublarColors.StatusGreen, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("place frames in ~/.nublaros/cctv", color = NublarColors.LabelCream.copy(alpha = 0.7f), fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            }
        }
    }
}

/** A titled CCTV subwindow (bevel frame + header) wrapping the [CctvView] feed. */
@Composable
fun SecurityCamPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(NublarColors.DarkFrame)
            .bevelBorder(raised = true, width = 2.dp)
            .padding(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(7.dp).background(NublarColors.WarningRed))
            Spacer(Modifier.width(6.dp))
            Text("SECURITY DASH 01", color = NublarColors.LabelCream, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Spacer(Modifier.width(6.dp))
            Text("• REC", color = NublarColors.WarningRed, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }
        CctvView(modifier = Modifier.fillMaxWidth().weight(1f))
        // Attribution for the CCTV footage.
        Box(
            modifier = Modifier.fillMaxWidth().background(Color.Black).padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Text(
                CCTV_CREDIT,
                color = NublarColors.LabelCream.copy(alpha = 0.6f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
            )
        }
    }
}
