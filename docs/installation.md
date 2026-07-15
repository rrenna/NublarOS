# Installation

## Requirements

- Linux with KDE Plasma or GNOME (primary target: Garuda Linux)
- `bash`, `git`
- Optional: `fastfetch`, `btop` for the full terminal experience

macOS support is planned as a lighter secondary track (wallpaper pack,
terminal profile, menu-bar dashboard) — see `macos/`.

## Quick Install (Minimum Viable Theme)

```bash
git clone <repo-url> nublaros
cd nublaros
./scripts/install.sh
```

By default `install.sh`:

1. Detects your desktop environment (`$XDG_CURRENT_DESKTOP`)
2. Backs up your current theme/terminal config (see
   [safety-and-restore.md](safety-and-restore.md))
3. Installs user-level assets only: wallpaper, color scheme, fonts, terminal
   profile, shell prompt, cursor theme, sounds
4. Leaves system-wide chrome (window decorations, SDDM/GDM, Plasma
   style/global theme) untouched unless you pass `--full`

## Full Install (Window and Shell Skinning)

```bash
./scripts/install.sh --full
```

Adds window decoration, Plasma style/global theme (KDE) or GTK+Shell theme
(GNOME), panel/launcher skin, notification skin. Still excludes login-screen
and lock-screen changes unless you also pass `--login-screen`.

## Uninstall

```bash
./scripts/uninstall.sh
```

Removes NublarOS-installed files. Run `./scripts/restore-theme.sh`
afterward to bring back your pre-install theme/terminal settings.

## Verifying an Install

After installing:

- Check the top bar shows NublarOS labels (`SECTOR STATUS`, `PARKNET`, etc.)
- Open a new terminal and confirm the ParkNet Terminal banner and prompt
- Confirm wallpaper and cursor theme applied
- See `screenshots/` for what a correct install should look like

## Known Gaps (First Milestone)

Per the README's Definition of the First Milestone, the following are
intentionally **not** part of a minimal install: full custom dashboard,
login-screen theme, complete icon set, macOS version, animated maps, sound
effects, CRT effects.
