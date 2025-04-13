package com.example.dnmotors.view.fragments.carFragment.сredit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R
import com.example.dnmotors.databinding.FragmentCarCreditBinding
import java.text.DecimalFormat

class CarCreditFragment : Fragment() {
    private var _binding: FragmentCarCreditBinding? = null
    private val binding get() = _binding!!

    private val interestRate = 14.0 // Годовая процентная ставка
    private var loanTerm = 12 // По умолчанию 12 месяцев
    private var selectedButton: View? = null // Хранит текущую выбранную кнопку

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarCreditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aboutCredit = view.findViewById<Button>(R.id.aboutCredit)
        aboutCredit.setOnClickListener {
            findNavController().navigate(R.id.action_carCreditFragment_to_aboutCreditFragment)
        }
        val requestCredit = view.findViewById<Button>(R.id.requestCredit)
        requestCredit.setOnClickListener {
            findNavController().navigate(R.id.action_carCreditFragment_to_requestCreditFragment)
        }

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        selectedButton = binding.month12
        setupListeners()

    }

    private fun setupListeners() {
        val buttons = listOf(
            binding.month12 to 12, binding.month24 to 24, binding.month36 to 36,
            binding.month48 to 48, binding.month60 to 60, binding.month72 to 72,
            binding.month84 to 84
        )

        buttons.forEach { (button, term) ->
            button.setOnClickListener { updateLoanTerm(term, button) }
        }

        binding.carCost.setOnFocusChangeListener { _, _ -> calculateMonthlyPayment() }
        binding.initialPayment.setOnFocusChangeListener { _, _ -> calculateMonthlyPayment() }
    }

    private fun updateLoanTerm(term: Int, button: View) {
        loanTerm = term

        // Сброс стиля у предыдущей кнопки
        selectedButton?.setBackgroundTintList(
            ContextCompat.getColorStateList(requireContext(), R.color.credit_term_button)
        )
        (selectedButton as? android.widget.Button)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.black)
        )

        // Установка стиля для новой кнопки
        button.setBackgroundTintList(
            ContextCompat.getColorStateList(requireContext(), R.color.primary_red)
        )
        (button as? android.widget.Button)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )

        // Обновление выбранной кнопки
        selectedButton = button

        // Пересчет платежа
        calculateMonthlyPayment()
    }

    private fun calculateMonthlyPayment() {
        val carCostStr = binding.carCost.text.toString().replace(" ", "")
        val initialPaymentStr = binding.initialPayment.text.toString().replace(" ", "")

        val carCost = carCostStr.toDoubleOrNull()
        val initialPayment = initialPaymentStr.toDoubleOrNull()

        binding.carCost.error = null
        binding.initialPayment.error = null

        if (carCost == null || carCost <= 0) {
            binding.carCost.error = "Введите корректную стоимость автомобиля"
            return
        }

        if (carCost < 1000000) {
            binding.carCost.error = "Минимальная цена 1 000 000 ₸"
            return
        }

        if (initialPayment == null || initialPayment <= 0) {
            binding.initialPayment.error = "Введите корректный первоначальный взнос"
            return
        }

        if (initialPayment < carCost / 5) {
            binding.initialPayment.error = "Минимальный первоначальный взнос 20% от стоимости авто"
            return
        }

        val loanAmount = carCost - initialPayment
        if (loanAmount <= 0) return

        val monthlyRate = (interestRate / 12) / 100
        val numerator = monthlyRate * Math.pow(1 + monthlyRate, loanTerm.toDouble())
        val denominator = Math.pow(1 + monthlyRate, loanTerm.toDouble()) - 1
        val monthlyPayment = loanAmount * (numerator / denominator)

        val formatter = DecimalFormat("#,###")
        binding.monthlyPayment.text = "${formatter.format(monthlyPayment)} ₸"
        binding.creditSum.text = "${formatter.format(loanAmount)} ₸"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
