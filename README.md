# NublarOS

A retro-futurist, cross-platform desktop application inspired by the control-room computers in *Jurassic Park*.

NublarOS is designed to feel like a believable 1993 park-operations workstation rather than a generic neon “hacker” theme. Its visual language combines:

- Classic Macintosh System 7-style windowing
- SGI IRIX workstation aesthetics
- Industrial park-control dashboards
- Dense system-status displays
- Dark blueprint maps and paddock diagrams
- Chunky beveled panels
- Restrained green, red, blue, gray, and cream accents

NublarOS is a standalone **Kotlin / Compose Multiplatform for Desktop**
application that runs on macOS, Linux, and Windows from one codebase — not a
Linux desktop theme or OS reskin. Everything (the dashboard, terminal, island
map, and future components) lives inside the app itself.

## Ultimate Goal

The end goal is a **"watch-along" mode**: run NublarOS alongside the film and
have it simulate the movie's events **in sync** with playback — the control
room reacting as the story unfolds. As scenes progress, the app drives its own
state to match: paddock security arming and failing, the storm closing in on
StormTrack, tour vehicles moving along their routes, power grid and fence
status changing, incidents logging, the raptor breakout, etc. The result is a
second-screen companion that turns the viewer's desktop into the park's
operations console for the duration of the movie.

### Sync tooling

The film plays in another window (or on a TV); NublarOS follows it via a
timeline of scripted events keyed to playback time. Intended controls:

- **Manual transport** — a simple start / pause / stop / seek that runs the
  event timeline on its own clock, which the user lines up with the film by
  hitting start at a known cue.
- **Playback integration where a platform allows it** — e.g. on macOS,
  observing the Apple TV / TV app (or a `Now Playing` / media-remote signal)
  to auto-start and stay aligned; equivalent hooks on other players/platforms
  where an accessible playback position exists.
- **Calibration / drift correction** — a nudge control to re-align if the app
  and film drift, and a chosen "sync point" scene the user can jump both to.

Manual transport is the baseline that works everywhere; automatic playback
integration is a per-platform enhancement layered on top.

---

## Implementation Status

Most of this document is the project *plan* and long-term vision. What is
actually built today is a **Kotlin / Compose Multiplatform for Desktop**
application suite (not the Qt/Tauri/etc. options weighed later in this file —
see [`docs/architecture.md`](docs/architecture.md) for the decision and the
current status list). Highlights:

- **Nedryland Monitor dashboard** (`dashboard/`) with live system metrics.
- A recreated **Control Room / Plan View** screen and an interactive
  **island map** (toggleable paddock / facility / dinosaur / vehicle / staff
  layers, right-drag panning, hover tooltips, and a JSON-backed paddock
  editor).
- An **embedded terminal** (pty4j + jediterm) for ParkNet Terminal.
- A shared **`design-system`** module (palette, typography, beveled UI kit).

`stormtrack/` and `system-navigator/` are placeholder modules, and the
film-synchronized "watch-along" simulation (see Ultimate Goal) is not started.

Build/run: `./gradlew :dashboard:run` (dashboard) or
`./gradlew :dashboard:runMapPreview` (standalone island-map editor).

---

## Project Goals

NublarOS should present a convincing fictional park-control environment as a
self-contained application, immersive enough to run as an ambient display yet
comfortable enough to interact with directly.

The app should:

- Feel inspired by the original *Jurassic Park* control room
- Screen layouts (button arrangement, panel structure, status-log/floor-plan
  composition) may be reproduced closely, including direct recreations of
  specific film screens, under the project's fair-use/parody basis — see
  "Legal and Asset Guidelines"
- Use original iconography, maps, labels, sounds, and interface elements
  where a direct recreation isn't the specific goal
- Remain readable and practical
- Treat system information as fictional park telemetry
- Be modular, so components (dashboard, map, terminal, StormTrack, …) work on
  their own and compose into the full console
- Ultimately support the film-synchronized "watch-along" mode (see Ultimate Goal)

The preferred design direction is a **cleaned-up retro recreation**: recognizably early-1990s, but polished enough for modern use.

---

## Platform Strategy

NublarOS is a **standalone cross-platform application**, not an OS reskin. The
whole experience — the Nedryland Monitor dashboard, the island map, the
ParkNet terminal, and the future StormTrack / System Navigator — is one Kotlin
/ Compose Multiplatform for Desktop codebase that builds and runs identically
on **macOS, Linux, and Windows**.

- One Kotlin/JVM + Compose Desktop codebase, packaged per-OS via `jpackage`
  (`.dmg` / `.deb` / `.rpm` / AppImage / `.exe`).
- Native fullscreen ("control room mode"), native process spawning, and a
  real embedded terminal (pty4j + jediterm) all work across platforms.
- macOS is the current primary development/verification platform; Linux and
  Windows are supported by the same source and validated as environments
  become available. See [`docs/architecture.md`](docs/architecture.md).

Deep OS-level theming (installing NublarOS as a KDE/GNOME desktop theme, SDDM
login, etc.) is **out of scope** — the project deliberately keeps everything
inside the app rather than modifying the host desktop. Some early theme-install
scaffolding still exists under `scripts/` and `linux/` from before this
decision; it is legacy and not part of the current direction.

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

