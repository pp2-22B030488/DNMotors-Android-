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
import android.widget.AutoCompleteTextView
import com.example.dnmotors.R


class FilterDialog : BottomSheetDialogFragment() {
    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!
    private var onFilterAppliedListener: ((Filter) -> Unit)? = null
    private var selectedState: State = State.ALL
    private var selectedTransmission: Transmission = Transmission.MANUAL

    data class Filter(
        val state: State = State.ALL,
        val brandModel: String? = null,
        val yearFrom: Int? = null,
        val yearTo: Int? = null,
        val priceFrom: Int? = null,
        val priceTo: Int? = null,
        val mileageFrom: Int? = null,
        val mileageTo: Int? = null,
        val transmission: String? = null,
        val location: String? = null
    )

    enum class State { ALL, NEW, USED }
    enum class Transmission { MANUAL, AUTOMATIC }

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
        setupTransmissionTabs()
        setupBrandModelAutocomplete()
        setupLocationAutocomplete()
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

    private fun setupTransmissionTabs() {
        val tabs = listOf(binding.transmissionManual, binding.transmissionAutomatic)
        tabs.forEach { btn ->
            btn.setOnClickListener {
                tabs.forEach { it.isSelected = false }
                btn.isSelected = true
                selectedTransmission = when (btn.id) {
                    binding.transmissionManual.id -> Transmission.MANUAL
                    binding.transmissionAutomatic.id -> Transmission.AUTOMATIC
                    else -> Transmission.MANUAL
                }
                updateTransmissionTabStyles()
            }
        }
        binding.transmissionAutomatic.isSelected = true
        updateTransmissionTabStyles()
    }

    private fun updateTransmissionTabStyles() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.primary_red)
        val unselectedColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val tabs = listOf(binding.transmissionManual, binding.transmissionAutomatic)
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

    private fun setupLocationAutocomplete() {
        val locations = arrayOf("Almaty", "Astana", "Karaganda", "Shymkent", "Pavlodar")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locations)
        binding.locationAutoComplete.setAdapter(adapter)
        binding.locationAutoComplete.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) (v as AutoCompleteTextView).showDropDown()
        }
        binding.locationAutoComplete.setOnClickListener {
            (it as AutoCompleteTextView).showDropDown()
        }
    }

    private fun setupShowResultsButton() {
        binding.showResultsButton.setOnClickListener {
            val filter = Filter(
                state = selectedState,
                brandModel = binding.brandModelAutoComplete.text.toString().takeIf { it.isNotEmpty() },
                yearFrom = binding.yearFrom.text?.toString()?.toIntOrNull(),
                yearTo = binding.yearTo.text?.toString()?.toIntOrNull(),
                priceFrom = binding.priceFrom.text?.toString()?.toIntOrNull(),
                priceTo = binding.priceTo.text?.toString()?.toIntOrNull(),
                mileageFrom = binding.mileageFrom.text?.toString()?.toIntOrNull(),
                mileageTo = binding.mileageTo.text?.toString()?.toIntOrNull(),
                transmission = selectedTransmission.toString(),
                location = binding.locationAutoComplete.text.toString().takeIf { it.isNotEmpty() }
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
} 