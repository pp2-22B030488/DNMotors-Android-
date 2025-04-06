package com.example.dnmotors

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dnmotors.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

//        auth = FirebaseAuth.getInstance()
        auth = Firebase.auth

        findViewById<Button>(R.id.bRegister).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            val name = findViewById<EditText>(R.id.etName).text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods ?: emptyList()
                        if (signInMethods.isEmpty()) {
                            registerUser(email, password, name)
                        } else {
                            Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d("MyLog", "Error checking email: ${task.exception?.message}")
                        Toast.makeText(this, "Error checking email!", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = hashMapOf(
                    "name" to name,
                    "email" to email,
                )
                checkAuthState()
                FirebaseFirestore.getInstance().collection("users").document(auth.currentUser!!.uid)
                    .set(user)
                    .addOnSuccessListener {
//                        Log.d("MyLog", "User registered successfully: ${auth.currentUser?.uid}")
//                        Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()


//                        checkAuthState()
//                        finish() // Закрываем активность после успешной регистрации

                    }
                    .addOnFailureListener {
                        Log.d("MyLog", "Firestore write failed: ${it.message}")
                    }
            } else {
                if (task.exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                    Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Registration failed!", Toast.LENGTH_SHORT).show()
                }
                Log.d("MyLog", "Registration failed: ${task.exception?.message}")
            }
        }

    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "CarFragment") // Передаём ключ
            startActivity(intent)
            finish()
        } else {
            Log.d("MyLog", "Auth state check failed: user is null")
        }
    }

}
