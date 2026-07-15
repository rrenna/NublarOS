package os.nublar.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import os.nublar.designsystem.NublarBevelWidth
import os.nublar.designsystem.NublarColors

/**
 * Recessed panel with a thick dark outer border and a thin light inner
 * highlight — the base building block for windows, tiles, and the
 * dashboard's status panels. See README "Window Styling".
 */
@Composable
fun BeveledPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(NublarColors.DarkFrame)
            .border(NublarBevelWidth, NublarColors.InsetPanel)
            .padding(NublarBevelWidth)
            .background(NublarColors.ScreenBlack)
            .padding(12.dp),
    ) {
        content()
    }
}
