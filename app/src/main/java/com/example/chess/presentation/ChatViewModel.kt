package com.example.chess.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.model.AnalysisResult
import com.example.chess.data.remote.GroqApiException
import com.example.chess.data.repository.ChatRepository
import com.example.chess.domain.model.ChatMessage
import com.example.chess.domain.model.ChatRole
import com.example.chess.payments.domain.repository.EntitlementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val TAG = "ChatViewModel"

// GroqApi now retries once on a 429/5xx before giving up (see GroqApi.retryWithBackoff):
// worst case is roughly a fast-failing first attempt + backoff (up to 5s) + a full
// 15s connect/read timeout on the second attempt, ~22s. 25s leaves headroom above that
// so a retry that was about to succeed isn't cut off mid-flight.
private const val SEND_TIMEOUT_MS = 25_000L

// How long a 429-triggered chat disablement lasts before auto-recovering. Matches
// Groq's own per-minute rate-limit window — see ChessGameViewModel's identical constant
// and _aiExplanationDisabled comment for the full reasoning.
private const val CHAT_DISABLE_COOLDOWN_MS = 60_000L

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val entitlements: EntitlementRepository,
) : ViewModel() {

    // The nav graph gates the whole chat route on this (paywall renders instead);
    // exposed from the VM so the gate flips reactively the moment a purchase lands.
    val isPremium: StateFlow<Boolean> = entitlements.isPremium

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Mirrors ChessGameViewModel's _aiExplanationDisabled — set on a 429 (after GroqApi's
    // own retry is exhausted) to stop hammering the API, surfaced to the UI to disable
    // the send button/show a banner. Self-resets after CHAT_DISABLE_COOLDOWN_MS via
    // chatCooldownJob — the send button can't be tapped to trigger a lazy re-check, so
    // the flag must flip back on its own.
    private val _chatDisabled = MutableStateFlow(false)
    val chatDisabled: StateFlow<Boolean> = _chatDisabled.asStateFlow()
    private var chatCooldownJob: Job? = null

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var sendJob: Job? = null

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage(analysisResult: AnalysisResult?) {
        // Belt-and-braces behind the nav-level gate: chat is premium-only and every
        // message costs an API call.
        if (!entitlements.isPremium.value) return
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
                    chatCooldownJob?.cancel()
                    chatCooldownJob = viewModelScope.launch {
                        delay(CHAT_DISABLE_COOLDOWN_MS)
                        _chatDisabled.value = false
                    }
                } else {
                    _errorMessage.value = "Couldn't reach the AI coach. Please try again."
                }
            } finally {
                _isSending.value = false
            }
        }
    }
}
