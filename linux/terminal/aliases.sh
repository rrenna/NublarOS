#!/usr/bin/env bash
# ParkNet Terminal aliases. Sourced by .zshrc/.bashrc via install.sh.
# Real tool -> NublarOS-themed alias. See docs/naming.md.

alias park-status='fastfetch'
alias sys-load='btop'
alias paddocks='ls'
alias access='cd'
alias glitches='journalctl -p warning'
alias parknet='ping'
alias incidents='journalctl -p err'
alias comms='ip addr'
alias grid-status='systemctl --failed'

# Show the ParkNet Terminal login banner on interactive shell start.
if [ -f "$HOME/.config/nublaros/motd.sh" ]; then
  bash "$HOME/.config/nublaros/motd.sh"
fi

# Apply the ParkNet Terminal prompt.
if [ -f "$HOME/.config/nublaros/prompt.sh" ]; then
  source "$HOME/.config/nublaros/prompt.sh"
fi
