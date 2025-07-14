package com.example.dormconnectapp.data

import com.google.firebase.Timestamp

data class FeedPost(
    @JvmField
    var postId: String? = null,
    var username: String = "",
    var content: String = "",
    var profileImageUrl: String? = null,
    var postImageUrl: String? = null,
    var timestamp: Timestamp? = null,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
)
