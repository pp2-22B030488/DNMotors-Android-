package com.example.domain.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<Boolean> = suspendCancellableCoroutine { cont ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userDoc = firestore.collection("users").document(user.uid)
                        userDoc.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val newUser = hashMapOf(
                                    "name" to (user.displayName ?: "No Name"),
                                    "email" to (user.email ?: "No Email")
                                )
                                userDoc.set(newUser).addOnSuccessListener {
                                    Log.d("FirebaseAuthRepo", "Google user saved to Firestore")
                                }.addOnFailureListener {
                                    Log.e("FirebaseAuthRepo", "Failed to save Google user: ${it.message}")
                                }
                            }
                        }
                    }
                    cont.resume(Result.success(true))
                } else {
                    cont.resume(Result.failure(task.exception ?: Exception("Google Sign-In failed")))
                }
            }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> = suspendCancellableCoroutine { cont ->
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val userDoc = firestore.collection("users").document(user.uid)
                        userDoc.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val newUser = hashMapOf(
                                    "email" to (user.email ?: "No Email"),
                                    "name" to (user.displayName ?: "No Name") // Email login may not have a display name
                                )
                                userDoc.set(newUser).addOnSuccessListener {
                                    Log.d("FirebaseAuthRepo", "Email user saved to Firestore")
                                }.addOnFailureListener {
                                    Log.e("FirebaseAuthRepo", "Failed to save Email user: ${it.message}")
                                }
                            }
                        }
                    }
                    cont.resume(Result.success(true))
                } else {
                    cont.resume(Result.failure(task.exception ?: Exception("Email Sign-In failed")))
                }
            }
    }

    override suspend fun registerWithEmail(email: String, password: String, name: String): Result<Boolean> = suspendCancellableCoroutine { cont ->
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                    )

                    if (uid != null) {
                        firestore.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                Log.d("AuthRepository", "User document created")
                                cont.resume(Result.success(true))
                            }
                            .addOnFailureListener {
                                Log.e("AuthRepository", "Firestore write failed", it)
                                cont.resume(Result.failure(it))
                            }
                    } else {
                        cont.resume(Result.failure(Exception("User ID is null after registration")))
                    }
                } else {
                    cont.resume(Result.failure(task.exception ?: Exception("Unknown registration error")))
                }
            }
    }

    override fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null
}
