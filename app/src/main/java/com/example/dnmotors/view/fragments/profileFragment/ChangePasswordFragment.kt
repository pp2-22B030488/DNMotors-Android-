package com.example.dnmotors.view.fragments.profileFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun shakeView(view: View) {
        val shake = AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
        view.startAnimation(shake)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.btnSave.setOnClickListener {
            clearErrors()

            val currentPassword = binding.etCurrentPassword.text.toString().trim()
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val user = auth.currentUser

            if (user?.email.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Ошибка: email пользователя не найден", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentPassword.isEmpty()) {
                binding.etCurrentPassword.error = "Введите текущий пароль"
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                binding.etNewPassword.error = "Введите новый пароль"
                return@setOnClickListener
            }

            if (!isPasswordValid(newPassword)) {
                binding.etNewPassword.error = "Минимум 8 символов, буквы в верхнем и нижнем регистре, цифры"
                shakeView(binding.etCurrentPassword)
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.etConfirmPassword.error = "Повторите новый пароль"
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                binding.etConfirmPassword.error = "Пароли не совпадают"
                shakeView(binding.etCurrentPassword)
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Пароль успешно изменен", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Ошибка при изменении: ${it.message}", Toast.LENGTH_SHORT).show()
                            shakeView(binding.etCurrentPassword)
                        }
                }
                .addOnFailureListener {
                    binding.etCurrentPassword.error = "Неверный текущий пароль"
                    shakeView(binding.etCurrentPassword)
                }
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}\$")
        return passwordRegex.matches(password)
    }

    private fun clearErrors() {
        binding.etCurrentPassword.error = null
        binding.etNewPassword.error = null
        binding.etConfirmPassword.error = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

