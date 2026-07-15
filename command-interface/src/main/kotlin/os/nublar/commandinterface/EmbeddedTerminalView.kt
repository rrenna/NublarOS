package os.nublar.commandinterface

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel

/**
 * Embeds a real terminal (see [createEmbeddedTerminal]) inside a Compose
 * layout via SwingPanel — the standard interop path for AWT/Swing-backed
 * libraries in Compose Desktop.
 */
@Composable
fun EmbeddedTerminalView(
    modifier: Modifier = Modifier.fillMaxSize(),
    workingDirectory: String = System.getProperty("user.home"),
) {
    val widget = remember { createEmbeddedTerminal(workingDirectory = workingDirectory) }

    DisposableEffect(widget) {
        onDispose { widget.close() }
    }

    SwingPanel(
        modifier = modifier,
        factory = { widget },
    )
}
