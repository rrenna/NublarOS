package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarTheme

/**
 * Compose @Previews for the ChunkyButton control (HOLD / QUIT / NEW / NEXT and
 * the transport row). Purely a design-iteration harness — these render in the
 * Android Studio / IntelliJ "Split"/"Design" preview pane (Compose Desktop
 * previews) so button styling can be tweaked without launching the whole app.
 *
 * Nothing here ships in the running app; @Preview functions are only invoked
 * by the tooling. Each is wrapped in [NublarTheme] over the MonitorGray panel
 * background so the bevel/contrast matches the real Control Room chrome.
 *
 * If the preview pane is blank, build the module once (the Compose compiler
 * has to process this file) and hit the refresh icon in the preview toolbar.
 */

/** Shared backdrop so a preview reads against the real panel color, not white. */
@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    NublarTheme {
        Column(
            modifier = Modifier
                .background(NublarColors.MonitorGray)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun ChunkyButtonDefaultPreview() {
    PreviewSurface {
        ChunkyButton("HOLD", modifier = Modifier.width(120.dp))
    }
}

@Preview
@Composable
private fun ChunkyButtonHighlightPreview() {
    PreviewSurface {
        ChunkyButton("►", modifier = Modifier.width(120.dp), highlight = true)
    }
}

/**
 * Default vs. highlighted side by side — the fastest way to eyeball both
 * states while tweaking [ChunkyButton]'s colors/bevel.
 */
@Preview
@Composable
private fun ChunkyButtonStatesPreview() {
    PreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChunkyButton("NORMAL", modifier = Modifier.width(110.dp))
            ChunkyButton("ACTIVE", modifier = Modifier.width(110.dp), highlight = true)
        }
    }
}

/**
 * The full transport-control cluster (HOLD/QUIT/NEW on top, the playback row
 * below) exactly as [TransportControls] lays it out, so spacing and grouping
 * can be judged as a unit.
 */
@Preview
@Composable
private fun TransportControlsPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.width(360.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChunkyButton("HOLD", modifier = Modifier.weight(1f))
                ChunkyButton("QUIT", modifier = Modifier.weight(1f))
                ChunkyButton("NEW", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ChunkyButton("NEXT", modifier = Modifier.weight(1f))
                ChunkyButton("◄◄", modifier = Modifier.weight(1f))
                ChunkyButton("►►", modifier = Modifier.weight(1f))
                ChunkyButton("►", modifier = Modifier.weight(1f), highlight = true)
                ChunkyButton("■", modifier = Modifier.weight(1f))
            }
        }
    }
}

/** A gallery of the individual labels used across the Control Room screens. */
@Preview
@Composable
private fun ChunkyButtonGalleryPreview() {
    PreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ChunkyButton labels", color = NublarColors.LabelCream)
            Spacer(Modifier.width(4.dp))
            listOf("HOLD", "QUIT", "NEW", "NEXT", "◄◄", "►►", "►", "■").chunked(4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { label ->
                        ChunkyButton(label, modifier = Modifier.width(72.dp), highlight = label == "►")
                    }
                }
            }
        }
    }
}
