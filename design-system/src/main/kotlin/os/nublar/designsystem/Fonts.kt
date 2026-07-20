package os.nublar.designsystem

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

/**
 * Bundled font families loaded from design-system/src/main/resources/fonts.
 *
 * The three UI roles (see design/typography.md) are all OFL-licensed
 * (licence text: fonts/OFL.txt):
 * - [Ui] — Oswald, condensed industrial sans for headers, buttons, labels.
 * - [Mono] — JetBrains Mono, for terminals, readouts, logs, metrics.
 * - [Display] — Archivo Black, single-weight hero face for big banners.
 *   Requested italic styles on these families are synthesized (none of them
 *   ship italic cuts), which suits the film's bold-oblique banner text.
 *
 * [JurassicTitle] is the movie-style title face (JurassicPark-BL48.ttf),
 * intended as a hero/display font. NOTE: it is licensed Freeware,
 * **Non-Commercial** — see assets/fonts/ATTRIBUTION.md. It is NOT wired
 * into [NublarType]; use it explicitly where a hero title is wanted, e.g.
 * `NublarType.Display.copy(fontFamily = NublarFonts.JurassicTitle)`.
 */
object NublarFonts {
    val JurassicTitle: FontFamily by lazy {
        FontFamily(Font(resource = "fonts/JurassicPark-BL48.ttf"))
    }

    val Ui: FontFamily by lazy {
        FontFamily(
            Font(resource = "fonts/Oswald-Medium.ttf", weight = FontWeight.Medium),
            Font(resource = "fonts/Oswald-Bold.ttf", weight = FontWeight.Bold),
        )
    }

    val Mono: FontFamily by lazy {
        FontFamily(
            Font(resource = "fonts/JetBrainsMono-Regular.ttf", weight = FontWeight.Normal),
            Font(resource = "fonts/JetBrainsMono-Bold.ttf", weight = FontWeight.Bold),
        )
    }

    val Display: FontFamily by lazy {
        FontFamily(Font(resource = "fonts/ArchivoBlack-Regular.ttf", weight = FontWeight.Black))
    }
}
