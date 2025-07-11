package com.example.dormconnectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dormconnectapp.databinding.FragmentFeedBinding

class Feedfragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sample feed data (replace drawables with actual ones in res/drawable)
        val feedList = listOf(
            FeedPost("ותת", R.drawable.user1, "ועון הסירה\nקומה 2 קומה 2\n204", R.drawable.clock),
            FeedPost("יוסי", R.drawable.user2, "מי רוצה לשבת איתי במסעדה?", R.drawable.pin_icon)
        )

        val adapter = FeedAdapter(feedList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
