# NublarOS

A retro-futurist desktop theme and operating-system reskin inspired by the control-room computers in *Jurassic Park*.

NublarOS is designed to feel like a believable 1993 park-operations workstation rather than a generic neon “hacker” theme. Its visual language combines:

- Classic Macintosh System 7-style windowing
- SGI IRIX workstation aesthetics
- Industrial park-control dashboards
- Dense system-status displays
- Dark blueprint maps and paddock diagrams
- Chunky beveled panels
- Restrained green, red, blue, gray, and cream accents

The primary target is Garuda Linux, with a lighter macOS version planned afterward.

---

## Project Goals

NublarOS should transform the desktop into a convincing fictional park-control environment while remaining comfortable enough for daily use.

The theme should:

- Feel inspired by the original *Jurassic Park* control room
- Screen layouts (button arrangement, panel structure, status-log/floor-plan
  composition) may be reproduced closely, including direct recreations of
  specific film screens, under the project's fair-use/parody basis — see
  "Legal and Asset Guidelines"
- Use original iconography, maps, labels, sounds, and interface elements
  where a direct recreation isn't the specific goal
- Remain readable and practical for everyday work
- Support both a lightweight theme-only installation and a full immersive setup
- Treat system information as fictional park telemetry
- Be modular so users can install only the pieces they want

The preferred design direction is a **cleaned-up retro recreation**: recognizably early-1990s, but polished enough for modern desktop use.

---

## Platform Strategy

### Primary Platform: Garuda Linux

Linux offers the greatest control over:

- Window decorations
- Shell or panel styling
- Icons
- Cursors
- Terminal profiles
- Login screens
- Lock screens
- Desktop widgets
- System monitors
- Sounds
- Wallpapers
- Application themes

The first development task is to determine the active desktop environment:

```bash
echo "$XDG_CURRENT_DESKTOP"
```

Likely results include:

```text
GNOME
```

or:

```text
KDE
```

### KDE Plasma

KDE Plasma is the preferred target for the most complete version of NublarOS.

Potential components:

- Global theme
- Plasma style
- Window decoration
- Color scheme
- Icon theme
- Cursor theme
- Konsole profile
- Panel layout
- Application launcher
- System monitor widgets
- Splash screen
- SDDM login theme
- Lock-screen treatment
- Notification styling

### GNOME

A GNOME version is still viable, but some system chrome is more constrained.

Potential components:

- GTK theme
- GNOME Shell theme
- Icon theme
- Cursor theme
- Terminal profile
- Dock styling
- Top-bar styling
- GNOME extensions
- Conky or Eww dashboard
- Lock-screen and login styling where practical

### Secondary Platform: macOS

The macOS version will focus on creating a themed workspace rather than replacing all system chrome.

Potential components:

- Wallpaper pack
- Custom folder and application icons
- Terminal profile
- Menu-bar status utility
- Desktop widgets
- Screen saver
- Custom alert sounds
- SwiftUI-based NublarOS dashboard
- Optional full-screen control-room mode

---

## Working Name

# NublarOS

Suggested internal component names:

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

---

## Visual Direction

NublarOS should resemble an industrial command-and-control workstation.

It should not resemble:

- Modern cyberpunk
- Neon synthwave
- A generic green-on-black terminal
- A heavily distressed CRT simulation
- A novelty interface that becomes tiring after ten minutes

Direct recreations of specific film screens (the "SYSTEM SECURED" control-room
plan-view screen, etc.) are in scope — see "Legal and Asset Guidelines" for
the basis.

The visual system should use:

- Thick dark frames
- Recessed panels
- Beveled borders
- Square buttons
- Compact labels
- Small status indicators
- Dense but organized information
- Minimal transparency
- Minimal gradients
- Strong alignment
- Blueprint and infrastructure diagrams

---

## Core Color Palette

Initial palette:

| Role | Color |
|---|---|
| Monitor gray | `#737A83` |
| Dark frame | `#303942` |
| Inset panel | `#515C68` |
| Screen black | `#07100D` |
| Status green | `#54D875` |
| Warning red | `#E55454` |
| Map blue | `#397FA4` |
| Paddock green | `#3F8F58` |
| Label cream | `#E2E0BF` |
| Highlight yellow | `#D5CD58` |

These values are starting points and should be tested for contrast and accessibility.

### Color Usage Rules

