package com.example.dormconnectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dormconnectapp.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope





class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.textViewWelcomeHome.text = "Welcome to DormConnect!"

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1000) // 1 seconds
            findNavController().navigate(R.id.Feedfragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
