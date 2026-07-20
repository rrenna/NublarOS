package os.nublar.dashboard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.designsystem.NublarColors
import os.nublar.designsystem.NublarFonts
import os.nublar.designsystem.ScreenShader

/**
 * Settings screen. First control: a dropdown selecting the full-screen
 * post-process shader (see [ScreenShader]).
 */
@Composable
fun SettingsView(
    selectedShader: ScreenShader,
    onSelectShader: (ScreenShader) -> Unit,
    fullscreen: Boolean,
    onSetFullscreen: (Boolean) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NublarColors.MonitorGray)
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("SETTINGS", color = NublarColors.LabelCream, fontFamily = NublarFonts.Display, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NublarColors.DarkFrame)
                    .bevelBorder(raised = false, width = 2.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SettingRow(label = "Screen Shader") {
                    ShaderDropdown(selected = selectedShader, onSelect = onSelectShader)
                }
                SettingRow(label = "Fullscreen (no title bar)") {
                    ChunkyButton(
                        if (fullscreen) "ON" else "OFF",
                        modifier = Modifier.width(80.dp),
                        highlight = fullscreen,
                        onClick = { onSetFullscreen(!fullscreen) },
                    )
                }
            }

            Spacer(Modifier.width(4.dp))
            ChunkyButton("DONE", modifier = Modifier.width(120.dp), onClick = onClose)
        }
    }
}

@Composable
private fun SettingRow(label: String, control: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(label, color = NublarColors.LabelCream, modifier = Modifier.width(160.dp), fontSize = 14.sp)
        control()
    }
}

@Composable
private fun ShaderDropdown(selected: ScreenShader, onSelect: (ScreenShader) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .width(240.dp)
                .background(NublarColors.MonitorGray)
                .bevelBorder(raised = true)
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected.label, color = NublarColors.DarkFrame, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.width(8.dp))
            Text("▼", color = NublarColors.DarkFrame, fontSize = 11.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ScreenShader.entries.forEach { shader ->
                DropdownMenuItem(onClick = {
                    onSelect(shader)
                    expanded = false
                }) {
                    Text(
                        shader.label,
                        color = if (shader == selected) NublarColors.MapBlue else Color.Black,
                        fontWeight = if (shader == selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