- Gray and blue-gray should dominate.
- Green should indicate normal operation.
- Yellow should indicate attention or moderate load.
- Red should indicate failure, disconnection, or critical alerts.
- Cream should be used for high-contrast labels.
- Pure white should be rare.
- Bright accent colors should not cover large areas.

---

## Typography

NublarOS should use three typographic roles.

### 1. Interface Headers

Use a square, condensed, industrial sans-serif for:

- Window titles
- Section headers
- Status labels
- Menu categories
- Dashboard titles

Examples of the desired style:

- Eurostile-like
- Microgramma-like
- Bank Gothic-like
- Condensed technical sans-serif

Use freely licensed alternatives for distribution.

### 2. System Text

Use a readable bitmap-inspired or monospaced font for:

- Terminal output
- Status logs
- File paths
- Metrics
- Paddock labels
- Event timestamps

The result should evoke classic Macintosh and SGI displays without sacrificing readability.

### 3. Large Display Text

Use a bold display face sparingly for labels such as:

```text
ANIMAL PADDOCKS
VEHICLE STATUS
SYSTEM SECURED
PARK CONTROL
AUXILIARY POWER
```

Not every element should use a pixel font.

---

## Desktop Layout

The desktop should function like a park-operations console.

### Top Bar

The top bar should become a narrow system-status strip.

Example:

```text
NUBLAROS | SECTOR STATUS: CLEAR | VEHICLES: 04 | FENCES: ARMED | 12:42
```

Suggested mappings:

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

### Dock or Application Launcher

Applications should be represented as equipment or subsystem tiles.

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

Icons should use simple monochrome or limited-color symbols with thick outlines.

### Wallpaper

The default wallpaper should look like an operations display rather than a movie poster.

Possible elements:

- Original island silhouette
- Visitor-center floor plan
- Paddock boundaries
- Utility routes
- Sector labels
- Vehicle routes
- Faint coordinate grid
- Maintenance tunnels
- Power distribution lines
- Small system-state labels

Example labels:

```text
LEVEL G
GRID 04
SYSTEM SECURED
PADDOCK 03
SERVICE ROUTE
AUXILIARY GRID
```

The wallpaper should remain dark and restrained so windows remain readable.

---

## Window Design

Each application window should resemble a detachable subsystem panel.

### Structure

```text
┌──────────────────────────────────────┐
│ VEHICLE TRACKING              LEVEL G│
├──────────────────────────────────────┤
│                                      │
│          application content         │
│                                      │
├──────────────────────────────────────┤
│ SCREEN: TOUR CONTROL     PARKNET 03  │
└──────────────────────────────────────┘
```

### Window Styling

- Thick dark-gray outer border
- Thin light inner highlight
- Recessed title bar
- Square controls
- All-caps title
- Compact footer strip
- Minimal rounded corners
- Minimal transparency
- Strong separation between panels

### Window Controls

Possible labels:

- `QUIT`
- `HIDE`
- `HOLD`
- `VIEW`

A less intrusive implementation may preserve familiar symbols while rendering them as chunky square buttons.

---

## Terminal Design

The terminal should evoke the SGI workstation side of the control room.

Suggested startup text:

```text
NUBLAROS PARK SYSTEMS COMMAND INTERFACE
NODE: VISITOR.CENTER
USER: OPERATIONS
COMMUNICATION LINK: ACTIVE

visitor.center %
```

Suggested shell aliases:

```bash
alias park-status='fastfetch'
alias sys-load='btop'
alias paddocks='ls'
alias access='cd'
alias glitches='journalctl -p warning'
alias parknet='ping'
alias incidents='journalctl -p err'
alias comms='ip addr'
alias grid-status='systemctl --failed'
```

Suggested login summary:

```text
SYSTEM SECURED
7 PADDOCK SYSTEMS ONLINE
4 TOUR VEHICLES ACTIVE
0 CRITICAL GLITCHES
```

Potential terminal integrations:

- Fastfetch
- Btop
- Tmux
- Starship
- Custom shell prompt
- Custom MOTD
- Custom command aliases
- Custom ASCII or block-art logo

---

## Signature Component: Nedryland Monitor

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

### Dashboard Features

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

### Status Mapping

- Green sector: normal
- Yellow sector: moderate load
- Orange sector: degraded
- Red sector: failed
- Flashing sector: disconnected or urgent
- Gray sector: unavailable

