# System Navigator — Design & Research Notes

Detailed notes for the spatial file-browser signature component (internal name
`nublar-fsn`). Summarized in the
[README](../README.md#signature-components). Rendering direction lives in
[`docs/architecture.md`](architecture.md#system-navigator-rendering-spike-pending);
the `system-navigator/` module is currently a placeholder.

---

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

## Goals

The navigator should:

- Provide a spatial overview of folders and files
- Make directory depth visually understandable
- Feel like an experimental SGI workstation tool
- Work as a launcher and exploration interface
- Preserve access to ordinary file operations
- Avoid becoming the only way to manage files
- Support keyboard navigation
- Remain functional with large directories

## Core Metaphor

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

## Visual Style

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

## File Representation

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

## Size and Metadata Mapping

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

## Navigation

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

## Selection and Inspection Panel

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

## Layout Modes

### City Mode

Directories and files appear as a navigable low-poly city.

Best for:

- Visual browsing
- Demonstrations
- Full-screen use
- Recreating the SGI-inspired experience

### Column Mode

Directories appear as connected platforms arranged by depth.

Best for:

- Understanding hierarchy
- Navigating deeply nested projects
- Preserving spatial context

### Map Mode

Directories are arranged as a top-down facility map.

Best for:

- Large directory trees
- Fast overview
- Integration with the broader NublarOS map aesthetic

The first milestone should implement only City Mode.

## Performance Requirements

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

## Safety Requirements

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

## Technology Options

Potential implementations:

### Qt/QML

Advantages:

- Strong Linux desktop integration
- GPU-accelerated scene graph
- Native filesystem APIs
- Good keyboard and window management
- Fits a KDE-first implementation

### Tauri with WebGL

Advantages:

- Fast UI iteration
- Three.js or PixiJS ecosystem
- Potential cross-platform reuse
- Easier visual prototyping

### Godot

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

(Note: with the project-wide Compose Multiplatform decision, the current
default is Compose + LWJGL — see
[`docs/architecture.md`](architecture.md#system-navigator-rendering-spike-pending).)

## First Prototype Scope

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

## Later Features

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
