package com.example.dormconnectapp

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dormconnectapp.data.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormconnectapp.data.Comment
import com.google.firebase.firestore.Query


class FeedAdapter(private val feedList: List<FeedPost>) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val usernameText: TextView = view.findViewById(R.id.usernameText)
        val postContent: TextView = view.findViewById(R.id.postContent)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val likeButton: ImageView = view.findViewById(R.id.likeButton)
        val timestampText: TextView = view.findViewById(R.id.postTimestamp)
        val likeCountText: TextView = view.findViewById(R.id.likeCountText)
        val commentButton: ImageView = view.findViewById(R.id.commentButton)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = feedList[position]
        holder.usernameText.text = item.username
        holder.postContent.text = item.content

        // Load profile image from URL or use fallback
        if (!item.profileImageUrl.isNullOrEmpty()) {
            Glide.with(holder.profileImage.context)
                .load(item.profileImageUrl)
                .placeholder(R.drawable.user1)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(R.drawable.user1)
        }

        // Load post image from URL or hide it
        if (!item.postImageUrl.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.postImage.context)
                .load(item.postImageUrl)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.likeButton.setOnClickListener {
            // TODO: Implement like logic
        }

        val postTime = item.timestamp?.toDate()?.time ?: System.currentTimeMillis()
        val relativeTime = DateUtils.getRelativeTimeSpanString(
            postTime,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.timestampText.text = relativeTime

        val isLiked = item.likedBy.contains(FirebaseAuth.getInstance().currentUser?.uid)
        holder.likeButton.setImageResource(
            if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )


        holder.likeButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val postId = item.postId ?: return@setOnClickListener

            val isLikedNow = item.likedBy.contains(userId)  // ✅ must re-check fresh state
            val postRef = FirebaseFirestore.getInstance().collection("feed_posts").document(postId)

            val update = if (isLikedNow) {
                mapOf(
                    "likes" to FieldValue.increment(-1),
                    "likedBy" to FieldValue.arrayRemove(userId)
                )
            } else {
                mapOf(
                    "likes" to FieldValue.increment(1),
                    "likedBy" to FieldValue.arrayUnion(userId)
                )
            }

            postRef.update(update)
        }



        holder.likeCountText.text = item.likes.toString()

        holder.commentButton.setOnClickListener {
            val context = holder.itemView.context
            val postId = item.postId ?: return@setOnClickListener
            showCommentsDialog(context, postId)
        }


    }

    private fun showCommentsDialog(context: Context, postId: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comments, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.commentsRecyclerView)
        val input = dialogView.findViewById<EditText>(R.id.etNewComment)
        val sendBtn = dialogView.findViewById<ImageView>(R.id.btnSendComment)

        val comments = mutableListOf<Comment>()
        val adapter = CommentAdapter(comments)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Load existing comments
        FirebaseFirestore.getInstance()
            .collection("feed_posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error == null && snapshots != null) {
                    comments.clear()
                    for (doc in snapshots) {
                        comments.add(doc.toObject(Comment::class.java))
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        // Send new comment
        sendBtn.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                val comment = hashMapOf(
                    "username" to (FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"),
                    "content" to text,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                FirebaseFirestore.getInstance()
                    .collection("feed_posts").document(postId)
                    .collection("comments")
                    .add(comment)

                input.setText("")
            }
        }

        AlertDialog.Builder(context)
            .setTitle("Comments")
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .show()
    }

    override fun getItemCount(): Int = feedList.size
}