---


## Signature Component: Storm Simulation

NublarOS should include a dedicated storm-analysis module inspired by the hurricane visualization shown on the SGI workstations in the control room.

The film display presented a real-time-looking 3D hurricane animation, although the on-set graphics were pre-generated and fed to the monitors by an off-stage graphics team. NublarOS should recreate the visual language of an early-1990s scientific workstation while using original assets and modern rendering techniques.

Possible module names:

- StormTrack
- Cyclone Control
- Island Weather System
- Tempest Model
- Weather Operations

**StormTrack** is the preferred working name.

### Goals

The interface should:

- Feel like a scientific simulation rather than a modern consumer weather app
- Keep the island at the center of the operational picture
- Visualize both the storm and its effect on park infrastructure
- Support an offline fictional simulation mode
- Optionally support live weather data later
- Integrate its alerts with the Nedryland Monitor
- Remain useful as an ambient full-screen display

### Core Views

#### Island Overview

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

#### Atmospheric Model

Show:

- Animated cloud mass
- Pressure field
- Wind direction
- Wind velocity
- Rain intensity
- Storm rotation
- Forecast uncertainty
- Time-step indicator

#### Infrastructure Impact

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

### Visual Style

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

### Controls

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

### Rendering Strategy

#### First Prototype

Use the simplest approach capable of proving the visual design:

- SVG island and infrastructure overlays
- Canvas-rendered storm field
- Pre-generated cloud frames
- Animated storm track
- Static pressure contours
- Timeline playback
- Simulated sector alerts

#### Full Version

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

### Simulation Data Model

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

### Integration with Nedryland Monitor

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

---

## Signature Component: Spatial File Navigator

NublarOS should include a visual file browser inspired by `fsn`, the experimental SGI File System Navigator used by Lex during the “It’s a UNIX system” scene.

In the film, Lex opens the `/usr` directory on an SGI workstation and navigates a three-dimensional representation of the file system. The NublarOS version should capture that sense of spatial exploration without replacing the normal file manager for routine work.

Possible module names:

- System Navigator
- ParkNet Navigator
- Archive Navigator
- File System Navigator
- Spatial Archives
- Grid Explorer

**System Navigator** is the preferred user-facing name. Internally, the project may refer to it as `nublar-fsn`.

### Goals

The navigator should:

- Provide a spatial overview of folders and files
- Make directory depth visually understandable
- Feel like an experimental SGI workstation tool
- Work as a launcher and exploration interface
- Preserve access to ordinary file operations
- Avoid becoming the only way to manage files
- Support keyboard navigation
- Remain functional with large directories

### Core Metaphor

A directory is represented as a raised platform or city block.

Its contents appear as objects placed on that platform:

- Directories become towers or larger blocks
- Regular files become smaller blocks
- Executables become distinct machine-like structures
- Symbolic links become bridges or cables
- Mounted volumes become separate islands or districts
- Hidden files appear only when the hidden-layer toggle is enabled

Directory hierarchy may be shown through:

- Height
- Nested platforms
- Connected districts
- Camera movement
- Breadcrumb path
- Color-coded depth
- Parent-child bridges

### Visual Style

The interface should draw from early real-time 3D graphics:

- Flat-shaded geometry
- Limited color palette
- Dark background
- Visible grid plane
- Low-poly objects
- Strong perspective
- Sparse lighting
- Chunky labels
- Minimal texture use
- Optional wireframe mode
- Deliberately restrained frame rate option

The goal is not photorealism. It should look like expensive 1993 workstation graphics rendered smoothly on modern hardware.

### File Representation

Suggested default mappings:

| File Type | Visual Representation |
|---|---|
| Directory | Tall rectangular tower |
| Regular file | Short rectangular block |
| Executable | Mechanical tower with indicator |
| Image | Thin panel |
| Video | Wide display block |
| Audio | Speaker-like block |
| Archive | Reinforced storage crate |
| Source code | Stacked terminal slab |
| Configuration | Control panel |
| Symbolic link | Bridge or light beam |
| Mounted volume | Separate platform |
| Hidden file | Dim translucent block |
| Broken link | Red interrupted bridge |

### Size and Metadata Mapping

Object properties may encode file metadata:

- Footprint: file size
- Height: directory item count or file size
- Color: file type
- Brightness: recent modification
- Pulse: active change
- Label: filename
- Border: permissions
- Marker: executable status
- Warning symbol: inaccessible or broken item

