package os.nublar.stormtrack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import os.nublar.designsystem.NublarTheme

/**
 * StormTrack proof-of-concept entry point: a real OpenGL 3D storm rendered
 * offscreen via LWJGL + OSMesa and displayed inside a Compose window.
 */
fun main() {
    application {
        Window(
            onCloseRequest = {
                GlContext.destroy()
                exitApplication()
            },
            title = "STORMTRACK",
            state = rememberWindowState(width = 1280.dp, height = 960.dp),
        ) {
            NublarTheme {
                Storm3DView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
