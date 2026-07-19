package os.nublar.dashboard.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import os.nublar.dashboard.viewmodel.Screen
import os.nublar.designsystem.NublarType
import os.nublar.designsystem.SectorStatus
import os.nublar.designsystem.components.BeveledPanel

/** A machine on the park network: its display name, link status, and — if it has
 *  a screen — the [Screen] opened by clicking its tile. */
private data class NetworkMachine(
    val name: String,
    val status: SectorStatus,
    val target: Screen? = null,
)

/** Short link-state label derived from a machine's [SectorStatus]. */
private fun SectorStatus.linkLabel(): String = when (this) {
    SectorStatus.Normal -> "ONLINE"
    SectorStatus.Moderate -> "BUSY"
    SectorStatus.Degraded -> "DEGRADED"
    SectorStatus.Failed -> "OFFLINE"
    SectorStatus.Unavailable -> "NO LINK"
}

// The eight networked park machines. The three with a [target] open their
// respective screens; the rest are status-only nodes.
private val PARK_MACHINES = listOf(
    NetworkMachine("Dennis Nedry's Machine", SectorStatus.Normal, Screen.ControlRoomPlanView),
    NetworkMachine("Animal Paddocks", SectorStatus.Normal, Screen.IslandMap),
    NetworkMachine("Jurassic Park System", SectorStatus.Normal, Screen.JurassicParkSystem),
    NetworkMachine("EarthWatch Weather", SectorStatus.Normal, Screen.WeatherComputer),
    NetworkMachine("Main Gate Security", SectorStatus.Normal),
    NetworkMachine("Visitor Center Kiosk", SectorStatus.Moderate),
    NetworkMachine("Aviary Control", SectorStatus.Unavailable),
    NetworkMachine("Power Grid Node", SectorStatus.Degraded),
    NetworkMachine("Ford Explorer Uplink", SectorStatus.Failed),
)

/**
 * The park network overview: a grid of the eight networked machines. Machines
 * with an associated screen are clickable and navigate via [onOpen]; the rest
 * are status-only. Laid out four-across in two rows.
 */
@Composable
fun NetworkMachinesGrid(onOpen: (Screen) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PARK NETWORK", style = NublarType.Header)
        PARK_MACHINES.chunked(4).forEach { rowMachines ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowMachines.forEach { machine ->
                    MachineTile(machine, onOpen, modifier = Modifier.weight(1f))
                }
                // Pad a short final row so tiles keep a consistent width.
                repeat(4 - rowMachines.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MachineTile(machine: NetworkMachine, onOpen: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val clickable = machine.target != null
    BeveledPanel(
        modifier = modifier.then(
            if (clickable) {
                Modifier
                    .clickable { onOpen(machine.target!!) }
                    .pointerHoverIcon(PointerIcon.Hand)
            } else {
                Modifier
            },
        ),
    ) {
        Column {
            Text(text = machine.name, style = NublarType.Header)
            Spacer(Modifier.height(6.dp))
            Text(
                text = machine.status.linkLabel(),
                style = NublarType.SystemText,
                color = machine.status.color,
            )
        }
    }
}
