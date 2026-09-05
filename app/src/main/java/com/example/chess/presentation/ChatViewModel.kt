package com.example.chess.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.model.AnalysisResult
import com.example.chess.data.remote.GroqApiException
import com.example.chess.data.repository.ChatRepository
import com.example.chess.domain.model.ChatMessage
import com.example.chess.domain.model.ChatRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val TAG = "ChatViewModel"

// Larger than ChessGameViewModel's move-explanation timeout: this is the user's
// primary, blocking action on this screen (they're waiting on a real answer with up
// to 400 output tokens), not a background fill-in with a deterministic fallback.
// Matches GroqApi's own connect/read timeouts (15s each).
private const val SEND_TIMEOUT_MS = 15_000L

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Mirrors ChessGameViewModel's _aiExplanationDisabled — set on a 429 to stop
    // hammering the API, surfaced to the UI to disable the send button/show a banner.
    private val _chatDisabled = MutableStateFlow(false)
    val chatDisabled: StateFlow<Boolean> = _chatDisabled.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var sendJob: Job? = null

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(analysisResult: AnalysisResult?) {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isSending.value || _chatDisabled.value) return

        val history = _messages.value
        _messages.value = history + ChatMessage(ChatRole.USER, text)
        _inputText.value = ""
        _errorMessage.value = null

        sendJob = viewModelScope.launch {
            _isSending.value = true
            try {
                val reply = withTimeout(SEND_TIMEOUT_MS) {
                    chatRepository.sendMessage(history, text, analysisResult)
                }
                _messages.value = _messages.value + ChatMessage(ChatRole.COACH, reply)
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage: failed", e)
                if (e is GroqApiException && e.statusCode == 429) {
                    _chatDisabled.value = true
                    _errorMessage.value = "The AI coach is rate-limited right now — please try again later."
                } else {
                    _errorMessage.value = "Couldn't reach the AI coach. Please try again."
                }
            } finally {
                _isSending.value = false
            }
        }
    }
}
