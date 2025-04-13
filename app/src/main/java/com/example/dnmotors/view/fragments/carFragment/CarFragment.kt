package com.example.dnmotors.view.fragments.carFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarBinding

class CarFragment : Fragment() {

    private var _binding: FragmentCarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val logo = view.findViewById<ImageView>(R.id.logo)
        val btnCredit = view.findViewById<ImageView>(R.id.btnCredit)

        btnCredit.setOnClickListener {
            // Переход на другой фрагмент
            findNavController().navigate(R.id.action_carFragment_to_carCreditFragment)
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}