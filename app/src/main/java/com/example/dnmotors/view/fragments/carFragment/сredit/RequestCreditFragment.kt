package com.example.dnmotors.view.fragments.carFragment.сredit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.dnmotors.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RequestCreditFragment : Fragment() {
    private var selectedCityIndex = -1  // Запоминаем выбранный город
    private val cities = arrayOf(
        "Актау", "Актобе", "Алматы", "Астана", "Атырау",
        "Караганда", "Кокшетау", "Костанай", "Кызылорда", "Павлодар"
    )

    private lateinit var database: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_request_credit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvSelectedCity = view.findViewById<TextView>(R.id.tv_selected_city)
        val etName = view.findViewById<EditText>(R.id.et_name)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val btnSubmit = view.findViewById<Button>(R.id.btn_submit)

        database = FirebaseDatabase.getInstance().getReference("credit_requests")

        tvSelectedCity.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Выберите город")
                .setSingleChoiceItems(cities, selectedCityIndex) { _, which ->
                    selectedCityIndex = which
                }
                .setPositiveButton("ОК") { dialog, _ ->
                    if (selectedCityIndex != -1) {
                        tvSelectedCity.text = cities[selectedCityIndex]
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
                .show()
        }

    btnSubmit.setOnClickListener {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val city = if (selectedCityIndex != -1) cities[selectedCityIndex] else ""

        if (name.isEmpty() || phone.isEmpty() || city.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }

        val requestId = database.push().key ?: return@setOnClickListener
        val creditRequest = CreditRequest(name, phone, city)

        database.child(requestId).setValue(creditRequest)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Заявка успешно отправлена!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка отправки. Повторите попытку.", Toast.LENGTH_SHORT).show()
            }
    }
}

}


