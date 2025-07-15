package com.example.dormconnectapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dormconnectapp.R
import com.example.dormconnectapp.data.local.FeedPost
import com.example.dormconnectapp.databinding.FragmentProfileBinding
import com.example.dormconnectapp.ui.feed.FeedAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let {
                val localPath = saveImageLocally(it)
                val uid = auth.currentUser?.uid ?: return@let

                db.collection("users").document(uid)
                    .update("profileImageUrl", localPath)
                    .addOnSuccessListener {
                        Glide.with(this).load(File(localPath)).into(binding.profileImageView)
                        Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update image", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = auth.currentUser
        val uid = user?.uid ?: return

        // Load profile data from Firestore
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Anonymous"
                val email = user.email ?: "No Email"

                binding.usernameText.text = name
                binding.emailText.text = email
                binding.editName.setText(name)
                binding.editEmail.setText(email)

                val localPath = doc.getString("profileImageUrl")
                if (!localPath.isNullOrEmpty()) {
                    Glide.with(this).load(File(localPath)).into(binding.profileImageView)
                }
            }

        // Edit toggle
        binding.btnEdit.setOnClickListener {
            binding.usernameText.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.editName.visibility = View.VISIBLE
            binding.editEmail.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.editName.text.toString().trim()
            val newEmail = binding.editEmail.text.toString().trim()

            if (newName.isNotEmpty()) {
                db.collection("users").document(uid).update("name", newName)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(profileUpdates)
                binding.usernameText.text = newName
            }

            if (newEmail.isNotEmpty() && newEmail != user.email) {
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        binding.emailText.text = newEmail
                        Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update email", Toast.LENGTH_SHORT).show()
                    }
            }

            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()

            binding.usernameText.visibility = View.VISIBLE
            binding.emailText.visibility = View.VISIBLE
            binding.editName.visibility = View.GONE
            binding.editEmail.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
            binding.btnEdit.visibility = View.VISIBLE
        }

        // Image picker
        binding.btnChangePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        // Logout
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val navController = findNavController()
            navController.navigate(R.id.logInFragment)
        }



        binding.userPostsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val currentUsername = FirebaseAuth.getInstance().currentUser?.displayName ?: return
        FirebaseFirestore.getInstance().collection("feed_posts")
            .whereEqualTo("username", currentUsername)
            .get()
            .addOnSuccessListener { snapshot ->
                val userPosts = snapshot.mapNotNull { it.toObject(FeedPost::class.java).apply { postId = it.id } }
                binding.userPostsRecyclerView.adapter = FeedAdapter(userPosts, null, null)
            }

    }

    private fun saveImageLocally(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(requireContext().filesDir, fileName)
        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
