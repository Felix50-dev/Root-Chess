package com.example.chess.data.remote

import com.example.chess.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

// Carries the HTTP status code so callers can react to specific failures (e.g. 429
// rate-limiting) without string-matching the exception message.
class GroqApiException(val statusCode: Int, message: String) : Exception(message)

// Free-tier, OpenAI-compatible chat completions API (https://console.groq.com/docs/api-reference).
// Hosts open-source models — no per-call billing, no prepay wall, unlike the Gemini API this
// replaced. If MODEL below ever starts 404ing ("model decommissioned"), check the current
// list at https://console.groq.com/docs/models and swap it — same failure mode we hit with
// Gemini's retired gemini-2.0-flash-lite.
class GroqApi @Inject constructor() {
    private val apiKey = BuildConfig.GROQ_API_KEY

    suspend fun complete(prompt: String): String {
        val requestBody = baseRequestBody().apply {
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            // MODEL is a reasoning model — hidden reasoning tokens come out of this same
            // budget before the model writes the visible answer. A raw FEN string in the
            // prompt (an earlier version of this fix) made reasoning usage huge and volatile
            // on verification-heavy positions (250-300+ tokens, sometimes truncating the
            // answer entirely). MoveExplainer now sends pre-verified plain-English facts
            // instead of a FEN (see ExplanationFacts/GamePlay.explanationFacts) — reasoning
            // dropped back to ~6-26 tokens even on forced-mate explanations, empirically
            // verified with zero truncation across 8 live-API test runs. 500 is a generous
            // ceiling against runaway output, not a budget this prompt shape is expected to
            // approach; the prompt's own "max 2 sentences" instruction is what actually
            // keeps the visible answer short.
            put("temperature", 0.6)
            put("max_tokens", 500)
        }
        return executeChatCompletion(requestBody)
    }

    // Multi-turn chat variant of complete(). role in each `turns` pair must be OpenAI-style
    // chat-completions vocabulary: "user" or "assistant".
    suspend fun completeChat(
        systemInstruction: String,
        turns: List<Pair<String, String>>,
        maxOutputTokens: Int = 400,
        temperature: Double = 0.7,
    ): String {
        val requestBody = baseRequestBody().apply {
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
                turns.forEach { (role, text) ->
                    put(JSONObject().apply {
                        put("role", role)
                        put("content", text)
                    })
                }
            })
            put("temperature", temperature)
            put("max_tokens", maxOutputTokens)
        }
        return executeChatCompletion(requestBody)
    }

    // MODEL is a "reasoning" model (OpenAI's gpt-oss family) — left unconfigured, it burns
    // the entire max_tokens budget on internal chain-of-thought (a separate "reasoning"
    // response field) and can return an EMPTY content string with finish_reason "length"
    // before ever writing an answer. reasoning_effort="low" keeps that internal budget
    // small (~5-10 tokens observed vs. ~100 at the default), and include_reasoning=false
    // keeps the (still model-required) reasoning text out of the response payload.
    private fun baseRequestBody(): JSONObject = JSONObject().apply {
        put("model", MODEL)
        put("reasoning_effort", "low")
        put("include_reasoning", false)
    }

    private suspend fun executeChatCompletion(requestBody: JSONObject): String =
        withContext(Dispatchers.IO) {
            val url = "https://api.groq.com/openai/v1/chat/completions"
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw GroqApiException(responseCode, "API error $responseCode: $error")
            }

            val json = JSONObject(responseText)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }

    private companion object {
        // Verified against this account's actual GET /openai/v1/models response —
        // don't reintroduce a model name from docs/memory without checking that
        // endpoint first. Groq's catalog (and per-account access to it) changes;
        // this is exactly the failure mode that hit the retired Gemini model.
        const val MODEL = "openai/gpt-oss-120b"
    }
}
