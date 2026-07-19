# Inspirations

A running list of references that inform NublarOS's look, feel, and behavior —
the control-room aesthetic, park-operations UI language, and film-accurate
details. These are research/mood references only; distributed NublarOS assets
remain original (see "Legal and Asset Guidelines" in the [README](../README.md)).

## Images

- **[Stills from InGen Harvest Operation (1997)](https://www.reddit.com/r/JurassicPark/comments/1uyx5bx/more_stills_from_ingen_harvest_operation_1997/)**
  — production stills reference for InGen field/operations hardware and screen
  chrome; informs the control panel and button styling.

## Video

- **[Jurassic Park control room / computer systems reference](https://www.youtube.com/watch?v=-OhLl0tdLVA)**
  — video reference for the control-room screens and interactions.
  <!-- TODO: replace with the exact video title + channel once confirmed. -->

- **[Jurassic Park Systems CCTV](https://www.youtube.com/watch?v=Xl4bDROhwd0&t=345s)**
  — recreation of the park's CCTV/security-camera system; reference for the
  security screen's camera feeds and surveillance UI (link starts at 5:45).
  **Used as footage** in the dashboard's SECURITY CAM subwindow: the app plays
  frames the user extracts locally from this clip (see
  `dashboard/.../ui/CctvView.kt`). The footage is **not** bundled or
  redistributed with NublarOS — it stays on the user's machine under
  `~/.nublaros/cctv/`. Full credit to the video's creator.
  <!-- TODO: add the exact YouTube channel/creator name here to credit them. -->


## Articles

- **Fabien Sanglard — "Jurassic Park computers in excruciating detail"**
  (<https://fabiensanglard.net/jurrasic_park_computers/index.html>) — the SGI
  workstations, pre-generated control-room animations, the `fsn` File System
  Navigator, and Macintosh System 7 / SGI IRIX visual characteristics.

## Code references

- **jurassicsystems.com** (<https://github.com/tojrobinson/jurassicsystems.com>,
  MIT) — patterns for a fictional command interface and scripted scenarios; see
  [`docs/command-interface.md`](command-interface.md).

<!-- Add new inspirations here: link + a one-line note on what it informs. -->
