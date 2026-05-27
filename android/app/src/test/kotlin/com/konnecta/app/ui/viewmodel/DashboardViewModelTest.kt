package com.konnecta.app.ui.viewmodel

import com.konnecta.app.data.model.Group
import com.konnecta.app.data.remote.ActivityService
import com.konnecta.app.data.remote.AttendanceService
import com.konnecta.app.data.remote.GroupService
import com.konnecta.app.data.remote.LeaderboardService
import com.konnecta.app.data.remote.ProfileService
import com.konnecta.app.data.remote.WeatherService
import com.konnecta.app.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val attendanceService = mockk<AttendanceService>(relaxed = true)
    private val activityService = mockk<ActivityService>(relaxed = true)
    private val weatherService = mockk<WeatherService>(relaxed = true)
    private val leaderboardService = mockk<LeaderboardService>(relaxed = true)
    private val groupService = mockk<GroupService>(relaxed = true)
    private val profileService = mockk<ProfileService>(relaxed = true)

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        viewModel = DashboardViewModel(
            attendanceService, activityService, weatherService,
            leaderboardService, groupService, profileService
        )
    }

    @After
    fun tearDown() = unmockkAll()

    // Stubs all services so loadInitialData/loadDashboardData completes cleanly.
    private suspend fun TestScope.initWithUserAndGroup(
        userId: String = "user-1",
        group: Group = Group("g-1", "Test Group", "test-group", userId)
    ) {
        coEvery { profileService.getProfile(userId) } returns null
        coEvery { groupService.getUserGroups(userId) } returns listOf(group)
        coEvery { attendanceService.getAttendance(any(), any()) } returns emptyList()
        coEvery { attendanceService.getGroupMembers(any()) } returns emptyList()
        coEvery { activityService.getActivities(any(), any()) } returns emptyList()
        coEvery { weatherService.getWeekendWeather(any()) } returns null
        coEvery { leaderboardService.getLeaderboard(any()) } returns emptyList()

        viewModel.loadInitialData(userId)
        advanceUntilIdle()
    }

    // ── createGroup validation ───────────────────────────────────────────────

    @Test
    fun `createGroup with blank name calls onResult with null without touching the service`() =
        runTest {
            viewModel.loadInitialData("user-1") // sets currentUserId synchronously

            val results = mutableListOf<Group?>()
            viewModel.createGroup("   ", results::add)

            assertEquals(listOf(null), results)
            coVerify(exactly = 0) { groupService.createGroup(any(), any(), any()) }
        }

    @Test
    fun `createGroup with name longer than 50 chars calls onResult with null`() = runTest {
        viewModel.loadInitialData("user-1")

        val results = mutableListOf<Group?>()
        viewModel.createGroup("A".repeat(51), results::add)

        assertEquals(listOf(null), results)
        coVerify(exactly = 0) { groupService.createGroup(any(), any(), any()) }
    }

    @Test
    fun `createGroup returns without calling onResult when currentUserId is not set`() = runTest {
        val results = mutableListOf<Group?>()
        viewModel.createGroup("Valid Name", results::add)

        assertTrue("onResult should not be called when userId is null", results.isEmpty())
        coVerify(exactly = 0) { groupService.createGroup(any(), any(), any()) }
    }

    // ── loadInitialData ──────────────────────────────────────────────────────

    @Test
    fun `loadInitialData with no groups leaves activeGroup null and clears loading`() = runTest {
        coEvery { profileService.getProfile("user-1") } returns null
        coEvery { groupService.getUserGroups("user-1") } returns emptyList()

        viewModel.loadInitialData("user-1")
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertNull(activeGroup)
            assertEquals(emptyList<Group>(), userGroups)
            assertFalse(isLoading)
        }
    }

    @Test
    fun `loadInitialData sets first group as activeGroup`() = runTest {
        initWithUserAndGroup(group = Group("g-1", "Amics", "amics", "user-1"))

        assertEquals("g-1", viewModel.state.value.activeGroup?.id)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `loadInitialData sets Catalan error message on exception`() = runTest {
        coEvery { profileService.getProfile(any()) } throws RuntimeException("network down")

        viewModel.loadInitialData("user-1")
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertFalse(isLoading)
            assertEquals("Error de connexió. Torna-ho a intentar.", error)
        }
    }

    // ── loadDashboardData ────────────────────────────────────────────────────

    @Test
    fun `loadDashboardData sets partial error when attendance service fails after retries`() =
        runTest {
            coEvery {
                attendanceService.getAttendance(
                    any(),
                    any()
                )
            } throws IOException("Network error")
            coEvery { attendanceService.getGroupMembers(any()) } returns emptyList()
            coEvery { activityService.getActivities(any(), any()) } returns emptyList()
            coEvery { weatherService.getWeekendWeather(any()) } returns null
            coEvery { leaderboardService.getLeaderboard(any()) } returns emptyList()

            viewModel.loadDashboardData("2025-01-17", "g-1")
            advanceUntilIdle()

            with(viewModel.state.value) {
                assertFalse(isLoading)
                assertEquals("No s'han pogut carregar totes les dades", error)
            }
        }

    @Test
    fun `loadDashboardData clears error and loading on success`() = runTest {
        coEvery { attendanceService.getAttendance(any(), any()) } returns emptyList()
        coEvery { attendanceService.getGroupMembers(any()) } returns emptyList()
        coEvery { activityService.getActivities(any(), any()) } returns emptyList()
        coEvery { weatherService.getWeekendWeather(any()) } returns null
        coEvery { leaderboardService.getLeaderboard(any()) } returns emptyList()

        viewModel.loadDashboardData("2025-01-17", "g-1")
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertNull(error)
            assertFalse(isLoading)
        }
    }

    // ── switchGroup ──────────────────────────────────────────────────────────

    @Test
    fun `switchGroup immediately updates activeGroup in state`() = runTest {
        val group1 = Group("g-1", "Group 1", "group-1", "user-1")
        val group2 = Group("g-2", "Group 2", "group-2", "user-1")
        coEvery { profileService.getProfile(any()) } returns null
        coEvery { groupService.getUserGroups(any()) } returns listOf(group1, group2)
        coEvery { attendanceService.getAttendance(any(), any()) } returns emptyList()
        coEvery { attendanceService.getGroupMembers(any()) } returns emptyList()
        coEvery { activityService.getActivities(any(), any()) } returns emptyList()
        coEvery { weatherService.getWeekendWeather(any()) } returns null
        coEvery { leaderboardService.getLeaderboard(any()) } returns emptyList()

        viewModel.loadInitialData("user-1")
        advanceUntilIdle()
        assertEquals("g-1", viewModel.state.value.activeGroup?.id)

        viewModel.switchGroup("g-2", "2025-01-17")

        // switchGroup updates state synchronously before launching the refresh coroutine.
        assertEquals("g-2", viewModel.state.value.activeGroup?.id)
    }

    // ── updateComment ────────────────────────────────────────────────────────

    @Test
    fun `updateComment truncates comment to 280 characters before persisting`() = runTest {
        initWithUserAndGroup()
        coEvery { attendanceService.updateComment(any(), any(), any(), any()) } returns true

        viewModel.updateComment("A".repeat(300), "2025-01-17")
        advanceUntilIdle()

        coVerify {
            attendanceService.updateComment(
                eq("user-1"),
                eq("g-1"),
                eq("2025-01-17"),
                match { it == "A".repeat(280) }
            )
        }
    }

    @Test
    fun `updateComment trims leading and trailing whitespace`() = runTest {
        initWithUserAndGroup()
        coEvery { attendanceService.updateComment(any(), any(), any(), any()) } returns true

        viewModel.updateComment("  benvinguts  ", "2025-01-17")
        advanceUntilIdle()

        coVerify {
            attendanceService.updateComment(any(), any(), any(), eq("benvinguts"))
        }
    }
}
