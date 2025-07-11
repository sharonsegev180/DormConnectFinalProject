package com.example.dormconnectapp

data class FeedPost(
    val username: String,
    val profileImageRes: Int,
    val content: String,
    val postImageRes: Int? = null
)
