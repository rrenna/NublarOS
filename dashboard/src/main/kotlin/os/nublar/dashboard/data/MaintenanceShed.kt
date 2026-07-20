package os.nublar.dashboard.data

/** The Maintenance Shed's operational status. */
enum class MaintenanceShedStatus(val label: String) {
    Idle("IDLE"),
    Repairing("REPAIRING"),
    Offline("OFFLINE"),
}

/** Number of breakers in the shed's power panel. */
const val MAINTENANCE_SHED_BREAKER_COUNT: Int = 8

/**
 * The Maintenance Shed facility's current state — mirrors [EastDockState] /
 * [HelipadState] as the model backing the Maintenance Shed status panel: its
 * operational status, and which tour vehicle (if any) is currently in the bay
 * for repair. Reuses [TourCar] as the vehicle's identity, since it's always one
 * of the tracked EXP fleet.
 *
 * While [MaintenanceShedStatus.Offline], the shed's power is down and
 * [breakers] tracks a small panel of switches that must each be pushed closed
 * by hand to bring it back — an original "restore power one switch at a time"
 * panel mechanic, not any specific film's exact screen or dialogue.
 */
data class MaintenanceShedState(
    val status: MaintenanceShedStatus = MaintenanceShedStatus.Idle,
    val vehicle: TourCar? = null,
    val breakers: List<Boolean> = List(MAINTENANCE_SHED_BREAKER_COUNT) { false },
) {
    /** True once every breaker has been pushed closed. */
    val powerRestored: Boolean get() = breakers.all { it }
}
