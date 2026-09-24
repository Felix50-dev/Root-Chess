package com.example.chess.data.repository

import com.example.chess.data.model.AnalysisResult
import com.example.chess.data.remote.GroqApi
import com.example.chess.domain.model.ChatContextBuilder
import com.example.chess.domain.model.ChatMessage
import com.example.chess.domain.model.ChatRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val groqApi: GroqApi,  
    private val gamePlay: GamePlay,
) {
    suspend fun sendMessage(
        history: List<ChatMessage>,
        newUserText: String,
        analysisResult: AnalysisResult?,
    ): String {
        // movesPlayed is a Collections.synchronizedList — compound operations like
        // snapshotting need an external synchronized block (same pattern GamePlay's
        // own handleAIMove uses).
        val moveSnapshot = synchronized(gamePlay.movesPlayed) { gamePlay.movesPlayed.toList() }
        val moveNotation = gamePlay.convertMovesToNotation(moveSnapshot)
        val systemInstruction = ChatContextBuilder.build(moveNotation, analysisResult)
        val turns = history.map { it.role.toApiRole() to it.text } + ("user" to newUserText)
        return groqApi.completeChat(systemInstruction = systemInstruction, turns = turns)
    }

    private fun ChatRole.toApiRole() = when (this) {
        ChatRole.USER -> "user"
        ChatRole.COACH -> "assistant"
    }
}
