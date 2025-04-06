package com.example.dnmotors

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dnmotors.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class SignInActivity : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            }catch (e: ApiException){
                Log.d("MyLog", "Api exception")
            }
        }

        // Авторизация через Email/Password
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkAuthState()
                        } else {
                            Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signIn)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }
        // Переход на страницу регистрации
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }



    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle(){
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MyLog", "Google sign in done")

                val user = auth.currentUser
                if (user != null) {
                    val name = user.displayName ?: "No Name"
                    val email = user.email ?: "No Email"

                    Log.d("MyLog", "User Name: $name, Email: $email")

                    val userRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)

                    userRef.get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            val newUser = hashMapOf(
                                "name" to name,
                                "email" to email
                            )
                            userRef.set(newUser).addOnSuccessListener {
                                Log.d("MyLog", "User data saved in Firestore")
                            }.addOnFailureListener { e ->
                                Log.d("MyLog", "Firestore write error: ${e.message}")
                            }
                        }
                    }

                }

                checkAuthState()
            } else {
                Log.d("MyLog", "Google sign in error")
            }
        }
    }


    private fun checkAuthState(){
        if(auth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("OPEN_FRAGMENT", "CarFragment")
            startActivity(intent)
            finish()
        }
    }
}