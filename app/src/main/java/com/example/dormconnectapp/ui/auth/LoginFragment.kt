package com.example.dormconnectapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dormconnectapp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.dormconnectapp.R
import com.google.firebase.firestore.FirebaseFirestore


class LogInFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val universities = arrayOf("Select University", "Bar-Ilan", "Tel Aviv", "Hebrew", "Technion")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, universities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUniversity.adapter = adapter


        auth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val university = binding.spinnerUniversity.selectedItem.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (university == "Select University" || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    val university = document.getString("university")
                                    Toast.makeText(requireContext(), "Logged in as: $university", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Login successful, but failed to load profile", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
                                }

                        } else {
                            Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }

            }

        }



        binding.buttonSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_registerFragment)
        }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
