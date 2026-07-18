package os.nublar.dashboard.metrics

import oshi.SystemInfo

/** Real metric -> NublarOS label mapping lives in design/component-reference.md. */
data class SystemSnapshot(
    val mainGridLoadPercent: Double,
    val operationsMemoryUsedPercent: Double,
    val miniArrayCapacityUsedPercent: Double,
    val parkRuntimeSeconds: Long,
    val hostname: String,
)

/**
 * Thin wrapper around oshi (pure-Java, cross-platform system info, no native
 * build step). [snapshot] blocks for ~200ms to sample CPU load between
 * ticks — always call it from a background dispatcher, never the UI thread.
 */
class SystemMetricsReader {
    private val systemInfo = SystemInfo()

    fun snapshot(): SystemSnapshot {
        val hal = systemInfo.hardware
        val os = systemInfo.operatingSystem

        val cpu = hal.processor
        val prevTicks = cpu.systemCpuLoadTicks
        Thread.sleep(200)
        // getSystemCpuLoadBetweenTicks returns -1.0 when tick data is
        // insufficient (e.g. first call) — clamp so the UI never shows "-100%".
        val load = (cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0).coerceIn(0.0, 100.0)

        val mem = hal.memory
        val memUsedPercent = (mem.total - mem.available).toDouble() / mem.total * 100.0

        val fileStores = os.fileSystem.fileStores
        val totalSpace = fileStores.sumOf { it.totalSpace }
        val usableSpace = fileStores.sumOf { it.usableSpace }
        val diskUsedPercent = if (totalSpace > 0) {
            (totalSpace - usableSpace).toDouble() / totalSpace * 100.0
        } else 0.0

        return SystemSnapshot(
            mainGridLoadPercent = load,
            operationsMemoryUsedPercent = memUsedPercent,
            miniArrayCapacityUsedPercent = diskUsedPercent,
            parkRuntimeSeconds = os.systemUptime,
            hostname = os.networkParams.hostName,
        )
    }
}
