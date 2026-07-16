package os.nublar.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font

/**
 * Bundled font families loaded from design-system/src/main/resources/fonts.
 *
 * [JurassicTitle] is the movie-style title face (JurassicPark-BL48.ttf),
 * intended as a hero/display font. NOTE: it is licensed Freeware,
 * **Non-Commercial** — see assets/fonts/ATTRIBUTION.md. It is NOT yet wired
 * into [NublarType]; use it explicitly where a hero title is wanted, e.g.
 * `NublarType.Display.copy(fontFamily = NublarFonts.JurassicTitle)`.
 */
object NublarFonts {
    val JurassicTitle: FontFamily by lazy {
        FontFamily(Font(resource = "fonts/JurassicPark-BL48.ttf"))
    }
}
