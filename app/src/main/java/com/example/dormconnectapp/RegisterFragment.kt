package com.example.dormconnectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormconnectapp.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val universities = arrayOf("Select University", "Bar-Ilan", "Tel Aviv", "Hebrew", "Technion")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, universities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUniversity.adapter = adapter

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        return binding.root
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val university = binding.spinnerUniversity.selectedItem.toString()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (university == "Select University") {
            Toast.makeText(context, "Please select a university", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnRegister.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnRegister.isEnabled = true

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "university" to university
                    )

                    firestore.collection("users").document(userId).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Registered successfully!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to save user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
            }
}