Scaling must be logarithmic so very large files do not dominate the entire scene.

### Navigation

Required interactions:

- Orbit camera
- Pan camera
- Zoom
- Select object
- Open directory
- Move to parent directory
- Open selected file
- Reveal metadata
- Copy path
- Open terminal at location
- Open location in standard file manager
- Toggle labels
- Toggle hidden files
- Toggle wireframe
- Search by filename
- Reset camera

Keyboard controls should be fully documented.

Suggested controls:

```text
WASD / ARROWS     MOVE
MOUSE DRAG        ORBIT
SCROLL            ZOOM
ENTER             OPEN
BACKSPACE         PARENT
SPACE             INSPECT
T                 OPEN TERMINAL
F                 OPEN STANDARD FILE MANAGER
H                 HIDDEN FILES
L                 LABELS
G                 GRID
R                 RESET VIEW
/                 SEARCH
```

### Selection and Inspection Panel

Selecting an object should open an inset information panel.

Example:

```text
SYSTEM OBJECT
NAME: Visitor.Center
TYPE: DIRECTORY
CONTENTS: 148
MODIFIED: 12:42
ACCESS: OPERATIONS
LOCATION: /usr/Visitor.Center
```

Actions:

- Open
- Inspect
- Copy path
- Open terminal
- Open standard file manager
- Rename
- Move to Purge
- View permissions

Destructive actions should require clear confirmation.

### Layout Modes

#### City Mode

Directories and files appear as a navigable low-poly city.

Best for:

- Visual browsing
- Demonstrations
- Full-screen use
- Recreating the SGI-inspired experience

#### Column Mode

Directories appear as connected platforms arranged by depth.

Best for:

- Understanding hierarchy
- Navigating deeply nested projects
- Preserving spatial context

#### Map Mode

Directories are arranged as a top-down facility map.

Best for:

- Large directory trees
- Fast overview
- Integration with the broader NublarOS map aesthetic

The first milestone should implement only City Mode.

### Performance Requirements

The navigator must not attempt to render an unlimited number of filesystem entries.

Initial safeguards:

- Default object limit per directory
- Progressive loading
- Level-of-detail reduction
- File-type aggregation
- Logarithmic size scaling
- Label culling
- Cached directory metadata
- Background scanning
- Configurable hidden-file behavior

Large directories should group excess items into aggregate structures such as:

```text
SOURCE FILES × 428
CACHE OBJECTS × 1,204
LOG FILES × 312
```

### Safety Requirements

The navigator is an alternate view of the real file system and must be conservative.

Requirements:

- Read-only mode by default in the first prototype
- No drag-to-delete behavior
- No permanent deletion
- Destructive actions require confirmation
- Symlinks must not be followed recursively without limits
- Permission failures must be shown clearly
- Network mounts should be opt-in
- Filesystem watchers must be resource-limited
- Standard file-manager access must always be available

### Technology Options

Potential implementations:

#### Qt/QML

Advantages:

- Strong Linux desktop integration
- GPU-accelerated scene graph
- Native filesystem APIs
- Good keyboard and window management
- Fits a KDE-first implementation

#### Tauri with WebGL

Advantages:

- Fast UI iteration
- Three.js or PixiJS ecosystem
- Potential cross-platform reuse
- Easier visual prototyping

#### Godot

Advantages:

- Excellent 3D scene workflow
- Fast prototyping
- Strong input and camera tools
- Particularly suitable for the city metaphor

Tradeoffs:

- Less native desktop integration
- Additional packaging complexity
- Filesystem operations require careful sandboxing and permissions

For a KDE-first NublarOS implementation, evaluate Qt/QML and Godot first. A short technical spike should compare filesystem integration, rendering effort, package size, and startup time.

### First Prototype Scope

The first prototype should:

- Open a user-selected directory
- Render directories and files as low-poly blocks
- Support orbit, pan, and zoom
- Show filename labels
- Open subdirectories
- Navigate to the parent directory
- Display metadata for a selected item
- Open a selected file with the system default application
- Open the location in the standard file manager
- Open a terminal at the current path
- Enforce read-only behavior

### Later Features

