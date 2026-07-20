package os.nublar.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import os.nublar.dashboard.data.BundledIslaNublarRepository
import os.nublar.dashboard.data.EastDockState
import os.nublar.dashboard.data.HelicopterState
import os.nublar.dashboard.data.HelipadState
import os.nublar.dashboard.data.IslaNublarRepository
import os.nublar.dashboard.data.LoadingBayState
import os.nublar.dashboard.data.MaintenanceShedState
import os.nublar.dashboard.data.MaintenanceShedStatus
import os.nublar.dashboard.data.TourCar
import os.nublar.dashboard.data.VisitorCenterState
import os.nublar.dashboard.ui.map.DinosaurMarker
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.PaddockShape
import os.nublar.dashboard.ui.map.StaffMarker
import os.nublar.dashboard.ui.map.VehicleMarker

/**
 * ViewModel for the Animal Paddocks screen (IslandMapView): supplies the map
 * layers' models from the repository. Read-only today; live updates (vehicle
 * telemetry, animal tracking) will land here as observable state.
 */
class IslandMapViewModel(
    repository: IslaNublarRepository = BundledIslaNublarRepository(),
) {
    /** Observable so the Events menu can fail (disarm) a paddock's fence live. */
    var paddocks: List<PaddockShape> by mutableStateOf(repository.paddocks().paddocks)
        private set

    val facilities: List<FacilityMarker> = repository.facilities()
    val dinosaurs: List<DinosaurMarker> = repository.dinosaurs()
    val vehicles: List<VehicleMarker> = repository.vehicles()
    val staff: List<StaffMarker> = repository.staff()

    /** Currently selected paddock (highlighted on the map). */
    var selectedPaddockId: String? by mutableStateOf(null)
        private set

    /** Currently selected facility (highlighted on the map). */
    var selectedFacilityId: String? by mutableStateOf(null)
        private set

    /** The id of the paddock whose name matches [name] (case-insensitive), or null. */
    fun paddockIdForName(name: String): String? =
        paddocks.firstOrNull { it.label.equals(name, ignoreCase = true) }?.id

    /** Paddock and facility selection are mutually exclusive — selecting one clears the other. */
    fun selectPaddock(id: String?) {
        selectedPaddockId = id
        if (id != null) selectedFacilityId = null
    }

    fun selectFacility(id: String?) {
        selectedFacilityId = id
        if (id != null) selectedPaddockId = null
    }

    /**
     * The tour-car fleet (starting state from the repository, which guarantees
     * it non-empty). Observable so the Vehicle Status panel re-renders when a
     * car's speed / headlights / responding state changes.
     */
    var tourCars: List<TourCar> by mutableStateOf(repository.tourCars())
        private set

    /** Id of the car the Vehicle Status panel tracks (switchable, e.g. by show events). */
    var trackedCarId: String by mutableStateOf(tourCars.first().id)
        private set

    fun trackCar(id: String) {
        if (tourCars.any { it.id == id }) trackedCarId = id
    }

    /** The car whose live status the Vehicle Status panel displays. */
    val trackedCar: TourCar
        get() = tourCars.firstOrNull { it.id == trackedCarId } ?: tourCars.first()

    private fun updateCar(id: String, transform: (TourCar) -> TourCar) {
        tourCars = tourCars.map { if (it.id == id) transform(it) else it }
    }

    fun setCarSpeed(id: String, mph: Int) = updateCar(id) { it.copy(speedMph = mph) }

    fun setCarHeadlights(id: String, on: Boolean) = updateCar(id) { it.copy(headlightsOn = on) }

    fun setCarResponding(id: String, responding: Boolean) = updateCar(id) { it.copy(responding = responding) }

    /**
     * The raptor pen's loading bay. Observable so the loading-bay status panel
     * (shown while the Raptor Paddock is selected) re-renders as the scripted
     * incident updates gate / loader / lock / alert.
     */
    var loadingBay: LoadingBayState by mutableStateOf(LoadingBayState())
        private set

    fun updateLoadingBay(transform: (LoadingBayState) -> LoadingBayState) {
        loadingBay = transform(loadingBay)
    }

    fun resetLoadingBay() {
        loadingBay = LoadingBayState()
    }

    /**
     * The East Dock's state. Observable so the East Dock status panel (shown
     * while the East Dock facility is selected) re-renders as a ship arrives,
     * departs, or the berth status otherwise changes.
     */
    var eastDock: EastDockState by mutableStateOf(EastDockState())
        private set

    fun updateEastDock(transform: (EastDockState) -> EastDockState) {
        eastDock = transform(eastDock)
    }

    fun resetEastDock() {
        eastDock = EastDockState()
    }

    /**
     * InGen's helicopter. Observable so the map re-renders its marker as the
     * helicopter's location/position changes (Costa Rica / in transit with live
     * coordinates / on the island).
     */
    var helicopter: HelicopterState by mutableStateOf(HelicopterState())
        private set

    fun updateHelicopter(transform: (HelicopterState) -> HelicopterState) {
        helicopter = transform(helicopter)
    }

    fun resetHelicopter() {
        helicopter = HelicopterState()
    }

    /**
     * The Helipad facility's state. Observable so the Helipad status panel
     * (shown while that facility is selected) re-renders as a helicopter lands,
     * sits on the pad, or departs.
     */
    var helipad: HelipadState by mutableStateOf(HelipadState())
        private set

    fun updateHelipad(transform: (HelipadState) -> HelipadState) {
        helipad = transform(helipad)
    }

    fun resetHelipad() {
        helipad = HelipadState()
    }

    /**
     * The Visitor Center's state. Observable so the Visitor Center status panel
     * (shown while that facility is selected) re-renders as its operational
     * status or occupancy changes.
     */
    var visitorCenter: VisitorCenterState by mutableStateOf(VisitorCenterState())
        private set

    fun updateVisitorCenter(transform: (VisitorCenterState) -> VisitorCenterState) {
        visitorCenter = transform(visitorCenter)
    }

    fun resetVisitorCenter() {
        visitorCenter = VisitorCenterState()
    }

    /**
     * The Maintenance Shed's state. Observable so the Maintenance Shed status
     * panel (shown while that facility is selected) re-renders as its
     * operational status or the vehicle in the bay changes.
     */
    var maintenanceShed: MaintenanceShedState by mutableStateOf(MaintenanceShedState())
        private set

    fun updateMaintenanceShed(transform: (MaintenanceShedState) -> MaintenanceShedState) {
        maintenanceShed = transform(maintenanceShed)
    }

    fun resetMaintenanceShed() {
        maintenanceShed = MaintenanceShedState()
    }

    /**
     * Pushes one breaker in the shed's power panel closed/open. Only has an
     * effect while the shed is Offline; once every breaker is closed the shed
     * comes back Idle automatically — power's restored.
     */
    fun toggleMaintenanceShedBreaker(index: Int) {
        val shed = maintenanceShed
        if (shed.status != MaintenanceShedStatus.Offline) return
        val breakers = shed.breakers.toMutableList().also { it[index] = !it[index] }
        val restored = breakers.all { it }
        maintenanceShed = shed.copy(
            breakers = breakers,
            status = if (restored) MaintenanceShedStatus.Idle else shed.status,
        )
    }

    /** Fails (disarms) a paddock's fence — armed -> unarmed, playing the disarm animation. */
    fun failFence(id: String) {
        paddocks = paddocks.map { if (it.id == id) it.copy(armed = false) else it }
    }

    /** Re-arms every paddock fence (resets the failures). */
    fun rearmAllFences() {
        paddocks = paddocks.map { if (it.armed) it else it.copy(armed = true) }
    }
}
