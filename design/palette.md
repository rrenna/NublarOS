# NublarOS Core Palette

Retro-industrial park-control palette. Gray and blue-gray dominate; bright
accents are reserved for status signaling and never cover large areas.

## Semantic Colors

| Role | Name | Hex | Usage |
|---|---|---|---|
| Base surface | Monitor gray | `#737A83` | Panel backgrounds, chrome |
| Outer frame | Dark frame | `#303942` | Window borders, bezels |
| Inset surface | Inset panel | `#515C68` | Recessed panels, fields |
| Display background | Screen black | `#07100D` | Terminal / monitor backgrounds |
| Status: normal | Status green | `#54D875` | Healthy / online state |
| Status: critical | Warning red | `#E55454` | Failure, disconnect, alert |
| Accent: informational | Map blue | `#397FA4` | Maps, links, informational chrome |
| Status: secondary normal | Paddock green | `#3F8F58` | Secondary "normal" indicator, map fills |
| Text: high-contrast | Label cream | `#E2E0BF` | Primary text on dark surfaces |
| Status: caution | Highlight yellow | `#D5CD58` | Moderate load / attention |

These are starting values — verify contrast (WCAG AA, 4.5:1 for body text)
once applied to real UI surfaces, and adjust before Phase 3 lock-in.

## Usage Rules

- Gray and blue-gray dominate the overall surface area.
- **Green** = normal operation.
- **Yellow** = attention / moderate load.
- **Red** = failure, disconnection, or critical alert.
- **Cream** = high-contrast labels only, not large fills.
- Pure white is rare — reserve for the sharpest highlights only.
- Bright accents (green/red/yellow) never cover large areas; they mark
  status, not decorate surfaces.

## Contrast Notes (to verify)

| Foreground | Background | Target | Status |
|---|---|---|---|
| Label cream `#E2E0BF` | Screen black `#07100D` | ≥ 7:1 (AAA, terminal text) | TODO: measure |
| Label cream `#E2E0BF` | Dark frame `#303942` | ≥ 4.5:1 | TODO: measure |
| Status green `#54D875` | Screen black `#07100D` | ≥ 4.5:1 | TODO: measure |
| Warning red `#E55454` | Screen black `#07100D` | ≥ 4.5:1 | TODO: measure |
| Highlight yellow `#D5CD58` | Dark frame `#303942` | ≥ 4.5:1 | TODO: measure |

## Light / Dark Variants

NublarOS is dark-first (control-room aesthetic); there is no "light mode" in
the traditional sense. If a lighter macOS-friendly variant is needed later,
it should lighten the gray/blue-gray scale (Monitor gray → a lighter neutral)
while keeping status colors and Screen black/Label cream fixed, so terminal
and dashboard displays stay legible.