- Multiple layout modes
- Search animation
- File-operation animations
- Git status overlays
- Storage heat map
- Recent-file pulses
- Mounted-volume districts
- Network-share visualization
- Split view
- Saved camera positions
- VR or stereoscopic mode as an experimental extra
- Integration with the NublarOS application launcher
- Optional deliberately low-resolution rendering mode

---


## Project Phases

## Phase 1: Repository and Environment Audit

Goals:

- Identify KDE Plasma or GNOME
- Record Linux distribution and version
- Record display server
- Record shell
- Record terminal emulator
- Record screen resolution
- Identify installed theming tools
- Create repository structure

Commands:

```bash
echo "$XDG_CURRENT_DESKTOP"
echo "$XDG_SESSION_TYPE"
echo "$SHELL"
uname -a
cat /etc/os-release
```

Deliverables:

- `docs/environment.md`
- Initial repository
- Platform-specific setup notes
- Confirmed primary desktop target

---

## Phase 2: Static Visual Prototype

Create one 2560×1440 concept image showing:

- Wallpaper
- Top bar
- Dock or launcher
- Terminal
- File manager
- Settings panel
- Nedryland Monitor
- Notifications
- Window decorations

The prototype should establish:

- Color balance
- Typography
- Border thickness
- Icon style
- Panel density
- Naming conventions
- Degree of retro fidelity

Deliverables:

- `design/desktop-concept.png`
- `design/palette.md`
- `design/typography.md`
- `design/component-reference.md`

---

## Phase 3: Minimum Viable Theme

Implement the parts that create the largest visual impact with the least complexity.

Initial scope:

- Wallpaper
- Color scheme
- Fonts
- Terminal profile
- Shell prompt
- Top panel
- Dock or launcher
- Core application icons
- Cursor theme
- System sounds

This phase should provide most of the NublarOS feel without requiring deep system modification.

Deliverables:

- Install script
- Uninstall script
- Terminal configuration
- Wallpaper pack
- Initial icon pack
- Initial color theme
- Screenshots
- Setup documentation

---

## Phase 4: Window and Shell Skinning

Add deeper desktop integration.

Potential KDE scope:

- Window decoration
- Plasma style
- Global theme
- Panel theme
- Launcher skin
- Notification skin
- Lock screen
- Splash screen
- SDDM theme

Potential GNOME scope:

- GTK theme
- Shell theme
- Top-bar styling
- Dock styling
- Extension presets
- Notification styling
- Lock-screen styling where practical

Deliverables:

- Full desktop theme package
- Theme preview screenshots
- Compatibility notes
- Safe restore procedure

---

## Phase 5: Nedryland Monitor

Build the custom system dashboard.

Recommended implementation options:

### Linux

- Qt/QML
- GTK
- Tauri
- Eww
- Conky for an early prototype

### macOS

- SwiftUI
- MenuBarExtra
- WidgetKit
- IOKit or system APIs where appropriate

The first Linux prototype should prioritize functionality over animation.

Deliverables:

- Dashboard application
- Configuration file
- Metric adapters
- Map component
- Alert component
- Packaging instructions

---

## Phase 6: Ambient Details

Add optional immersive effects.

Possible features:

- Startup sequence
- Lock-screen animation
- Park intercom sounds
- System warning tones
- Drive activity sounds
- Idle screen
- Faux operational messages
- Optional scanlines
- Optional CRT bloom
- Optional screen curvature
- “You didn’t say the magic word” Easter egg

These effects must be optional and disabled by default where they could interfere with daily use.

---

## Repository Structure

Suggested initial structure:

```text
nublaros/
├── README.md
├── LICENSE
├── docs/
│   ├── environment.md
│   ├── installation.md
│   ├── architecture.md
│   ├── naming.md
│   └── safety-and-restore.md
├── design/
│   ├── palette.md
│   ├── typography.md
│   ├── component-reference.md
│   ├── mockups/
│   └── source-assets/
├── assets/
│   ├── wallpapers/
│   ├── icons/
│   ├── cursors/
│   ├── sounds/
│   └── fonts/
├── linux/
│   ├── common/
│   ├── kde/
│   ├── gnome/
│   ├── terminal/
│   └── scripts/
├── macos/
│   ├── dashboard/
│   ├── terminal/
│   ├── icons/
│   └── wallpapers/
├── dashboard/
│   ├── src/
│   ├── assets/
│   ├── config/
│   └── tests/
├── stormtrack/
│   ├── src/
│   ├── assets/
│   ├── simulations/
│   ├── shaders/
│   └── tests/
├── system-navigator/
│   ├── src/
│   ├── assets/
│   ├── shaders/
│   ├── config/
│   └── tests/
├── scripts/
│   ├── install.sh
│   ├── uninstall.sh
│   ├── backup-current-theme.sh
│   └── restore-theme.sh
└── screenshots/
```