## Signature Components

Each signature component has its own detailed design & research notes under
[`docs/`](docs/). Summaries:

- **Nedryland Monitor** — the custom system dashboard that maps real telemetry
  onto park-operations terminology (CPU → Main Grid Load, failed services →
  Glitches, etc.), with sector status colors and a later interactive island
  map. Built as a working Compose Desktop app with live `oshi` metrics.
  → [`docs/nedryland-monitor.md`](docs/nedryland-monitor.md)

- **StormTrack** — a storm-analysis module recreating the film's SGI hurricane
  display: island overview, atmospheric model, and infrastructure-impact
  views driven by a loadable fictional simulation, with alerts feeding the
  Nedryland Monitor. Placeholder module today.
  → [`docs/stormtrack.md`](docs/stormtrack.md)

- **System Navigator** (`nublar-fsn`) — a spatial, low-poly 3D file browser
  inspired by SGI `fsn`, with a city/column/map metaphor, read-only-first
  safety rules, and full keyboard navigation. Placeholder module today.
  → [`docs/system-navigator.md`](docs/system-navigator.md)

- **Command Interface** — the modular command + scripted-scenario engine
  behind ParkNet Terminal (registration, history, sequencing, fake errors),
  running on the already-built pty4j/jediterm embedded terminal.
  → [`docs/command-interface.md`](docs/command-interface.md)

---


## Project Phases

The roadmap for the standalone Compose app, building toward the
film-synchronized watch-along [Ultimate Goal](#ultimate-goal). Status reflects
what's in the repo today.

### Phase 0 — Foundation · **done**

- Multi-module Gradle / Kotlin scaffold and the Compose Multiplatform stack
  decision ([`docs/architecture.md`](docs/architecture.md)).
- `design-system` module: palette, typography (incl. bundled title font), and
  the beveled System-7/SGI-style UI kit.

### Phase 1 — Core screens & data · **in progress**

- **Nedryland Monitor** dashboard with live cross-platform metrics (`oshi`)
  and a native-fullscreen "control room mode".
- **Control Room / Plan View** screen (recreated film screen).
- **Island map**: island artwork + toggleable layers (paddocks, facilities,
  dinosaurs, vehicles, staff), right-drag panning, hover tooltips, and
  icon-disc markers. Map data is JSON-backed under
  `dashboard/src/main/resources/data/isla-nublar/` (paddocks, facilities),
  with a standalone editor (`./gradlew :dashboard:runMapPreview`) that
  supports selection, vertex editing, and Copy-JSON round-tripping.
- **ParkNet Terminal**: embedded PTY (pty4j + jediterm).

Remaining: sounds, more recreated screens, richer metric coverage.

### Phase 2 — Full console assembly · **planned**

Bring the individual screens together into one navigable console: a
shell/launcher that switches subsystems, the park-status top strip and
incident log as in-app UI, control-room fullscreen mode, consistent
windowing/notifications/audio, and per-OS packaging via `jpackage`.

### Phase 3 — StormTrack · **planned**

The storm-simulation module — island overview, atmospheric model, and
infrastructure-impact views from a loadable fictional simulation, feeding
alerts into the Nedryland Monitor. See [`docs/stormtrack.md`](docs/stormtrack.md).

### Phase 4 — System Navigator · **planned**

The spatial, low-poly file browser (`nublar-fsn`), read-only-first. See
[`docs/system-navigator.md`](docs/system-navigator.md).

### Phase 5 — Command interface & scenarios · **planned**

The modular command + scripted-scenario engine running on the embedded
terminal (command registration, history, sequencing, staged "glitches"). See
[`docs/command-interface.md`](docs/command-interface.md). This is also the
event-driver groundwork for the watch-along timeline.

### Phase 6 — Watch-along sync · **planned** · *the ultimate goal*

The film-synchronized event timeline that drives every subsystem's state in
time with the movie, plus the sync tooling (manual transport, per-platform
playback integration, drift calibration). See
[Ultimate Goal](#ultimate-goal).

### Phase 7 — Ambient details · **optional**

Immersive extras, off by default: startup sequence, park intercom / warning
tones, idle screen, faux operational messages, optional CRT effects
(scanlines / bloom / curvature), and the "you didn't say the magic word"
easter egg.

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

A running list of mood/research references lives in
[`docs/inspirations.md`](docs/inspirations.md).

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

The `jurassicsystems.com` project (MIT-licensed) is a useful code reference
for the fictional command interface and scripted scenarios — command
registration, history, delayed output, event timing, fake errors, etc. The
suggested NublarOS command/scenario engine structure and reference notes have
moved to [`docs/command-interface.md`](docs/command-interface.md).

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

### Task 7: Choose the Dashboard Technology — **DONE**

Qt/QML, GTK, Tauri, Eww, Conky, and Compose Multiplatform were evaluated on
desktop integration, performance, packaging, animation, system-metric access,
cross-platform potential, and maintenance.

**Decision: Kotlin / Compose Multiplatform for Desktop** — one language and UI
toolkit across the dashboard, terminal, and future StormTrack / System
Navigator, with native fullscreen, native process spawning, and a real
embedded terminal (pty4j + jediterm). The full rationale is in
[`docs/architecture.md`](docs/architecture.md).

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
