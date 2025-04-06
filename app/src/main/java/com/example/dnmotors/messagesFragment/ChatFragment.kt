package com.example.dnmotors.messagesFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.example.dnmotors.R

class ChatFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val generalChat = view.findViewById<CardView>(R.id.generalChat)

        generalChat.setOnClickListener {
            // Переход на другой фрагмент
            findNavController().navigate(R.id.action_chatFragment_to_messagesFragment)
        }
    }

}