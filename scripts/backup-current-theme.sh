#!/usr/bin/env bash
# Snapshot the current desktop theme and terminal config before NublarOS
# touches anything. Safe to run multiple times; each run creates a new
# timestamped snapshot. See docs/safety-and-restore.md.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$SCRIPT_DIR/lib.sh"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
SNAPSHOT_DIR="$NUBLAR_BACKUP_DIR/$TIMESTAMP"
mkdir -p "$SNAPSHOT_DIR"

DE="$(nublar_detect_de)"
nublar_log "Backing up current theme (desktop: $DE) to $SNAPSHOT_DIR"

{
  echo "timestamp=$TIMESTAMP"
  echo "XDG_CURRENT_DESKTOP=$DE"
  echo "XDG_SESSION_TYPE=${XDG_SESSION_TYPE:-UNKNOWN}"
  echo "SHELL=${SHELL:-UNKNOWN}"
} > "$SNAPSHOT_DIR/environment.txt"

case "$DE" in
  *KDE*)
    mkdir -p "$SNAPSHOT_DIR/kde"
    for f in "$HOME/.config/kdeglobals" "$HOME/.config/plasmarc" "$HOME/.config/kwinrc"; do
      [ -f "$f" ] && cp "$f" "$SNAPSHOT_DIR/kde/" || true
    done
    ;;
  *GNOME*)
    mkdir -p "$SNAPSHOT_DIR/gnome"
    if command -v dconf >/dev/null 2>&1; then
      dconf dump /org/gnome/desktop/interface/ > "$SNAPSHOT_DIR/gnome/interface.dconf" || true
      dconf dump /org/gnome/shell/extensions/ > "$SNAPSHOT_DIR/gnome/extensions.dconf" || true
    fi
    ;;
  *)
    nublar_warn "Unrecognized desktop environment '$DE' — only generic terminal/shell config will be backed up."
    ;;
esac

# Terminal / shell config (best-effort, desktop-agnostic)
mkdir -p "$SNAPSHOT_DIR/terminal"
for f in "$HOME/.zshrc" "$HOME/.bashrc" "$HOME/.config/konsolerc" "$HOME/.bash_aliases"; do
  [ -f "$f" ] && cp "$f" "$SNAPSHOT_DIR/terminal/" || true
done

# Reference list of currently installed theming-relevant packages (best-effort)
if command -v pacman >/dev/null 2>&1; then
  pacman -Qe > "$SNAPSHOT_DIR/packages.txt" || true
elif command -v dpkg >/dev/null 2>&1; then
  dpkg -l > "$SNAPSHOT_DIR/packages.txt" || true
fi

nublar_log "Backup complete: $SNAPSHOT_DIR"
