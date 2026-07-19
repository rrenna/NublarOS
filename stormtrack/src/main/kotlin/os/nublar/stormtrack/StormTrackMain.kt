package os.nublar.stormtrack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import os.nublar.designsystem.NublarTheme

/**
 * StormTrack / EarthWatch entry point: the weather-computer workstation UI (two
 * live viewports over the control deck). The 3D storm animation still drives the
 * "Animation" window; see [Storm3DView].
 */
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "EARTHWATCH",
            state = rememberWindowState(width = 1440.dp, height = 1024.dp),
        ) {
            NublarTheme {
                EarthWatchView(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
