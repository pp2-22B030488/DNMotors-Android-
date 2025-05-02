package com.example.data.repository

import android.net.Uri
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        firebaseAuth = mock(FirebaseAuth::class.java)
        firestore = mock(FirebaseFirestore::class.java)
        repository = AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Test
    fun `isUserSignedIn returns true if user exists`() {
        `when`(firebaseAuth.currentUser).thenReturn(mock(FirebaseUser::class.java))
        assertTrue(repository.isUserSignedIn())
    }

    @Test
    fun `returnAuth returns correct AuthUser`() = runTest {
        val user = mock(FirebaseUser::class.java)
        `when`(user.uid).thenReturn("uid123")
        `when`(user.email).thenReturn("test@example.com")
        `when`(user.photoUrl).thenReturn(Uri.parse("https://example.com/pic"))
        `when`(user.displayName).thenReturn("John")
        `when`(FirebaseAuth.getInstance().currentUser).thenReturn(user)

        val result = repository.returnAuth()

        assertEquals("uid123", result.uid)
        assertEquals("test@example.com", result.email)
        assertEquals("https://example.com/pic", result.photoUrl)
        assertEquals("John", result.displayName)
    }
}
