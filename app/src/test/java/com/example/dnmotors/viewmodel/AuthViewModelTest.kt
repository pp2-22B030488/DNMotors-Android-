package com.example.dnmotors.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.domain.model.AuthUser
import com.example.domain.usecase.AuthUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel

    @Mock
    private lateinit var useCase: AuthUseCase

    @Mock
    private lateinit var observer: Observer<AuthResult>

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        authViewModel = AuthViewModel(useCase)
        authViewModel.authState.observeForever(observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn should post Success when signInWithEmail succeeds`() = testScope.runTest {
        `when`(useCase.signInWithEmail("test@example.com", "password")).thenReturn(Result.success(Unit))

        authViewModel.signIn("test@example.com", "password")
        advanceUntilIdle()

        verify(observer).onChanged(AuthResult.Loading)
        verify(observer).onChanged(AuthResult.Success)
    }

    @Test
    fun `signIn should post Error when signInWithEmail fails`() = testScope.runTest {
        `when`(useCase.signInWithEmail("test@example.com", "password"))
            .thenReturn(Result.failure(Exception("Login failed")))

        authViewModel.signIn("test@example.com", "password")
        advanceUntilIdle()

        verify(observer).onChanged(AuthResult.Loading)
        verify(observer).onChanged(AuthResult.Error("Login failed"))
    }

    @Test
    fun `register should post Success when registerWithEmail succeeds`() = testScope.runTest {
        `when`(useCase.registerWithEmail(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Result.success(Unit))

        authViewModel.register("test@example.com", "pass", "John", "NY", "123456")
        advanceUntilIdle()

        verify(observer).onChanged(AuthResult.Loading)
        verify(observer).onChanged(AuthResult.Success)
    }

    @Test
    fun `register should post Error when registerWithEmail fails`() = testScope.runTest {
        `when`(useCase.registerWithEmail(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Result.failure(Exception("Registration failed")))

        authViewModel.register("test@example.com", "pass", "John", "NY", "123456")
        advanceUntilIdle()

        verify(observer).onChanged(AuthResult.Loading)
        verify(observer).onChanged(AuthResult.Error("Registration failed"))
    }

    @Test
    fun `isUserSignedIn returns correct value`() {
        `when`(useCase.isUserSignedIn()).thenReturn(true)
        assert(authViewModel.isUserSignedIn())
    }

    @Test
    fun `returnAuth returns expected AuthUser`() = testScope.runTest {
        val expectedUser = AuthUser("uid123", "test@example.com", null, "John")
        `when`(useCase.returnAuth()).thenReturn(expectedUser)

        val result = authViewModel.returnAuth()
        assert(result == expectedUser)
    }
}
