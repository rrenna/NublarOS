#!/usr/bin/env bash
# Removes files installed by install.sh. Does not restore prior theme state
# (run restore-theme.sh for that).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib.sh
source "$SCRIPT_DIR/lib.sh"

if [ ! -f "$NUBLAR_STATE_FILE" ]; then
  nublar_log "No installed-files record found ($NUBLAR_STATE_FILE) — nothing to uninstall."
  exit 0
fi

nublar_log "Removing NublarOS-installed files..."
while IFS= read -r path; do
  [ -z "$path" ] && continue
  if [ -e "$path" ]; then
    rm -rf "$path"
    nublar_log "Removed $path"
  fi
done < "$NUBLAR_STATE_FILE"

rm -f "$NUBLAR_STATE_FILE"

SHELL_RC="$HOME/.zshrc"
[ "$(basename "${SHELL:-}")" = "bash" ] && SHELL_RC="$HOME/.bashrc"
MARKER="# NublarOS terminal integration"
if [ -f "$SHELL_RC" ] && grep -qF "$MARKER" "$SHELL_RC"; then
  # Remove the marker line and the line immediately after it.
  sed -i.nublar-bak "/$MARKER/,+1d" "$SHELL_RC"
  nublar_log "Removed NublarOS sourcing lines from $SHELL_RC (backup at ${SHELL_RC}.nublar-bak)."
fi

nublar_log "Uninstall complete. Run scripts/restore-theme.sh to restore your previous theme settings."
