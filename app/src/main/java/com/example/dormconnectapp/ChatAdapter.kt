package com.example.dormconnectapp

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dormconnectapp.data.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.example.dormconnectapp.R

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.chatMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 1)
            R.layout.item_chat_right
        else
            R.layout.item_chat_left

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.messageText.text = messages[position].message

    }

    override fun getItemCount(): Int = messages.size
}
