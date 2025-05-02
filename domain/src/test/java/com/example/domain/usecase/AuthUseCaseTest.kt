package com.example.domain.usecase

import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class AuthUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: AuthUseCase

    @Before
    fun setup() {
        repository = mock(AuthRepository::class.java)
        useCase = AuthUseCase(repository)
    }

    @Test
    fun `signInWithEmail returns success`() = runTest {
        `when`(repository.signInWithEmail("email", "pass")).thenReturn(Result.success(Unit))
        val result = useCase.signInWithEmail("email", "pass")
        assert(result.isSuccess)
    }

    @Test
    fun `registerWithEmail returns success`() = runTest {
        `when`(repository.registerWithEmail("e", "p", "n", "l", "ph"))
            .thenReturn(Result.success(Unit))

        val result = useCase.registerWithEmail("e", "p", "n", "l", "ph")
        assert(result.isSuccess)
    }

    @Test
    fun `returnAuth returns correct user`() = runTest {
        val expected = AuthUser("uid123", "test@example.com", null, "John")
        `when`(repository.returnAuth()).thenReturn(expected)

        val result = useCase.returnAuth()
        assertEquals(expected, result)
    }
}
