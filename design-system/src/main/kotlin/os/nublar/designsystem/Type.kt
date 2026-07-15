package os.nublar.designsystem

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Three typographic roles, per design/typography.md:
 * headers (Oswald), system text (JetBrains Mono), display (Archivo Black).
 *
 * Font families are loaded at runtime from bundled OFL-licensed assets
 * (see design-system/src/main/resources/fonts) rather than hardcoded here;
 * FontFamily.Default is a placeholder until those are wired up.
 */
object NublarType {
    val Header = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
    )

    val SystemText = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    )

    val Display = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = 1.sp,
    )
}

val NublarTypography = Typography(
    h1 = NublarType.Display,
    body1 = NublarType.SystemText,
    subtitle1 = NublarType.Header,
)
