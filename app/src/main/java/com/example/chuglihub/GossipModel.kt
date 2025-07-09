package com.example.chuglihub

data class GossipModel(
    val senderName: String = "",
    val message: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)