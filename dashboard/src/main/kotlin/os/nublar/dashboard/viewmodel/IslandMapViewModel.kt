package os.nublar.dashboard.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import os.nublar.dashboard.data.BundledIslaNublarRepository
import os.nublar.dashboard.data.IslaNublarRepository
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
    val paddocks: List<PaddockShape> = repository.paddocks().paddocks
    val facilities: List<FacilityMarker> = repository.facilities()
    val dinosaurs: List<DinosaurMarker> = repository.dinosaurs()
    val vehicles: List<VehicleMarker> = repository.vehicles()
    val staff: List<StaffMarker> = repository.staff()

    /** Currently selected paddock (highlighted on the map). */
    var selectedPaddockId: String? by mutableStateOf(null)
        private set

    /** The id of the paddock whose name matches [name] (case-insensitive), or null. */
    fun paddockIdForName(name: String): String? =
        paddocks.firstOrNull { it.label.equals(name, ignoreCase = true) }?.id

    fun selectPaddock(id: String?) {
        selectedPaddockId = id
    }
}
