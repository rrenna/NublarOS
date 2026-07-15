# Component Naming Reference

Canonical mapping from real desktop concepts to NublarOS fictional
terminology. Keep this file in sync with `README.md` — this is the
single source of truth used by scripts, config keys, and UI copy.

## Internal Component Names

| Component | Name |
|---|---|
| Desktop shell | Nublar Shell |
| Terminal profile | ParkNet Terminal |
| System monitor | Nedryland Monitor |
| Application launcher | Paddock Control |
| Lock screen | White Rabbit |
| File manager theme | Archives |
| Settings app styling | Control Systems |
| Notification center | Incident Log |
| Network monitor | Communications Link |
| Storage monitor | Mini Array |
| Storm module | StormTrack |
| Spatial file browser | System Navigator (`nublar-fsn`) |

## Top Bar

| Normal Desktop Concept | NublarOS Label |
|---|---|
| Workspace | Sector |
| Wi-Fi | Communications Link |
| Battery | Auxiliary Power |
| Notifications | Incidents |
| Network | ParkNet |
| System errors | Glitches |
| User menu | Operations |
| Clock | Local / Nublar |

Top bar format:

```text
NUBLAROS | SECTOR STATUS: CLEAR | VEHICLES: 04 | FENCES: ARMED | 12:42
```

## Applications / Dock

| Application | NublarOS Label |
|---|---|
| Files | Archives |
| Browser | External Net |
| Terminal | System Access |
| Settings | Control Systems |
| Music | Audio Feed |
| Video player | VidNet |
| Trash | Purge |
| Steam | Recreation Systems |
| System monitor | Load Status |
| Package manager | System Provisioning |

## Nedryland Monitor Metrics

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

Status colors: green = normal, yellow = moderate load, orange = degraded,
red = failed, flashing = disconnected/urgent, gray = unavailable.

## Window Controls

| Standard | NublarOS Label |
|---|---|
| Close | `QUIT` |
| Minimize | `HIDE` |
| Maximize/Restore | `HOLD` |
| Focus/inspect | `VIEW` |

A less intrusive implementation may keep familiar symbols but render them as
chunky square buttons rather than replacing the labels outright.
