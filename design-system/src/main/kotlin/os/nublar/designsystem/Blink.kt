package os.nublar.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Shared flashing state for urgent status tags ("flashing = disconnected/
 * urgent", see design/component-reference.md): toggles true/false at half the
 * given cycle period, starting ON so a flashing element is visible immediately.
 *
 * Apply as an alpha/visibility switch on FAILED tags, REC dots, and offline
 * statuses — the film's control screens flash these rather than holding them
 * steadily lit.
 */
@Composable
fun rememberBlinkOn(cycleMs: Long = 900L): Boolean {
    var on by remember { mutableStateOf(true) }
    LaunchedEffect(cycleMs) {
        while (true) {
            delay(cycleMs / 2)
            on = !on
        }
    }
    return on
}
