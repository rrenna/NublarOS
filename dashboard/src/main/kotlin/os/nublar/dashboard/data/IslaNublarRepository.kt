package os.nublar.dashboard.data

import kotlinx.serialization.Serializable
import os.nublar.dashboard.ui.map.DinosaurMarker
import os.nublar.dashboard.ui.map.FacilityMarker
import os.nublar.dashboard.ui.map.FenceSegment
import os.nublar.dashboard.ui.map.PaddockCollection
import os.nublar.dashboard.ui.map.PaddockRecipe
import os.nublar.dashboard.ui.map.StaffMarker
import os.nublar.dashboard.ui.map.VehicleMarker
import os.nublar.dashboard.ui.map.loadCollection
import os.nublar.dashboard.ui.map.loadDinosaurs
import os.nublar.dashboard.ui.map.loadFacilities
import os.nublar.dashboard.ui.map.loadFences
import os.nublar.dashboard.ui.map.loadPaddockCollection
import os.nublar.dashboard.ui.map.loadPaddockRecipes
import os.nublar.dashboard.ui.map.loadStaff
import os.nublar.dashboard.ui.map.loadVehicles

/** One line of the control room's system/glitches log. */
@Serializable
data class LogEntry(val text: String, val status: String = "CLEAR")

@Serializable
data class GlitchesLogCollection(val entries: List<LogEntry> = emptyList())

/**
 * Access point for Isla Nublar park data (paddocks, facilities, vehicles,
 * tracked animals, staff, fences, and the control room log). Screens never
 * load data themselves: a screen's ViewModel asks a repository, and the
 * repository decides where the models come from — bundled JSON today, live
 * simulation feeds later (the watch-along mode will swap in a source that
 * replays film events).
 */
interface IslaNublarRepository {
    fun paddocks(): PaddockCollection
    fun facilities(): List<FacilityMarker>
    fun vehicles(): List<VehicleMarker>
    fun dinosaurs(): List<DinosaurMarker>
    fun staff(): List<StaffMarker>
    fun fences(): List<FenceSegment>
    fun paddockRecipes(): List<PaddockRecipe>
    fun glitchesLog(): List<LogEntry>

    /** The tour-car fleet's starting state. Must be non-empty. */
    fun tourCars(): List<TourCar>
}

/**
 * The default repository: every model type is read from the JSON files
 * bundled on the classpath under data/isla-nublar/.
 */
class BundledIslaNublarRepository : IslaNublarRepository {
    override fun paddocks(): PaddockCollection = loadPaddockCollection()
    override fun facilities(): List<FacilityMarker> = loadFacilities()
    override fun vehicles(): List<VehicleMarker> = loadVehicles()
    override fun dinosaurs(): List<DinosaurMarker> = loadDinosaurs()
    override fun staff(): List<StaffMarker> = loadStaff()
    override fun fences(): List<FenceSegment> = loadFences()
    override fun paddockRecipes(): List<PaddockRecipe> = loadPaddockRecipes()

    override fun glitchesLog(): List<LogEntry> =
        loadCollection<GlitchesLogCollection>("glitches-log.json")?.entries ?: emptyList()

    // In-code defaults today; becomes a JSON file when the fleet needs authoring.
    override fun tourCars(): List<TourCar> = DEFAULT_TOUR_CARS
}
