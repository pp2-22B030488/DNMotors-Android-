package com.example.androidadvanceddnmotors.ui.dialogs

import android.content.res.ColorStateList
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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

    override fun onStart() {
        super.onStart()

        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

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
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.primary_red)
        val unselectedColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val tabs = listOf(binding.stateAll, binding.stateNew, binding.stateUsed)
        tabs.forEach { btn ->
            val color = if (btn.isSelected) selectedColor else unselectedColor
            val textColor = if (btn.isSelected) selectedTextColor else unselectedTextColor

            btn.backgroundTintList = ColorStateList.valueOf(color)
            btn.setTextColor(textColor)
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
        val container = binding.yearChipGroup
        container.removeAllViews()
        container.isSingleSelection = true
        val years = (2024 downTo 2010).toList()

        val chips = mutableListOf<Chip>()

        years.forEach { year ->
            val chip = Chip(requireContext()).apply {
                text = year.toString()
                textSize = 16f
                isCheckable = true
                isCheckedIconVisible = false
                setTextColor(ContextCompat.getColor(context, R.color.black))

                chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))

                chipStrokeWidth = 2f
                chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.secondary_text))
                chipCornerRadius = 8f

                setPadding(60, 50, 60, 50)

                val params = ChipGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 20, 0)
                layoutParams = params



                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedYear = year
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_red))
                    } else {
                        if (selectedYear == year) selectedYear = null
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.white))
                    }
                }
            }

            container.addView(chip)
            chips.add(chip)
        }

        yearButtons = chips

        // Отметить ранее выбранный год
        selectedYear?.let { year ->
            chips.find { it.text == year.toString() }?.isChecked = true
        }
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