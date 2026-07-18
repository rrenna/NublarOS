package os.nublar.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import os.nublar.dashboard.metrics.SystemMetricsReader
import os.nublar.dashboard.metrics.SystemSnapshot
import os.nublar.designsystem.NublarType
import os.nublar.designsystem.SectorStatus
import os.nublar.designsystem.components.BeveledPanel

private const val REFRESH_INTERVAL_MS = 1000L

@Composable
fun MetricsGrid(
    metricsReader: SystemMetricsReader,
    onToggleFullscreen: () -> Unit,
) {
    var snapshot by remember { mutableStateOf<SystemSnapshot?>(null) }

    LaunchedEffect(metricsReader) {
        while (true) {
            try {
                snapshot = withContext(Dispatchers.IO) { metricsReader.snapshot() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Keep the last good snapshot and keep polling — a transient
                // oshi failure must not silently freeze every tile forever.
                System.err.println("Metrics snapshot failed: ${e.message}")
            }
            delay(REFRESH_INTERVAL_MS)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricTile("MAIN GRID LOAD", snapshot?.mainGridLoadPercent, "%")
            MetricTile("OPERATIONS MEMORY", snapshot?.operationsMemoryUsedPercent, "%")
            MetricTile("MINI ARRAY CAPACITY", snapshot?.miniArrayCapacityUsedPercent, "%")
        }
        Button(onClick = onToggleFullscreen) {
            Text("CONTROL ROOM MODE", style = NublarType.SystemText)
        }
    }
}

@Composable
private fun MetricTile(label: String, value: Double?, unit: String) {
    val status = when {
        value == null -> SectorStatus.Unavailable
        value < 60 -> SectorStatus.Normal
        value < 80 -> SectorStatus.Moderate
        value < 95 -> SectorStatus.Degraded
        else -> SectorStatus.Failed
    }
    BeveledPanel {
        Column {
            Text(text = label, style = NublarType.Header)
            Text(
                text = value?.let { "%.0f%s".format(it, unit) } ?: "—",
                style = NublarType.Display,
                color = status.color,
            )
        }
    }
}
