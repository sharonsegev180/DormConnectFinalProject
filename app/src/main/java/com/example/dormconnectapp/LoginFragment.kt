package com.example.dormconnectapp

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
import androidx.navigation.fragment.findNavController


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

        val universities = arrayOf("Select University", "BIU", "Tel Aviv University", "Haifa University")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, universities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUniversity.adapter = adapter


        auth = FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener {
            val university = binding.spinnerUniversity.selectedItem.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val rememberMe = binding.checkBoxRememberMe.isChecked

            if (university == "Select University" || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Logging in...", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
            }
        }

        binding.buttonSignUp.setOnClickListener {
            val university = binding.spinnerUniversity.selectedItem.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (university == "Select University" || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.feedFragment)
                        } else {
                            Toast.makeText(requireContext(), "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
