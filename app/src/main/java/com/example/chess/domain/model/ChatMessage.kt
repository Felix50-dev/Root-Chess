package com.example.chess.domain.model

data class ChatMessage(
    val role: ChatRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class ChatRole { USER, COACH }
