package os.nublar.dashboard.viewmodel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppViewModelTest {

    @Test
    fun `restores the persisted screen`() {
        val vm = AppViewModel(restoreScreen = { Screen.IslandMap.name }, persistScreen = {})
        assertEquals(Screen.IslandMap, vm.screen)
    }

    @Test
    fun `falls back to Dashboard when nothing is persisted`() {
        val vm = AppViewModel(restoreScreen = { null }, persistScreen = {})
        assertEquals(Screen.Dashboard, vm.screen)
    }

    @Test
    fun `falls back to Dashboard on an unknown persisted name`() {
        // e.g. a screen that was renamed/removed between versions.
        val vm = AppViewModel(restoreScreen = { "NoSuchScreen" }, persistScreen = {})
        assertEquals(Screen.Dashboard, vm.screen)
    }

    @Test
    fun `navigateTo updates state and persists the screen name`() {
        var persisted: String? = null
        val vm = AppViewModel(restoreScreen = { null }, persistScreen = { persisted = it })

        vm.navigateTo(Screen.ControlRoomPlanView)

        assertEquals(Screen.ControlRoomPlanView, vm.screen)
        assertEquals(Screen.ControlRoomPlanView.name, persisted)
    }

    @Test
    fun `updateFullscreen stores and persists the flag`() {
        var persisted: Boolean? = null
        val vm = AppViewModel(
            restoreScreen = { null },
            persistScreen = {},
            restoreFullscreen = { false },
            persistFullscreen = { persisted = it },
        )
        assertFalse(vm.fullscreen)
        vm.updateFullscreen(true)
        assertTrue(vm.fullscreen)
        assertEquals(true, persisted)
        vm.updateFullscreen(false)
        assertFalse(vm.fullscreen)
        assertEquals(false, persisted)
    }

    @Test
    fun `transient screens are shown but not persisted`() {
        var persisted: String? = null
        val vm = AppViewModel(restoreScreen = { null }, persistScreen = { persisted = it })

        vm.navigateTo(Screen.IslandMap)
        vm.navigateTo(Screen.Settings)

        assertEquals(Screen.Settings, vm.screen)
        // Relaunch would restore the last MAIN screen, not Settings.
        assertEquals(Screen.IslandMap.name, persisted)
    }

    @Test
    fun `updateSplitFraction stores the new fraction`() {
        val vm = AppViewModel(restoreScreen = { null }, persistScreen = {})
        vm.updateSplitFraction(0.42f)
        assertEquals(0.42f, vm.splitFraction)
    }
}
