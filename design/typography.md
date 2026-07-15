# NublarOS Typography

Three typographic roles. All fonts must be freely licensed for distribution
(no Eurostile/Microgramma/Bank Gothic originals — use open alternatives).

## 1. Interface Headers

Square, condensed, industrial sans-serif. Used for window titles, section
headers, status labels, menu categories, dashboard titles.

- Candidates: **Bebas Neue**, **Oswald**, **Michroma**, **Big Shoulders
  Display**, **Iceland** — condensed/technical sans, geometric, uppercase-friendly.
- Recommended starting pick: **Oswald** (wide weight range, excellent Linux
  packaging via Google Fonts, condensed but still legible at small sizes).

## 2. System Text

Readable bitmap-inspired or monospace font. Used for terminal output, status
logs, file paths, metrics, paddock labels, timestamps.

- Candidates: **JetBrains Mono**, **IBM Plex Mono**, **Space Mono**,
  **Departure Mono** (more bitmap-flavored).
- Recommended starting pick: **JetBrains Mono** (excellent hinting, ligature
  support optional, widely packaged, reads well at terminal sizes).

## 3. Large Display Text

Bold display face, used sparingly for hero labels (`ANIMAL PADDOCKS`,
`SYSTEM SECURED`, etc.). Not every element should use a pixel font.

- Candidates: **Big Shoulders Display Black**, **Archivo Black**, **Rajdhani
  Bold**.
- Recommended starting pick: **Archivo Black** for high-impact labels;
  reserve a pixel/bitmap face (e.g. **Press Start 2P**, used very sparingly)
  for explicitly "old CRT" moments only.

## Application

| Role | Font | Weight(s) | Case |
|---|---|---|---|
| Window titles | Oswald | 500–600 | UPPERCASE |
| Status labels / top bar | Oswald | 500 | UPPERCASE |
| Terminal / logs / metrics | JetBrains Mono | 400–500 | as-is |
| Hero display labels | Archivo Black | 900 | UPPERCASE |

All three are open-source (OFL) and available via Google Fonts, safe to
bundle and distribute with the theme package.
