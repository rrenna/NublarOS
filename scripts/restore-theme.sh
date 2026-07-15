#!/usr/bin/env bash
# Restore a theme/terminal backup created by backup-current-theme.sh.
# Usage: ./scripts/restore-theme.sh [timestamp]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$SCRIPT_DIR/lib.sh"

TIMESTAMP="${1:-$(nublar_latest_backup)}"

if [ -z "$TIMESTAMP" ]; then
  nublar_log "No backups found in $NUBLAR_BACKUP_DIR — nothing to restore."
  exit 1
fi

SNAPSHOT_DIR="$NUBLAR_BACKUP_DIR/$TIMESTAMP"
if [ ! -d "$SNAPSHOT_DIR" ]; then
  nublar_warn "Backup '$TIMESTAMP' not found in $NUBLAR_BACKUP_DIR."
  exit 1
fi

nublar_log "Restoring backup: $SNAPSHOT_DIR"

if [ -d "$SNAPSHOT_DIR/kde" ]; then
  for f in "$SNAPSHOT_DIR"/kde/*; do
    [ -f "$f" ] && cp "$f" "$HOME/.config/$(basename "$f")"
  done
  nublar_log "Restored KDE config files."
fi

if [ -f "$SNAPSHOT_DIR/gnome/interface.dconf" ] && command -v dconf >/dev/null 2>&1; then
  dconf load /org/gnome/desktop/interface/ < "$SNAPSHOT_DIR/gnome/interface.dconf"
  nublar_log "Restored GNOME interface settings."
fi
if [ -f "$SNAPSHOT_DIR/gnome/extensions.dconf" ] && command -v dconf >/dev/null 2>&1; then
  dconf load /org/gnome/shell/extensions/ < "$SNAPSHOT_DIR/gnome/extensions.dconf"
  nublar_log "Restored GNOME extension settings."
fi

if [ -d "$SNAPSHOT_DIR/terminal" ]; then
  for f in "$SNAPSHOT_DIR"/terminal/*; do
    [ -f "$f" ] || continue
    dest="$HOME/.$(basename "$f")"
    [ "$(basename "$f")" = "konsolerc" ] && dest="$HOME/.config/konsolerc"
    cp "$f" "$dest"
    nublar_log "Restored $dest"
  done
fi

nublar_log "Restore complete from snapshot $TIMESTAMP."
nublar_log "Note: package list at $SNAPSHOT_DIR/packages.txt is informational only and is not reinstalled automatically."
