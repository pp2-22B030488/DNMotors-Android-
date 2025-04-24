package com.example.dnmotors.utils

import com.example.domain.model.Message
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

fun observeNewMessages(
    vin: String,
    userId: String,
    onNewMessage: (Message) -> Unit
) {
    val ref = FirebaseDatabase.getInstance()
        .getReference("messages")
        .child(vin)
        .child(userId)

    ref.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val message = snapshot.getValue(Message::class.java)
            if (message != null) {
                onNewMessage(message)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
    })
}
