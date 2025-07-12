package com.example.dormconnectapp

import android.app.AlertDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormconnectapp.databinding.FragmentFeedBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.dormconnectapp.data.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage

class Feedfragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: FeedAdapter
    private val feedList = mutableListOf<FeedPost>()
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imagePreview?.setImageURI(uri)
        }
    }
    private var imagePreview: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

        adapter = FeedAdapter(feedList)
        binding.feedRecyclerView.adapter = adapter
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadFeedPosts()

        binding.fabAddPost.setOnClickListener {
            showAddPostDialog()
        }

        Toast.makeText(requireContext(), "Feedfragment loaded", Toast.LENGTH_SHORT).show()

        Log.d("FABCheck", "FAB exists: ${binding.fabAddPost != null}")
        binding.fabAddPost.setBackgroundColor(Color.GREEN)


    }

    private fun showAddPostDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_post, null)
        val etPostContent = dialogView.findViewById<EditText>(R.id.etPostContent)
        imagePreview = dialogView.findViewById(R.id.ivSelectedImage)

        imagePreview?.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("New Post")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val content = etPostContent.text.toString().trim()
                if (content.isNotEmpty()) {
                    if (selectedImageUri != null) {
                        uploadImageAndSavePost(content, selectedImageUri!!)
                    } else {
                        savePostToFirestore(content, null)
                    }
                } else {
                    Toast.makeText(context, "Post cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




    private fun loadFeedPosts() {
        firestore.collection("feed_posts")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Error loading posts", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    feedList.clear()
                    Log.d("FeedSnapshot", "Documents: ${snapshots.size()}")

                    for (doc in snapshots) {
                        val post = doc.toObject(FeedPost::class.java)
                        post.postId = doc.id
                        feedList.add(post)
                        Log.d("RawDoc", doc.data.toString())

                    }

                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun savePostToFirestore(content: String, imageUrl: String?) {
        val post = hashMapOf(
            "username" to (FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"),
            "content" to content,
            "profileImageUrl" to null,
            "postImageUrl" to imageUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("feed_posts")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(context, "Post added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add post: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun uploadImageAndSavePost(content: String, imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
            .child("feed_images/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    savePostToFirestore(content, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
