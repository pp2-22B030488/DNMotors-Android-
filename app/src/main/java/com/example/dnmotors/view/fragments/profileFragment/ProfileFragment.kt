package com.example.dnmotors.view.fragments.profileFragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentProfileBinding
import com.example.dnmotors.view.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutLayout = view.findViewById<View>(R.id.logout)
        logoutLayout.setOnClickListener {
            auth.signOut()

            val intent = Intent(requireActivity(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
        val userId = auth.currentUser?.uid ?: return

        // Слушатель переключателя уведомлений
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveUserNotificationSetting(userId, isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Уведомления включены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Уведомления выключены", Toast.LENGTH_SHORT).show()
            }
        }

        loadUserData()

        val passwordLayout = view.findViewById<View>(R.id.password_layout) // укажи id LinearLayout
        passwordLayout.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }
        binding.languageLayout.setOnClickListener {
            showLanguageSelectionDialog() // или перейти в LanguageFragment
        }



    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "No Name"
                    binding.userName.text = name
                    val avatarUrl = document.getString("avatarUrl")
                    if (!avatarUrl.isNullOrEmpty()) {
                        Picasso.get().load(avatarUrl).into(binding.ivProfile)
                    }
                    val backgroundUrl = document.getString("profileFon")
                    if (!backgroundUrl.isNullOrEmpty()) {
                        Picasso.get().load(backgroundUrl).into(binding.ivBackground)
                    }
                    val location = document.getString("location") ?: "No Location"
                    binding.tvLocation.text = location
                    val phoneNumber = document.getString("phoneNumber") ?: "No Phone Number"
                    binding.tvPhone.text = phoneNumber

                    val isEnabled = document.getBoolean("notificationsEnabled") ?: true
                    binding.switchNotifications.isChecked = isEnabled

                } else {
                    binding.userName.text = "User not found"
                }
            }
            .addOnFailureListener {
                binding.userName.text = "Error loading name"
            }
    }


    // Сохраняем состояние уведомлений
    private fun saveUserNotificationSetting(userId: String, isEnabled: Boolean) {
        db.collection("users").document(userId)
            .update("notificationsEnabled", isEnabled)
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Не удалось сохранить настройки уведомлений", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("Қазақша", "Русский", "English")
        val codes = arrayOf("kk", "ru", "en")
        val currentLang = getSavedLanguageCode()
        val selectedIndex = codes.indexOf(currentLang)

        AlertDialog.Builder(requireContext())
            .setTitle("Select Language")
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                saveLanguageCode(codes[which])
                setLocale(codes[which])
                dialog.dismiss()
                requireActivity().recreate() // перезапуск для применения изменений
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
    }

    private fun saveLanguageCode(code: String) {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("lang", code).apply()
    }

    private fun getSavedLanguageCode(): String {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("lang", "en") ?: "en"
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

