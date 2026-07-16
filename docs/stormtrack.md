# StormTrack — Design & Research Notes

Detailed notes for the storm-simulation signature component. Summarized in the
[README](../README.md#signature-components). Rendering direction lives in
[`docs/architecture.md`](architecture.md#stormtrack-rendering); the
`stormtrack/` module is currently a placeholder.

---

NublarOS should include a dedicated storm-analysis module inspired by the hurricane visualization shown on the SGI workstations in the control room.

The film display presented a real-time-looking 3D hurricane animation, although the on-set graphics were pre-generated and fed to the monitors by an off-stage graphics team. NublarOS should recreate the visual language of an early-1990s scientific workstation while using original assets and modern rendering techniques.

Possible module names:

- StormTrack
- Cyclone Control
- Island Weather System
- Tempest Model
- Weather Operations

**StormTrack** is the preferred working name.

## Goals

The interface should:

- Feel like a scientific simulation rather than a modern consumer weather app
- Keep the island at the center of the operational picture
- Visualize both the storm and its effect on park infrastructure
- Support an offline fictional simulation mode
- Optionally support live weather data later
- Integrate its alerts with the Nedryland Monitor
- Remain useful as an ambient full-screen display

## Core Views

### Island Overview

Show:

- Original island silhouette
- Paddock and sector boundaries
- Visitor facilities
- Dock and helipad
- Vehicle routes
- Power infrastructure
- Communications nodes
- Storm center
- Current wind radius
- Projected path
- Sector warning states

### Atmospheric Model

Show:

- Animated cloud mass
- Pressure field
- Wind direction
- Wind velocity
- Rain intensity
- Storm rotation
- Forecast uncertainty
- Time-step indicator

### Infrastructure Impact

Show predicted or simulated effects on:

- Main power grid
- Auxiliary power
- Perimeter fences
- Communications
- Dock operations
- Helipad operations
- Vehicle routes
- Visitor facilities
- Paddock access
- Emergency shelters

## Visual Style

- Dark blue-gray map background
- Thin vector coastlines
- Low-resolution or pixel-stepped cloud rendering
- Sparse contour lines
- Green, yellow, orange, and red intensity bands
- Compact all-caps labels
- Chunky playback controls
- Timestamped forecast frames
- Visible simulation step number
- Minimal anti-aliasing where practical
- Optional frame-by-frame playback
- No glossy modern weather-app styling

Suggested labels:

```text
STORM TRACKING
CYCLONE MODEL
ISLAND WEATHER SYSTEM
PRESSURE FIELD
WIND VELOCITY
PROJECTED LANDFALL
SECTOR EXPOSURE
EMERGENCY WEATHER MODE
MODEL TIME
SIMULATION STEP
```

## Controls

Suggested controls:

- Play
- Pause
- Step forward
- Step backward
- Restart simulation
- Change playback speed
- Toggle cloud layer
- Toggle pressure layer
- Toggle wind vectors
- Toggle infrastructure
- Toggle projected path
- Select forecast model
- Select simulation time
- Enter full-screen mode

The controls should resemble physical workstation buttons rather than modern media controls.

## Rendering Strategy

### First Prototype

Use the simplest approach capable of proving the visual design:

- SVG island and infrastructure overlays
- Canvas-rendered storm field
- Pre-generated cloud frames
- Animated storm track
- Static pressure contours
- Timeline playback
- Simulated sector alerts

### Full Version

Potential technologies:

- WebGL
- Three.js
- PixiJS
- Qt Quick Scene Graph
- Vulkan-backed Qt rendering
- Metal for a future macOS version

Potential full-version features:

- GPU-rendered cloud field
- Particle-based wind visualization
- Procedural storm generation
- Pressure and precipitation textures
- Time-step simulation
- Multiple forecast paths
- Configurable island coordinates
- Live weather-data adapter
- Historical storm playback
- Exportable screenshots and recordings

## Simulation Data Model

A fictional simulation should be loadable from a local configuration file.

Example:

```yaml
storm:
  id: CYCLONE-04
  name: CLARISSA
  category: 2
  center:
    latitude: 10.42
    longitude: -84.17
  heading_degrees: 302
  forward_speed_kmh: 18
  central_pressure_hpa: 972
  maximum_wind_kmh: 168

timeline:
  start: 1993-06-11T18:00:00
  step_minutes: 15
  frame_count: 48

overlays:
  clouds: true
  pressure: true
  wind: true
  rainfall: false
  infrastructure: true
  projected_path: true
```

## Integration with Nedryland Monitor

StormTrack alerts should appear in the main dashboard.

Examples:

```text
WEATHER ADVISORY: EAST DOCK
SECTOR 04 EXPOSURE: HIGH
TOUR ROUTE B SUSPENDED
AUXILIARY GRID LOAD INCREASING
HELIPAD OPERATIONS CLOSED
```

Storm status may also influence the color state of sectors on the main island map.
