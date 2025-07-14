package com.example.dormconnectapp

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dormconnectapp.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp

class ChatFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var editMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: ChatAdapter

    private val messages = mutableListOf<ChatMessage>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)
        editMessage = view.findViewById(R.id.editMessage)
        sendButton = view.findViewById(R.id.btnSend)

        adapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatRecyclerView.adapter = adapter

        listenForMessages()

        sendButton.setOnClickListener {
            val text = editMessage.text.toString().trim()
            val userId = auth.currentUser?.uid
            if (text.isNotEmpty() && userId != null) {
                val message = hashMapOf(
                    "senderId" to userId,
                    "message" to text,
                    "timestamp" to Timestamp.now()
                )
                db.collection("chats")
                    .document("global")
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        editMessage.setText("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        Log.d("ChatFragment", "ChatFragment loaded!")

    }

    private fun listenForMessages() {
        db.collection("chats")
            .document("global")
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshot.documents) {
                    val message = doc.toObject(ChatMessage::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
                adapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
    }
}
