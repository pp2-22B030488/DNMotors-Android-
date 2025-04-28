package com.example.androidadvanceddnmotors.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.dnmotors.databinding.DialogFilterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.dnmotors.R
import android.widget.LinearLayout
import android.widget.AutoCompleteTextView

class FilterDialog : BottomSheetDialogFragment() {
    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!
    private var onFilterAppliedListener: ((Filter) -> Unit)? = null
    private var selectedState: State = State.ALL
    private var selectedYear: Int? = null
    private var yearButtons: List<Button> = emptyList()

    data class Filter(
        val state: State = State.ALL,
        val brandModel: String? = null,
        val year: Int? = null,
        val priceFrom: Int? = null,
        val priceTo: Int? = null,
        val initialPayment: Boolean = false,
        val withPhoto: Boolean = false,
        val customsCleared: Boolean = false
    )

    enum class State { ALL, NEW, USED }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStateTabs()
        setupBrandModelAutocomplete()
        setupYearButtons()
        setupShowResultsButton()
    }

    private fun setupStateTabs() {
        val tabs = listOf(binding.stateAll, binding.stateNew, binding.stateUsed)
        tabs.forEach { btn ->
            btn.setOnClickListener {
                tabs.forEach { it.isSelected = false }
                btn.isSelected = true
                selectedState = when (btn.id) {
                    binding.stateAll.id -> State.ALL
                    binding.stateNew.id -> State.NEW
                    binding.stateUsed.id -> State.USED
                    else -> State.ALL
                }
                updateTabStyles()
            }
        }
        binding.stateAll.isSelected = true
        updateTabStyles()
    }

    private fun updateTabStyles() {
        val selectedBg = ContextCompat.getDrawable(requireContext(), com.example.dnmotors.R.drawable.edittext_bg_carcredit)
        val unselectedBg = ContextCompat.getDrawable(requireContext(), android.R.color.transparent)
        val selectedText = ContextCompat.getColor(requireContext(), com.example.dnmotors.R.color.white)
        val unselectedText = ContextCompat.getColor(requireContext(), com.example.dnmotors.R.color.white)
        val tabs = listOf(binding.stateAll, binding.stateNew, binding.stateUsed)
        tabs.forEach { btn ->
            btn.background = if (btn.isSelected) selectedBg else unselectedBg
            btn.setTextColor(if (btn.isSelected) selectedText else unselectedText)
        }
    }

    private fun setupBrandModelAutocomplete() {
        val brands = arrayOf("BMW", "Toyota", "Honda", "Mercedes", "Audi", "Volkswagen")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, brands)
        binding.brandModelAutoComplete.setAdapter(adapter)
        binding.brandModelAutoComplete.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as AutoCompleteTextView).showDropDown()
        }
        binding.brandModelAutoComplete.setOnClickListener {
            (it as AutoCompleteTextView).showDropDown()
        }
    }

    private fun setupYearButtons() {
        val container = binding.root.findViewById<LinearLayout>(com.example.dnmotors.R.id.yearButtonsContainer)
        container.removeAllViews()
        val years = (2024 downTo 2010).toList()
        val buttons = mutableListOf<Button>()
        years.forEach { year ->
            val btn = Button(requireContext()).apply {
                text = year.toString()
                textSize = 15f
                setTextColor(ContextCompat.getColor(context, com.example.dnmotors.R.color.black))
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
                isAllCaps = false
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    80 // px, можно заменить на dp через TypedValue
                )
                params.setMargins(0, 0, 16, 0)
                layoutParams = params
                setOnClickListener {
                    if (selectedYear == year) {
                        // Сбросить выбор
                        selectedYear = null
                        buttons.forEach { it.isSelected = false }
                    } else {
                        buttons.forEach { it.isSelected = false }
                        this.isSelected = true
                        selectedYear = year
                    }
                    updateYearStyles()
                }
            }
            container.addView(btn)
            buttons.add(btn)
        }
        yearButtons = buttons
        // Выделить ранее выбранный год, если есть
        selectedYear?.let { year ->
            buttons.find { it.text == year.toString() }?.let {
                it.isSelected = true
            }
        }
        updateYearStyles()
    }

    private fun updateYearStyles() {
        yearButtons.forEach { btn ->
            if (btn.isSelected) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), com.example.dnmotors.R.color.primary_red))
                btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                btn.setTextColor(ContextCompat.getColor(requireContext(), com.example.dnmotors.R.color.black))
            }
        }
    }

    private fun setupShowResultsButton() {
        binding.showResultsButton.setOnClickListener {
            val filter = Filter(
                state = selectedState,
                brandModel = binding.brandModelAutoComplete.text.toString().takeIf { it.isNotEmpty() },
                year = selectedYear,
                priceFrom = binding.priceFrom.text?.toString()?.toIntOrNull(),
                priceTo = binding.priceTo.text?.toString()?.toIntOrNull(),
                initialPayment = binding.checkboxInitialPayment.isChecked,
                withPhoto = binding.checkboxWithPhoto.isChecked,
                customsCleared = binding.checkboxCustomsCleared.isChecked
            )
            onFilterAppliedListener?.invoke(filter)
            dismiss()
        }
    }

    fun setOnFilterAppliedListener(listener: (Filter) -> Unit) {
        onFilterAppliedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Extensions для view binding (id-шники)
    private val DialogFilterBinding.stateAll: Button get() = root.findViewById(com.example.dnmotors.R.id.state_all)
    private val DialogFilterBinding.stateNew: Button get() = root.findViewById(com.example.dnmotors.R.id.state_new)
    private val DialogFilterBinding.stateUsed: Button get() = root.findViewById(com.example.dnmotors.R.id.state_used)
} 