---

## Configuration

NublarOS should use a central configuration file where practical.

Example:

```yaml
theme:
  variant: standard
  scanlines: false
  crt_bloom: false
  animation_level: subtle

labels:
  hostname: VISITOR.CENTER
  user_role: OPERATIONS
  network_name: PARKNET

dashboard:
  refresh_interval_ms: 1000
  show_cpu: true
  show_memory: true
  show_disk: true
  show_network: true
  show_temperatures: true
  show_failed_services: true

audio:
  enabled: false
  startup_sound: true
  warning_sounds: true
```

---

## Safety and Reversibility

Theme installation must be reversible.

Before making changes, the installer should:

- Detect the desktop environment
- Back up current theme settings
- Back up terminal settings
- Record installed packages
- Avoid overwriting user-created files
- Create a restore script
- Warn before modifying login-manager configuration

Required scripts:

```text
backup-current-theme.sh
install.sh
uninstall.sh
restore-theme.sh
```

The project should not require root access for components that can be installed at the user level.

Login-screen and system-wide changes should be optional.

---

## Legal and Asset Guidelines

NublarOS is a free, non-commercial, fan-made parody project. Per counsel
consulted by the project maintainer, direct recreations of the film's
control-room screens — including close reproductions of specific screen
layouts such as the "SYSTEM SECURED" plan-view display — are treated as
fair use on that basis (parody, non-commercial, transformative recreation
rather than extracted footage). This is a project-level legal
determination, not a general license — anyone redistributing or adapting
NublarOS should independently evaluate their own fair-use position, since
it depends on jurisdiction and use (free/non-commercial vs. commercial,
parody framing, amount/substantiality reproduced).

Under this basis, NublarOS may:

- Recreate specific film screen layouts (panel arrangement, button
  placement, status-log structure, floor-plan composition) closely,
  including built-from-scratch reproductions of screens like the
  control-room plan view
- Use in-universe terminology and labels drawn from the film

NublarOS should still not distribute, since these are extracted directly
from the source rather than recreated:

- Film screenshots or extracted movie graphics (frame captures, texture
  rips)
- Movie audio
- Official Jurassic Park / InGen logo files (recreate the *idea* of park
  branding, not the registered logo artwork itself)
- Proprietary fonts (use freely licensed alternatives, per "Typography")

Where practical, prefer building original assets (island silhouette,
paddock map, icons, sounds) inspired by the film's visual language — but
this is a design preference for variety and originality, not a legal
requirement, given the fair-use basis above.

---


## Design Reference

Primary research reference:

- Fabien Sanglard, “Jurassic Park computers in excruciating detail”
- https://fabiensanglard.net/jurrasic_park_computers/index.html

Relevant details include:

- SGI workstations used for the hurricane display
- The pre-generated control-room animations
- SGI IRIX system-monitor visuals
- The `fsn` File System Navigator used in Lex's UNIX scene
- Macintosh System 7 and SGI IRIX visual characteristics

Use the article as historical and visual research only. Distributed NublarOS assets should remain original.

---



### JurassicSystems.com Code Reference

`jurassicsystems.com` and its public GitHub repository are useful as a
code reference for implementing a fictional command interface and
scripted park-system scenarios.

Repository:

- https://github.com/tojrobinson/jurassicsystems.com

The project is MIT licensed and may be consulted for software patterns
such as:

- Command registration
- Command history
- Keyboard handling
- Delayed terminal output
- Scripted interface sequences
- Audio and video event timing
- Fake system errors
- State-driven command availability
- Interactive nostalgia experiences

NublarOS should not use this project as its architectural foundation.
Instead, it should implement a cleaner modular command-and-scenario
engine with separate command parsing, state management, output
rendering, event sequencing, and application launching.

Suggested NublarOS structure:

```text
command-interface/
├── commands/
│   ├── help.ts
│   ├── park-status.ts
│   ├── stormtrack.ts
│   └── navigator.ts
├── scenarios/
│   ├── storm.yaml
│   └── security-lockout.yaml
├── core/
│   ├── parser.ts
│   ├── registry.ts
│   ├── history.ts
│   ├── sequencer.ts
│   └── renderer.ts
└── assets/
    └── original-sounds/
```

