package os.nublar.dashboard

import java.util.prefs.Preferences

/**
 * Persistent app preferences via [java.util.prefs.Preferences] — the JVM's
 * cross-platform equivalent of NSUserDefaults (macOS plist, Windows registry,
 * Linux dotfile). Used to remember lightweight UI state across launches.
 */
object AppPreferences {
    private val prefs = Preferences.userRoot().node("os/nublar/dashboard")

    private const val KEY_LAST_SCREEN = "last_screen"
    private const val KEY_MAP_PREVIEW_LAYERS = "map_preview_layers"
    private const val KEY_SHADER = "screen_shader"
    private const val KEY_FULLSCREEN = "fullscreen"

    /** Name of the last-active screen, or null if never set / not persisted. */
    var lastScreen: String?
        get() = prefs.get(KEY_LAST_SCREEN, null)
        set(value) {
            if (value == null) prefs.remove(KEY_LAST_SCREEN) else prefs.put(KEY_LAST_SCREEN, value)
            // Flush immediately so the value survives even a non-graceful exit;
            // the default background sync would otherwise delay the write.
            runCatching { prefs.flush() }
        }

    /**
     * The set of enabled map-layer names in the Island Map Preview, stored as a
     * comma-separated list. `null` means never persisted (use defaults); an empty
     * set is persisted as the empty string so "all off" is preserved distinctly.
     */
    var mapPreviewLayers: Set<String>?
        get() = prefs.get(KEY_MAP_PREVIEW_LAYERS, null)
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toSet()
        set(value) {
            if (value == null) prefs.remove(KEY_MAP_PREVIEW_LAYERS)
            else prefs.put(KEY_MAP_PREVIEW_LAYERS, value.joinToString(","))
            runCatching { prefs.flush() }
        }

    /** Name of the selected screen shader, or null if never set. */
    var shader: String?
        get() = prefs.get(KEY_SHADER, null)
        set(value) {
            if (value == null) prefs.remove(KEY_SHADER) else prefs.put(KEY_SHADER, value)
            runCatching { prefs.flush() }
        }

    /** Whether kiosk fullscreen is enabled. */
    var fullscreen: Boolean
        get() = prefs.getBoolean(KEY_FULLSCREEN, false)
        set(value) {
            prefs.putBoolean(KEY_FULLSCREEN, value)
            runCatching { prefs.flush() }
        }
}
