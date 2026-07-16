# Command Interface — Design & Research Notes

Notes for the fictional command/scenario engine behind ParkNet Terminal.
Summarized in the [README](../README.md#signature-components). The embedded
terminal itself (pty4j + jediterm) already exists in the `command-interface/`
module; this covers the higher-level command + scenario layer that will run
on top of it.

---

## JurassicSystems.com code reference

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
