#!/usr/bin/env bash
# ParkNet Terminal shell prompt. Sourced by .zshrc/.bashrc, or used as a
# reference for a starship.toml preset (see prompt-starship.toml).
#
# Format: "visitor.center %" per README Terminal Design.

if [ -n "${ZSH_VERSION:-}" ]; then
  PROMPT='%F{green}visitor.center%f %# '
elif [ -n "${BASH_VERSION:-}" ]; then
  PS1='\[\e[32m\]visitor.center\[\e[0m\] % '
fi
