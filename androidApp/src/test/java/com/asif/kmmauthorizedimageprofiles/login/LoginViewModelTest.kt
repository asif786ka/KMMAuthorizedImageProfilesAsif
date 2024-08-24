package com.asif.kmmauthorizedimageprofiles.android.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.asif.kmmauthorizedimageprofiles.TokenResponse
import com.asif.kmmauthorizedimageprofiles.UserRepository
import com.asif.kmmauthorizedimageprofiles.android.SecurePreferences
import io.mockk.coEvery
import io.mockk.mockk
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
class LoginViewModelTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    private lateinit var repository: UserRepository
    private lateinit var securePreferences: SecurePreferences
    private lateinit var viewModel: LoginViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        repository = mockk()
        securePreferences = mockk(relaxed = true)  // relaxed = true allows you to ignore unstubbed calls
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(repository, securePreferences)
    }

    @Test
    fun `login success updates state to Success`() = runTest {
        val tokenResponse = TokenResponse("fake-userid", "fake-token")
        coEvery { repository.login("test@example.com", "password123") } returns tokenResponse

        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginState.Success)
        assertEquals("fake-token", (viewModel.loginState.value as LoginState.Success).tokenResponse.token)
    }

    @Test
    fun `login failure updates state to Error`() = runTest {
        coEvery { repository.login("test@example.com", "password123") } returns null

        viewModel.login("test@example.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginState.Error)
        assertEquals("Invalid email or password. Please try again.", (viewModel.loginState.value as LoginState.Error).message)
    }

    @Test
    fun `register success updates state to Success`() = runTest {
        val tokenResponse = TokenResponse("fake-userid", "fake-token")
        coEvery { repository.register("test@example.com", "password123") } returns tokenResponse

        viewModel.register("test@example.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginState.Success)
        assertEquals("fake-token", (viewModel.loginState.value as LoginState.Success).tokenResponse.token)
    }

    @Test
    fun `register failure updates state to Error`() = runTest {
        coEvery { repository.register("test@example.com", "password123") } returns null

        viewModel.register("test@example.com", "password123")
        advanceUntilIdle()

        assertTrue(viewModel.loginState.value is LoginState.Error)
        assertEquals("Registration failed. Please try again.", (viewModel.loginState.value as LoginState.Error).message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()  // reset the main dispatcher to the original Main dispatcher
        //testDispatcher.cleanupTestCoroutines()
    }
}
