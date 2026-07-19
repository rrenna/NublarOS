package os.nublar.stormtrack

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ViewModel for the EarthWatch weather computer ([EarthWatchView]). Holds the
 * screen's interactive state as Compose snapshot state — plain class, no
 * AndroidX lifecycle, matching the rest of NublarOS.
 */
class EarthWatchViewModel {
    /** Whether the workstation is in interactive mode. Off by default. */
    var interactive: Boolean by mutableStateOf(false)
        private set

    fun toggleInteractive() {
        interactive = !interactive
    }
}
