package com.asif.kmmauthorizedimageprofiles.profile

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.asif.kmmauthorizedimageprofiles.ProfileResponse
import com.asif.kmmauthorizedimageprofiles.UserRepository
import com.asif.kmmauthorizedimageprofiles.android.SecurePreferences
import com.asif.kmmauthorizedimageprofiles.android.profile.ProfileState
import com.asif.kmmauthorizedimageprofiles.android.profile.ProfileViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: UserRepository
    private lateinit var securePreferences: SecurePreferences
    private lateinit var context: Context
    private lateinit var viewModel: ProfileViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = mockk()
        securePreferences = mockk(relaxed = true)
        context = mockk(relaxed = true)
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(repository, securePreferences, context)
    }

    @Test
    fun `getProfile success updates state to Success`() = runTest {
        val profileResponse = ProfileResponse("fake-userid", "test@example.com")
        every { securePreferences.getToken() } returns "fake-token"
        every { securePreferences.getUserId() } returns "fake-userid"
        coEvery { repository.getProfile("fake-userid", "fake-token") } returns profileResponse

        viewModel.getProfile()
        advanceUntilIdle()

        assertTrue(viewModel.profileState.value is ProfileState.Success)
        assertEquals(profileResponse, (viewModel.profileState.value as ProfileState.Success).profile)
    }

    @Test
    fun `getProfile failure updates state to Error`() = runTest {
        every { securePreferences.getToken() } returns "fake-token"
        every { securePreferences.getUserId() } returns "fake-userid"
        coEvery { repository.getProfile("fake-userid", "fake-token") } throws Exception("Profile load failed")

        viewModel.getProfile()
        advanceUntilIdle()

        assertTrue(viewModel.profileState.value is ProfileState.Error)
        assertEquals("Error loading profile: Profile load failed", (viewModel.profileState.value as ProfileState.Error).message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // reset the main dispatcher to the original Main dispatcher
    }
}
