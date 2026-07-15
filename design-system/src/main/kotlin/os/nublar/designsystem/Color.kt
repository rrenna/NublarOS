package os.nublar.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Core NublarOS palette. Mirrors design/palette.md — keep both in sync.
 */
object NublarColors {
    val MonitorGray = Color(0xFF737A83)
    val DarkFrame = Color(0xFF303942)
    val InsetPanel = Color(0xFF515C68)
    val ScreenBlack = Color(0xFF07100D)
    val StatusGreen = Color(0xFF54D875)
    val WarningRed = Color(0xFFE55454)
    val MapBlue = Color(0xFF397FA4)
    val PaddockGreen = Color(0xFF3F8F58)
    val LabelCream = Color(0xFFE2E0BF)
    val HighlightYellow = Color(0xFFD5CD58)
}

/**
 * Sector/status state colors, per README "Status Mapping":
 * green = normal, yellow = moderate, orange = degraded, red = failed,
 * flashing = disconnected/urgent, gray = unavailable.
 */
enum class SectorStatus(val color: Color) {
    Normal(NublarColors.StatusGreen),
    Moderate(NublarColors.HighlightYellow),
    Degraded(Color(0xFFD98A3D)),
    Failed(NublarColors.WarningRed),
    Unavailable(NublarColors.MonitorGray),
}