Potential NublarOS commands:

```text
help
park-status
grid-status
weather
stormtrack
paddocks
vehicles
incidents
archives
navigator
clear
about
```

Potential scripted scenarios:

```text
scenario security-lockout
scenario tropical-storm
scenario auxiliary-power
scenario fence-failure
```

The MIT license applies to the project’s authored software, but it does
not automatically grant rights to any third-party movie clips, audio,
logos, fonts, screenshots, or other copyrighted media included in the
repository. NublarOS must replace all such material with original
assets.

---


## Initial Claude Code Tasks

Claude Code should begin with the following tasks.

### Task 1: Audit the Current Linux Environment

Create a script that records:

- Distribution
- Desktop environment
- Session type
- Shell
- Terminal emulator
- Screen resolution
- KDE or GNOME version
- Installed theming utilities

Write the output to:

```text
docs/environment.md
```

### Task 2: Scaffold the Repository

Create the repository structure described above.

Add placeholder files where useful.

### Task 3: Create the Core Palette

Create:

```text
design/palette.md
```

Include:

- Hex values
- Semantic color names
- Usage rules
- Contrast notes
- Light and dark variants where needed

### Task 4: Create a Terminal Prototype

Build an initial terminal experience using:

- Custom shell prompt
- Login banner
- Fastfetch
- Btop
- Aliases
- NublarOS labels

Ensure installation is reversible.

### Task 5: Create the First Wallpaper

Produce an original wallpaper specification using:

- Dark island silhouette
- Blueprint grid
- Paddock boundaries
- Sector labels
- Utility routes
- Restrained status overlays

Do not use copyrighted film assets.

### Task 6: Build the Minimum Viable Installer

Create:

```text
scripts/backup-current-theme.sh
scripts/install.sh
scripts/uninstall.sh
scripts/restore-theme.sh
```

The installer should initially support only user-level changes.

### Task 7: Choose the Dashboard Technology

Evaluate:

- Qt/QML
- GTK
- Tauri
- Eww
- Conky

Recommend one based on:

- Desktop integration
- Performance
- Packaging
- Animation support
- System-metric access
- Cross-platform potential
- Ease of maintenance

Document the decision in:

```text
docs/architecture.md
```

---


### Task 8: Prototype StormTrack

Create a visual prototype that:

- Loads a fictional storm simulation from YAML or JSON
- Renders an original island outline
- Animates the storm center along a projected path
- Displays cloud, pressure, and infrastructure overlays
- Supports play, pause, stepping, and timeline scrubbing
- Emits simulated sector warnings

Document the rendering approach and data model.

### Task 9: Prototype System Navigator

Create a read-only technical spike that:

- Opens a user-selected directory
- Renders files and folders as low-poly objects
- Supports orbit, pan, and zoom
- Allows entering directories and returning to the parent
- Displays selected-item metadata
- Opens a terminal or standard file manager at the current path
- Limits the number of rendered objects safely

Compare Qt/QML and Godot before selecting the implementation technology.


## Definition of the First Milestone

The first milestone is complete when:

- The current desktop environment has been identified
- The repository has been scaffolded
- A backup and restore workflow exists
- The NublarOS palette is documented
- A wallpaper is installed
- The terminal is themed
- The top panel and launcher use NublarOS terminology
- At least ten core icons exist
- Screenshots are included
- Installation and removal are documented

The first milestone does not require:

- A full custom dashboard
- A login-screen theme
- A complete application icon set
- A macOS version
- Animated island maps
- Sound effects
- CRT effects

---

## Long-Term Vision

The finished NublarOS environment should feel like a complete park-operations workstation:

- The desktop becomes a sector map
- Applications become park subsystems
- System health becomes grid status
- Network activity becomes ParkNet traffic
- Storage becomes the Mini Array
- Notifications become incident reports
- The terminal becomes system access
- The system monitor becomes Nedryland Monitor
- Storm analysis becomes StormTrack
- File browsing becomes a spatial System Navigator
- The lock screen becomes White Rabbit
- The entire desktop feels like one coherent fictional operating system

The result should be immersive, practical, original, reversible, and comfortable enough to use every day.
