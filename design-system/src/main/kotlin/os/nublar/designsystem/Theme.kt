package os.nublar.designsystem

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Minimal rounded corners, per README Window Styling — Shapes() defaults to
// square (zero-radius RoundedCornerShape) corners already.
private val NublarShapes = Shapes()

private val NublarDarkColors = darkColors(
    primary = NublarColors.MapBlue,
    primaryVariant = NublarColors.DarkFrame,
    secondary = NublarColors.StatusGreen,
    background = NublarColors.ScreenBlack,
    surface = NublarColors.InsetPanel,
    error = NublarColors.WarningRed,
    onPrimary = NublarColors.LabelCream,
    onSecondary = NublarColors.ScreenBlack,
    onBackground = NublarColors.LabelCream,
    onSurface = NublarColors.LabelCream,
    onError = NublarColors.LabelCream,
)

/** Beveled panel border width, per README "Chunky beveled panels". */
val NublarBevelWidth = 2.dp

@Composable
fun NublarTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = NublarDarkColors,
        typography = NublarTypography,
        shapes = NublarShapes,
        content = content,
    )
}
