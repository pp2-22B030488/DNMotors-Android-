package com.example.data.repository

import android.util.Log
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val TAG = "FirebaseAuthRepo"

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        Log.d(TAG, "Attempting Google Sign-In with token.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user
        if (user != null) {
            checkAndCreateUserDocument(user.uid, user.displayName, user.email)
            Log.d(TAG, "Google Sign-In successful for UID: ${user.uid}")
            Unit
        } else {
            Log.e(TAG, "Google Sign-In successful but user object is null.")
            throw IllegalStateException("Google Sign-In successful but user object is null.")
        }
    }.onFailure {
        Log.e(TAG, "Google Sign-In failed: ${it.message}", it)
    }


    override suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        Log.d(TAG, "Attempting Email Sign-In for: $email")
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = authResult.user
        if (user != null) {
            Log.d(TAG, "Email Sign-In successful for UID: ${user.uid}")
            Unit
        } else {
            Log.e(TAG, "Email Sign-In successful but user object is null.")
            throw IllegalStateException("Email Sign-In successful but user object is null.")
        }
    }.onFailure {
        Log.e(TAG, "Email Sign-In failed for $email: ${it.message}", it)
    }

    override suspend fun registerWithGoogle(idToken: String): Result<Unit> = runCatching {
        Log.d(TAG, "[Register] Attempting Google Registration/Sign-In with token.")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        val user = authResult.user
        if (user != null) {
            checkAndCreateUserDocument(user.uid, user.displayName, user.email)
            Log.d(TAG, "[Register] Google Registration/Sign-In successful for UID: ${user.uid}")
            Unit
        } else {
            Log.e(TAG, "[Register] Google Registration/Sign-In successful but user object is null.")
            throw IllegalStateException("Google Registration/Sign-In successful but user object is null.")
        }
    }.onFailure {
        Log.e(TAG, "[Register] Google Registration/Sign-In failed: ${it.message}", it)
    }

    override suspend fun registerWithEmail(email: String, password: String, name: String): Result<Unit> = runCatching {
        Log.d(TAG, "Attempting Email Registration for: $email, Name: $name")
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user

        if (user != null) {
            Log.d(TAG, "User created with UID: ${user.uid}. Updating profile and Firestore.")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            val userMap = hashMapOf(
                "name" to name,
                "email" to email,
            )
            firestore.collection("users").document(user.uid).set(userMap).await()
            Log.d(TAG, "Registration and Firestore setup successful for UID: ${user.uid}")
            Unit
        } else {
            Log.e(TAG, "Email Registration created user but user object is null.")
            throw IllegalStateException("User creation successful but user object is null.")
        }
    }.onFailure {
        Log.e(TAG, "Email Registration failed for $email: ${it.message}", it)
    }

    override fun isUserSignedIn(): Boolean {
        val signedIn = firebaseAuth.currentUser != null
        Log.d(TAG, "Checking if user is signed in: $signedIn")
        return signedIn
    }

    private suspend fun checkAndCreateUserDocument(uid: String, name: String?, email: String?) {
        val userDocRef = firestore.collection("users").document(uid)
        try {
            val document = userDocRef.get().await()
            if (!document.exists()) {
                Log.d(TAG, "User document for UID $uid does not exist. Creating...")
                val newUser = hashMapOf(
                    "name" to (name ?: "Unknown Name"),
                    "email" to (email ?: "Unknown Email")
                )
                userDocRef.set(newUser).await()
                Log.d(TAG, "Firestore user document created for UID: $uid")
            } else {
                Log.d(TAG, "User document already exists for UID: $uid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/creating Firestore user document for UID $uid: ${e.message}", e)
        }
    }
}