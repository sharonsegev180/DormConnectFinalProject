package com.example.dormconnectapp

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormconnectapp.data.local.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val usersCol = FirebaseFirestore.getInstance().collection("users")

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.chatSenderName)
        val messageText: TextView = view.findViewById(R.id.chatMessageText)
        val timeText: TextView = view.findViewById(R.id.chatTimestampText)
    }

    override fun getItemViewType(position: Int): Int =
        if (messages[position].senderId == currentUserId) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 1) R.layout.item_chat_right else R.layout.item_chat_left
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.messageText.text = msg.message

        // 1. Lookup & display sender name
        usersCol.document(msg.senderId)
            .get()
            .addOnSuccessListener { doc ->
                holder.nameText.text = doc.getString("name") ?: "Anonymous"
            }
            .addOnFailureListener {
                holder.nameText.text = "Anonymous"
            }

        // 2. Show relative timestamp
        val tsMillis = msg.timestamp?.toDate()?.time ?: System.currentTimeMillis()
        val rel = DateUtils.getRelativeTimeSpanString(
            tsMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.timeText.text = rel
    }

    override fun getItemCount(): Int = messages.size
}
