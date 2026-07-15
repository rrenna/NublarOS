# Naming Conventions

Canonical fictional-terminology mapping lives in
[`design/component-reference.md`](../design/component-reference.md) — treat
that file as the single source of truth for UI copy, config keys, and script
labels. This file covers naming conventions for the codebase itself.

## Project Names

- **NublarOS** — the project / theme as a whole.
- **Nublar Shell** — desktop shell layer.
- **ParkNet Terminal** — terminal profile.
- **Nedryland Monitor** — system dashboard app (`dashboard/`).
- **Paddock Control** — application launcher.
- **White Rabbit** — lock screen.
- **StormTrack** — storm simulation module (`stormtrack/`).
- **System Navigator** — spatial file browser, package name `nublar-fsn`
  (`system-navigator/`).

## Code-Level Conventions

- Directory names: lowercase, kebab-case (`system-navigator`, not
  `SystemNavigator`).
- Package/crate/module identifiers use the internal working name
  (`nublar-fsn`), not the user-facing display name ("System Navigator").
- Config keys use `snake_case` (see the `theme:`, `labels:`, `dashboard:`
  example in the root README).
- User-facing strings (window titles, button labels, dashboard text) use the
  NublarOS fictional terminology from `component-reference.md`, always
  UPPERCASE for headers/labels per the typography spec.
- Shell aliases use the fictional names as the alias, real tool as the
  target (e.g. `alias park-status='fastfetch'`) — see
  `linux/terminal/aliases.sh`.

## Adding a New Mapping

When introducing a new real-world concept that needs a NublarOS label:

1. Add the mapping to the relevant table in `design/component-reference.md`.
2. Keep it in-universe: park operations, not generic sci-fi ("Glitches" not
   "Error Log Viewer").
3. Avoid names that collide with real Jurassic Park trademarked terms beyond
   what's already established (see [Legal and Asset
   Guidelines](../README.md#legal-and-asset-guidelines) in the root README).
