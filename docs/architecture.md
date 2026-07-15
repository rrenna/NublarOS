# Architecture Decisions

## Application Stack: Kotlin Multiplatform / Compose Multiplatform for Desktop

**Decision (supersedes the earlier Tauri recommendation below): NublarOS's
custom applications — Nedryland Monitor, StormTrack, System Navigator, and
the command-interface terminal — are built as Kotlin/JVM apps using Compose
Multiplatform for Desktop.**

Options evaluated for the dashboard: Qt/QML, GTK, Tauri, Eww, Conky, Compose
Multiplatform (KMP).

| Option | Desktop integration | Performance | Packaging | Animation | Metric access | Cross-platform | Maintenance |
|---|---|---|---|---|---|---|---|
| Qt/QML | Excellent (KDE-native) | Excellent | Moderate (Qt deps) | Excellent (Scene Graph) | Native (QProcess/DBus) | Good (Qt runs on macOS too) | Moderate — QML has a learning curve |
| GTK | Good on GNOME, weaker on KDE | Good | Good (widely packaged) | Fair | Native | Poor on macOS | Moderate |
| Tauri | Good (via web view) | Good | Small binaries, easy | Excellent (CSS/WebGL) | Needs Rust/Node bridges | Excellent | Low — web skills transfer |
| Eww | Minimal (widget-only) | Excellent (lightweight) | Trivial | Limited | Shell-script driven | Linux-only | Low, but limited ceiling |
| Conky | None (overlay only) | Excellent | Trivial | None | Native | Linux-only | Very low, prototype-only |
| Compose Multiplatform (KMP) | Good (Swing/AWT interop, native fullscreen) | Good (Skia/Skiko GPU-backed) | Moderate (jpackage, JVM bundled) | Good (Compose Canvas + Skia) | Native (`ProcessBuilder`, `/proc`, `sysinfo`-equivalents via JNA if needed) | Excellent (Linux/macOS/Windows from one codebase) | Low — single language across all four apps |

### Recommendation: **Compose Multiplatform for Desktop (Kotlin/JVM)**

Reasoning:

- One language and one UI toolkit across all four custom apps — Nedryland
  Monitor, StormTrack, System Navigator, and the terminal/command-interface
  — instead of splitting between Rust+web and native shells. Kotlin is the
  chosen implementation language project-wide going forward.
- Compose Desktop windows are backed by AWT/Swing, so **native macOS
  fullscreen** (`WindowPlacement.Fullscreen`, real Spaces-based fullscreen,
  not a borderless maximized window) works out of the box — required for
  StormTrack's ambient full-screen mode and Nedryland Monitor's full-screen
  control-room mode.
- Process spawning (`ProcessBuilder`) is plain JVM — needed for System
  Navigator's "open terminal at location" / "open in standard file manager"
  actions and the command-interface's scripted scenarios.
- A **real embedded terminal is achievable in-process** using JetBrains'
  own **pty4j** (spawns a genuine PTY running the user's actual shell —
  not a fake command box) plus **jediterm** (the ANSI terminal emulator
  widget that renders it — the same pair that powers IntelliJ's built-in
  terminal). Both are plain Java libraries wrapped in a Compose `SwingPanel`.
  This lets ParkNet Terminal / System Access be a first-class embedded pane
  inside the dashboard, not just an external terminal-profile theme.
- Rendering ceiling is sufficient for the project's stated visual style
  (flat-shaded, low-poly, limited palette, deliberately restrained frame
  rate) — Compose's Skia-backed Canvas handles 2D StormTrack overlays, and
  System Navigator's low-poly 3D city can use a JVM OpenGL binding (LWJGL)
  embedded via `SwingPanel`, same interop pattern as the terminal.
- Cross-platform by construction: the same dashboard code runs on Linux and
  macOS, directly serving the macOS secondary-platform goal without a
  SwiftUI rewrite. A thin native menu-bar shim (SwiftUI `MenuBarExtra` or
  AWT `SystemTray`) can still front the same app for menu-bar-only
  interactions where a webview/Compose window isn't appropriate.
- Qt/QML remains the better choice specifically for deep KDE **system
  chrome** (window decorations, Plasma style, SDDM theme) — that is a
  separate concern from the custom apps and is handled under `linux/kde/`,
  unaffected by this decision.
- Tradeoff accepted: JVM startup time and memory footprint are heavier than
  a native Rust/Tauri binary or Qt app. Acceptable for an app meant to run
  as a persistent dashboard/ambient display rather than a frequently
  cold-started CLI tool; revisit if startup latency proves to be a real
  problem on target hardware.
- Eww and Conky remain useful only as disposable early prototypes (metric
  mapping validation) — not the long-term implementation.

### Consequences

- `dashboard/`, `stormtrack/`, `system-navigator/`, and
  `command-interface/` become Kotlin/Gradle modules in a single
  multi-module Gradle build (see `settings.gradle.kts`).
- A shared `design-system` module holds the Compose theme (colors from
  `design/palette.md`, typography from `design/typography.md`,
  reusable components: beveled panel, status indicator, chunky button) —
  imported by all four app modules.
- System metric collection: read `/proc`, `/sys`, and shell out to
  `systemctl --failed` on Linux; use the `oshi` library (cross-platform,
  pure-Java system info, no native compilation required) or `ProcessBuilder`
  + `sysctl`/`vm_stat` on macOS.
- Terminal: `pty4j` + `jediterm` for the embedded terminal pane.
- 3D rendering (System Navigator): LWJGL (OpenGL bindings) embedded via
  `SwingPanel`, or Compose Canvas-based low-poly rendering if LWJGL
  integration proves too heavy for the first prototype — decide during the
  System Navigator spike below.
- Packaging: `jpackage` (bundled with the JDK) produces native `.dmg` /
  `.deb`/`.rpm`/AppImage installers with the JVM bundled, so end users don't
  need a separate Java install.

## System Navigator Rendering (spike pending)

Task 9 originally called for comparing Qt/QML and Godot. With the KMP
decision above, the default is **Compose + LWJGL** (OpenGL embedded via
`SwingPanel`) to stay in one codebase. Godot remains a fallback if
LWJGL/Compose interop proves too fragile for the low-poly city renderer —
record the spike outcome here once run.

## StormTrack Rendering

First prototype: Compose Canvas (Skia-backed) for the island overlay and
storm field — equivalent fidelity to the originally-planned SVG+Canvas
approach, now in Kotlin. Cloud/pressure frame data loads from YAML/JSON per
the existing simulation data model in the README.

<details>
<summary>Superseded: original Tauri-based recommendation (kept for history)</summary>

Tauri was the initial recommendation because it shared a single Rust+web
stack across all three signature components and offered small binaries and
excellent cross-platform packaging. It was dropped in favor of Compose
Multiplatform once the project settled on Kotlin/JVM as the implementation
language, which gets equivalent cross-platform reach plus native macOS
fullscreen, native process spawning, and a proven embedded-terminal path
(pty4j/jediterm) in a single language across the whole app suite.

</details>
