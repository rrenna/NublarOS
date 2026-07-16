# Nedryland Monitor — Design & Research Notes

Detailed notes for the signature system-dashboard component. Summarized in the
[README](../README.md#signature-components); see also
[`docs/architecture.md`](architecture.md) for the current implementation
status (a working Compose Desktop dashboard with live `oshi` metrics).

---

The Nedryland Monitor is the key custom application that will make NublarOS feel like a real fictional operating environment.

It should convert real computer telemetry into park-operations terminology.

| Real Metric | NublarOS Label |
|---|---|
| CPU usage | Main Grid Load |
| RAM usage | Operations Memory |
| Disk space | Mini Array Capacity |
| Network activity | ParkNet Traffic |
| Running processes | Active Systems |
| Temperatures | Environmental Control |
| Battery | Auxiliary Power |
| Failed services | Glitches |
| Mounted drives | System Volumes |
| Uptime | Park Runtime |
| Fan speed | Ventilation Systems |
| GPU load | Visual Systems Load |

## Dashboard Features

Initial version:

- CPU usage
- Memory usage
- Disk usage
- Network activity
- System uptime
- Failed services
- Current time
- Hostname
- Operating-system version

Later version:

- Interactive island map
- Sector-based system status
- Animated vehicle markers
- Alert history
- Faux surveillance feeds
- Application launcher
- Audio alerts
- Configurable themes
- Full-screen control-room mode

## Status Mapping

- Green sector: normal
- Yellow sector: moderate load
- Orange sector: degraded
- Red sector: failed
- Flashing sector: disconnected or urgent
- Gray sector: unavailable
