package com.example.dormconnectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dormconnectapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = auth.currentUser
        val uid = user?.uid
        val db = FirebaseFirestore.getInstance()

        // Load profile data from Firestore
        if (uid != null) {
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "Anonymous"
                    val email = user.email ?: "No Email"

                    binding.usernameText.text = name
                    binding.emailText.text = email
                    binding.editName.setText(name)
                    binding.editEmail.setText(email)
                }
        }

        // Edit mode toggle
        binding.btnEdit.setOnClickListener {
            binding.usernameText.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.editName.visibility = View.VISIBLE
            binding.editEmail.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
        }

        // Save button
        binding.btnSave.setOnClickListener {
            val newName = binding.editName.text.toString().trim()
            val newEmail = binding.editEmail.text.toString().trim()

            if (uid != null && newName.isNotEmpty()) {
                // Update Firestore
                db.collection("users").document(uid).update("name", newName)
                // Update Auth display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user?.updateProfile(profileUpdates)

                binding.usernameText.text = newName
            }

            if (newEmail.isNotEmpty() && newEmail != user?.email) {
                user?.updateEmail(newEmail)?.addOnSuccessListener {
                    binding.emailText.text = newEmail
                    Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener {
                    Toast.makeText(context, "Failed to update email", Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()

            // Toggle back to view mode
            binding.usernameText.visibility = View.VISIBLE
            binding.emailText.visibility = View.VISIBLE
            binding.editName.visibility = View.GONE
            binding.editEmail.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
            binding.btnEdit.visibility = View.VISIBLE
        }

        // Logout
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            requireActivity().recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
