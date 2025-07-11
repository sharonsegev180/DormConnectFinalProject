package com.example.dormconnectapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedAdapter(private val feedList: List<FeedPost>) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = feedList[position]
        holder.usernameText.text = item.username
        holder.profileImage.setImageResource(item.profileImageRes)
        holder.postContent.text = item.content

        if (item.postImageRes != null) {
            holder.postImage.setImageResource(item.postImageRes)
            holder.postImage.visibility = View.VISIBLE
        } else {
            holder.postImage.visibility = View.GONE
        }

        // Placeholder like button logic
        holder.likeButton.setOnClickListener {
            // You can toggle like here
        }
    }

    override fun getItemCount(): Int = feedList.size
}
