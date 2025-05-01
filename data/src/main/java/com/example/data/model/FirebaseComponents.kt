package com.example.data.model

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class FirebaseComponents(
    val firestore: FirebaseFirestore,
    val messagesRef: CollectionReference,
    val snapshotListener: ListenerRegistration?
)
