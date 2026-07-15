# Safety and Restore

NublarOS makes visible changes to your desktop environment. Every change
must be reversible.

## Guarantees

- No component requires root access unless it modifies login-manager
  (SDDM/GDM) configuration, which is always opt-in and clearly warned about.
- Nothing is installed or overwritten without first backing up the existing
  configuration.
- User-created files (wallpapers, custom keybindings, existing terminal
  configs not managed by NublarOS) are never overwritten silently.

## Workflow

```text
scripts/backup-current-theme.sh   # snapshot current theme/terminal state
scripts/install.sh                # apply NublarOS (user-level by default)
scripts/uninstall.sh              # remove NublarOS-installed files
scripts/restore-theme.sh          # restore the pre-install snapshot
```

Run `backup-current-theme.sh` before `install.sh` — `install.sh` calls it
automatically if no backup exists, but running it explicitly first lets you
inspect what will be captured.

## What Gets Backed Up

- Desktop environment detection (`$XDG_CURRENT_DESKTOP`, `$XDG_SESSION_TYPE`)
- Current GTK/Plasma theme, icon theme, cursor theme settings
- Current terminal profile/config file(s)
- A list of currently installed packages relevant to theming (for reference,
  not automatic reinstall)

Backups are written to `~/.local/share/nublaros/backup/<timestamp>/` and are
never deleted automatically.

## Restore

`restore-theme.sh` restores the most recent backup by default, or accepts a
specific timestamp:

```bash
./scripts/restore-theme.sh                # restore latest backup
./scripts/restore-theme.sh 20260715-1200   # restore a specific snapshot
```

## Login Manager and System-Wide Changes

SDDM/GDM theme changes and any change requiring `sudo` are treated as a
separate, explicitly opt-in step — never bundled into the default
`install.sh` run. The installer prints a warning and requires confirmation
before touching login-manager configuration.

## Reporting Issues

If `restore-theme.sh` fails to fully restore your previous setup, the raw
backup files remain in `~/.local/share/nublaros/backup/` for manual
recovery — nothing is deleted until you remove it yourself.
