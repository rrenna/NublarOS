package os.nublar.dashboard.viewmodel

import os.nublar.dashboard.data.BundledIslaNublarRepository
import os.nublar.dashboard.data.IslaNublarRepository
import os.nublar.dashboard.data.LogEntry

/**
 * ViewModel for the Control Room / Plan View screen. Serves the system-log
 * entries from the repository (static film-flavor text today; a live event
 * feed — sensor trips, fence faults, Nedry's mischief — will stream in here
 * for watch-along mode).
 */
class ControlRoomViewModel(
    repository: IslaNublarRepository = BundledIslaNublarRepository(),
) {
    val glitchesLog: List<LogEntry> = repository.glitchesLog()
}
