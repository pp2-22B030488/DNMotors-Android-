package com.example.data.repository

import android.util.Log
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user ?: throw IllegalStateException("User is null after Google sign-in")
        checkAndCreateUserDocument(user.uid, user.displayName, user.email)
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        authResult.user ?: throw IllegalStateException("User is null after email sign-in")
    }

    override suspend fun registerWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user ?: throw IllegalStateException("User is null after Google registration")
        checkAndCreateUserDocument(user.uid, user.displayName, user.email)
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        location: String,
        phoneNumber: String
    ): Result<Unit> = runCatching {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw IllegalStateException("User is null after registration")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri(android.net.Uri.parse("https://example.com/default_profile_image.jpg"))
            .build()
        user.updateProfile(profileUpdates).await()

        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "location" to location,
            "phoneNumber" to phoneNumber,
            "role" to "user"
        )
        firestore.collection("users").document(user.uid).set(userMap).await()
    }

    override suspend fun fetchUserRole(uid: String): Result<String> = runCatching {
        val doc = firestore.collection("users").document(uid).get().await()
        doc.getString("role") ?: "user"
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private suspend fun checkAndCreateUserDocument(uid: String, name: String?, email: String?) {
        val docRef = firestore.collection("users").document(uid)
        val doc = docRef.get().await()
        if (!doc.exists()) {
            val userMap = hashMapOf(
                "name" to (name ?: "Unknown"),
                "email" to (email ?: "Unknown"),
                "role" to "user"
            )
            docRef.set(userMap).await()
        }
    }
}
