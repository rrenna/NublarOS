package os.nublar.dashboard.data

import kotlin.test.Test
import kotlin.test.assertTrue

class IslaNublarRepositoryTest {

    /**
     * The bundled repository must serve every model type non-empty — all of
     * them now come from JSON files under data/isla-nublar/. A missing or
     * renamed JSON resource fails here.
     */
    @Test
    fun `bundled repository serves all park data`() {
        val repo = BundledIslaNublarRepository()

        assertTrue(repo.paddocks().paddocks.isNotEmpty(), "no paddocks loaded")
        assertTrue(repo.facilities().isNotEmpty(), "no facilities loaded")
        assertTrue(repo.vehicles().isNotEmpty(), "no vehicles loaded")
        assertTrue(repo.dinosaurs().isNotEmpty(), "no dinosaurs loaded")
        assertTrue(repo.staff().isNotEmpty(), "no staff loaded")
        assertTrue(repo.fences().isNotEmpty(), "no fences loaded")
        assertTrue(repo.paddockRecipes().isNotEmpty(), "no paddock recipes loaded")
        assertTrue(repo.glitchesLog().isNotEmpty(), "no glitches log loaded")
    }
}
