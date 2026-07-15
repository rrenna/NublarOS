#!/usr/bin/env bash
# Shared helpers for NublarOS scripts. Sourced, not executed directly.

NUBLAR_DATA_DIR="${NUBLAR_DATA_DIR:-$HOME/.local/share/nublaros}"
NUBLAR_BACKUP_DIR="$NUBLAR_DATA_DIR/backup"
NUBLAR_STATE_FILE="$NUBLAR_DATA_DIR/installed-files.list"

nublar_log() {
  echo "[NUBLAROS] $*"
}

nublar_warn() {
  echo "[NUBLAROS] WARNING: $*" >&2
}

nublar_detect_de() {
  if [ -n "$XDG_CURRENT_DESKTOP" ]; then
    echo "$XDG_CURRENT_DESKTOP"
  elif [ -n "$DESKTOP_SESSION" ]; then
    echo "$DESKTOP_SESSION"
  else
    echo "UNKNOWN"
  fi
}

nublar_latest_backup() {
  ls -1 "$NUBLAR_BACKUP_DIR" 2>/dev/null | sort -r | head -n1
}

nublar_record_installed() {
  mkdir -p "$NUBLAR_DATA_DIR"
  echo "$1" >> "$NUBLAR_STATE_FILE"
}
