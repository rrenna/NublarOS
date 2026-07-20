package os.nublar.designsystem

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Three typographic roles, per design/typography.md:
 * headers (Oswald), system text (JetBrains Mono), display (Archivo Black).
 * All three are OFL-licensed and bundled under
 * design-system/src/main/resources/fonts — see [NublarFonts].
 */
object NublarType {
    val Header = TextStyle(
        fontFamily = NublarFonts.Ui,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
    )

    val SystemText = TextStyle(
        fontFamily = NublarFonts.Mono,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    )

    val Display = TextStyle(
        fontFamily = NublarFonts.Display,
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
