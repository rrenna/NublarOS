#!/usr/bin/env bash
# NublarOS installer. User-level by default and reversible.
# Usage: ./scripts/install.sh [--full] [--login-screen]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
# shellcheck source=lib.sh
source "$SCRIPT_DIR/lib.sh"

FULL=0
LOGIN_SCREEN=0
for arg in "$@"; do
  case "$arg" in
    --full) FULL=1 ;;
    --login-screen) LOGIN_SCREEN=1 ;;
    *) nublar_warn "Unknown option: $arg" ;;
  esac
done

DE="$(nublar_detect_de)"
nublar_log "Detected desktop environment: $DE"

if [ "$(uname -s)" != "Linux" ]; then
  nublar_warn "This installer targets Linux (KDE/GNOME). Detected $(uname -s)."
  nublar_warn "For macOS, see macos/ once that track is implemented."
fi

if [ -z "$(nublar_latest_backup)" ]; then
  nublar_log "No existing backup found — running backup-current-theme.sh first."
  "$SCRIPT_DIR/backup-current-theme.sh"
fi

mkdir -p "$NUBLAR_DATA_DIR"

install_user_level() {
  nublar_log "Installing user-level assets (wallpaper, terminal, fonts, cursors, sounds)..."

  mkdir -p "$HOME/.local/share/nublaros/wallpapers"
  if compgen -G "$REPO_ROOT/assets/wallpapers/*" > /dev/null; then
    cp -r "$REPO_ROOT"/assets/wallpapers/* "$HOME/.local/share/nublaros/wallpapers/"
    nublar_record_installed "$HOME/.local/share/nublaros/wallpapers"
  else
    nublar_warn "No wallpapers found in assets/wallpapers/ yet — skipping."
  fi

  if [ -f "$REPO_ROOT/linux/terminal/aliases.sh" ]; then
    mkdir -p "$HOME/.config/nublaros"
    cp "$REPO_ROOT/linux/terminal/aliases.sh" "$HOME/.config/nublaros/aliases.sh"
    cp "$REPO_ROOT/linux/terminal/motd.sh" "$HOME/.config/nublaros/motd.sh"
    cp "$REPO_ROOT/linux/terminal/prompt.sh" "$HOME/.config/nublaros/prompt.sh"
    nublar_record_installed "$HOME/.config/nublaros/aliases.sh"
    nublar_record_installed "$HOME/.config/nublaros/motd.sh"
    nublar_record_installed "$HOME/.config/nublaros/prompt.sh"

    SHELL_RC="$HOME/.zshrc"
    [ "$(basename "${SHELL:-}")" = "bash" ] && SHELL_RC="$HOME/.bashrc"
    MARKER="# NublarOS terminal integration"
    if [ -f "$SHELL_RC" ] && ! grep -qF "$MARKER" "$SHELL_RC"; then
      {
        echo ""
        echo "$MARKER"
        echo "[ -f \"\$HOME/.config/nublaros/aliases.sh\" ] && source \"\$HOME/.config/nublaros/aliases.sh\""
      } >> "$SHELL_RC"
      nublar_log "Appended NublarOS sourcing line to $SHELL_RC (guarded by marker comment, easy to remove)."
    fi
  fi

  nublar_log "User-level install complete."
}

install_full() {
  nublar_log "Installing full desktop skin (window decoration, panel, launcher)..."
  nublar_warn "Full desktop skinning is not yet implemented (Phase 4). Nothing to do."
}

install_login_screen() {
  nublar_warn "Login-screen (SDDM/GDM) changes require root and are NOT applied automatically."
  read -r -p "Proceed with login-screen theming? [y/N] " reply
  if [[ "$reply" =~ ^[Yy]$ ]]; then
    nublar_warn "Login-screen theming is not yet implemented (Phase 4). Nothing to do."
  else
    nublar_log "Skipping login-screen theming."
  fi
}

install_user_level
[ "$FULL" -eq 1 ] && install_full
[ "$LOGIN_SCREEN" -eq 1 ] && install_login_screen

nublar_log "Install finished. Run scripts/restore-theme.sh to revert at any time."
